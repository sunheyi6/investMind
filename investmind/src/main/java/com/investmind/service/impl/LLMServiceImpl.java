package com.investmind.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.investmind.dto.ReportRequest;
import com.investmind.service.LLMService;
import com.investmind.service.VectorService;
import com.investmind.service.WebResearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class LLMServiceImpl implements LLMService {

    private static final String BASE_SYSTEM_PROMPT_PATH = "classpath:prompts/system/base-system-en.md";
    private static final String REPORT_SYSTEM_PROMPT_PATH = "classpath:prompts/system/report-system-en.md";
    private static final String QA_SYSTEM_PROMPT_PATH = "classpath:prompts/system/qa-system-en.md";
    private static final String PHILOSOPHY_EXTRACT_SYSTEM_PROMPT_PATH = "classpath:prompts/system/philosophy-extract-system-en.md";
    private static final String PHILOSOPHY_DOC_SYSTEM_PROMPT_PATH = "classpath:prompts/system/philosophy-document-system-en.md";
    private static final Pattern TEMPLATE_VAR_PATTERN = Pattern.compile("\\$\\{([A-Z0-9_]+)}");

    private final VectorService vectorService;
    private final WebResearchService webResearchService;
    private final ResourceLoader resourceLoader;

    @Value("${investmind.ai.api-key:}")
    private String apiKey;

    @Value("${investmind.ai.base-url:}")
    private String baseUrl;

    @Value("${investmind.ai.model:moonshot-v1-8k}")
    private String model;

    @Value("${investmind.ai.chat-endpoint:/chat/completions}")
    private String chatEndpoint;

    @Value("${investmind.ai.max-tokens:1000}")
    private Integer maxTokens;

    @Value("${investmind.ai.temperature:0.7}")
    private Double temperature;

    public LLMServiceImpl(
            @Lazy VectorService vectorService,
            WebResearchService webResearchService,
            ResourceLoader resourceLoader) {
        this.vectorService = vectorService;
        this.webResearchService = webResearchService;
        this.resourceLoader = resourceLoader;
    }

    private static final String DEFAULT_PROMPT = """
            请作为专业投资分析师，生成一份今日投资分析报告（约500-800字）。
            
            报告应包含以下部分：
            ## 一、市场概况
            简要描述今日整体市场走势和主要指数表现
            
            ## 二、板块分析  
            分析表现突出或值得关注的主要板块
            
            ## 三、策略建议
            给出具体的投资策略和操作方向
            
            请以Markdown格式输出，语言专业、简洁、有条理。
            """;

    @Override
    public String generateReport(ReportRequest.GenerateRequest request) {
        return generateReport(request, null);
    }

    @Override
    public String generateReport(ReportRequest.GenerateRequest request, Long userId) {
        String prompt = buildPrompt(request, userId);
        return callLLMApi(prompt);
    }

    @Override
    public List<Float> getEmbedding(String text) {
        log.warn("Embedding service not configured, returning empty list");
        return new ArrayList<>();
    }

    @Override
    public String buildEnhancedPrompt(String basePrompt, List<String> historicalContent) {
        if (historicalContent == null || historicalContent.isEmpty()) {
            return basePrompt;
        }

        StringBuilder enhancedPrompt = new StringBuilder();
        enhancedPrompt.append(basePrompt).append("\n\n");
        enhancedPrompt.append("【参考历史优质内容】\n");
        
        for (int i = 0; i < historicalContent.size(); i++) {
            enhancedPrompt.append("参考").append(i + 1).append(":\n");
            enhancedPrompt.append(historicalContent.get(i), 0, 
                    Math.min(200, historicalContent.get(i).length())).append("...\n\n");
        }
        
        enhancedPrompt.append("请结合以上参考内容，生成新的投资分析报告。");
        
        return enhancedPrompt.toString();
    }

    @Override
    public String answerQuestion(String question, String investmentContext) {
        if (isPhilosophyRecallQuestion(question)) {
            if (!StringUtils.hasText(investmentContext)) {
                return "你当前还没有可用的投资理念内容。请先在“投资理念”页面填写，或使用 /add 录入后再让我复述。";
            }
            return "以下是你当前保存的投资理念（按数据库内容整理展示）：\n\n" + investmentContext;
        }

        if (isPhilosophySummaryQuestion(question)) {
            if (!StringUtils.hasText(investmentContext)) {
                return "你当前还没有可用的投资理念内容，暂时无法总结。请先录入投资理念后再试。";
            }
            return buildPhilosophySummaryFromContext(investmentContext);
        }

        String webContext = webResearchService.research(question);
        StringBuilder prompt = new StringBuilder();
        if (StringUtils.hasText(investmentContext)) {
            prompt.append("\n\n【用户投资理念】\n").append(investmentContext);
        }
        if (StringUtils.hasText(webContext)) {
            prompt.append("\n\n【互联网检索摘要（最新）】\n").append(webContext);
        } else {
            prompt.append("\n\n【互联网检索摘要（最新）】\n未获取到可靠联网数据，必须在回答中明确数据不足，不得编造。");
        }
        prompt.append("\n\n【用户问题】\n").append(question);
        if (!StringUtils.hasText(apiKey)) {
            log.warn("未配置AI API Key，问答接口使用本地降级回答");
            return buildFallbackAnswer(question, investmentContext, webContext);
        }
        return callWithSystemPrompt(
                QA_SYSTEM_PROMPT_PATH,
                "You are an investment research assistant. Follow the user's investment philosophy first, and respond in Chinese with structured, actionable output.",
                prompt.toString());
    }

    @Override
    public Map<String, String> extractPhilosophyFields(String inputText, String currentContext) {
        if (!StringUtils.hasText(apiKey)) {
            return heuristicExtract(inputText);
        }

        String prompt = """
                当前已有投资理念：
                %s

                用户最新输入：
                %s
                """.formatted(StringUtils.hasText(currentContext) ? currentContext : "无", inputText);

        String raw = callWithSystemPrompt(
                PHILOSOPHY_EXTRACT_SYSTEM_PROMPT_PATH,
                "You extract investment philosophy fields and only return a JSON object.",
                prompt);
        String jsonText = extractJsonObject(raw);
        if (!StringUtils.hasText(jsonText)) {
            return heuristicExtract(inputText);
        }

        try {
            JSONObject json = JSON.parseObject(jsonText);
            Map<String, String> out = new HashMap<>();
            for (String key : json.keySet()) {
                String val = json.getString(key);
                if (StringUtils.hasText(val)) {
                    out.put(key, val.trim());
                }
            }
            return out.isEmpty() ? heuristicExtract(inputText) : out;
        } catch (Exception e) {
            log.warn("解析投资理念JSON失败，回退启发式解析", e);
            return heuristicExtract(inputText);
        }
    }

    @Override
    public String generatePhilosophyDocument(String structuredContext) {
        if (!StringUtils.hasText(apiKey)) {
            return fallbackDocument(structuredContext);
        }

        String prompt = """
                结构化内容：
                %s
                """.formatted(structuredContext);
        return callWithSystemPrompt(
                PHILOSOPHY_DOC_SYSTEM_PROMPT_PATH,
                "You are an investment writing assistant. Produce a concise and professional Markdown document without hallucination.",
                prompt);
    }

    private String buildPrompt(ReportRequest.GenerateRequest request, Long userId) {
        StringBuilder prompt = new StringBuilder();

        if (StringUtils.hasText(request.getCustomPrompt())) {
            prompt.append(request.getCustomPrompt());
        } else {
            prompt.append(DEFAULT_PROMPT);
        }

        prompt.append("\n\n报告日期: ").append(request.getReportDate());
        prompt.append("\n报告类型: ").append(request.getReportType());

        if (Boolean.TRUE.equals(request.getUseHistoricalContent())) {
            try {
                List<String> similarContent = vectorService.searchSimilarContent(
                        request.getReportDate() + "投资分析", 3);
                if (!similarContent.isEmpty()) {
                    prompt.append("\n\n【历史优质内容参考】\n");
                    for (int i = 0; i < similarContent.size(); i++) {
                        prompt.append("参考").append(i + 1).append(": ")
                              .append(similarContent.get(i), 0,
                                      Math.min(300, similarContent.get(i).length()))
                              .append("...\n");
                    }
                }
            } catch (Exception e) {
                log.warn("检索历史内容失败", e);
            }
        }

        return prompt.toString();
    }

    private String callLLMApi(String prompt) {
        if (!StringUtils.hasText(apiKey)) {
            log.warn("未配置AI API Key，返回模拟数据");
            return generateMockReport();
        }

        return callWithSystemPrompt(
                REPORT_SYSTEM_PROMPT_PATH,
                "You are a professional investment research assistant. Respond in Chinese with structured analysis and explicit risk warnings.",
                prompt);
    }

    private String extractJsonObject(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return null;
    }

    private String callWithSystemPrompt(String promptPath, String fallbackSystemPrompt, String userPrompt) {
        try {
            String systemPrompt = loadSystemPrompt(promptPath, fallbackSystemPrompt);
            return callApi(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.error("调用大模型API失败", e);
            String message = e.getMessage();
            if (StringUtils.hasText(message) && message.contains("status: 401")) {
                throw new RuntimeException("生成内容失败: 大模型鉴权失败（401），请检查 AI_API_KEY 是否正确，且与 base-url/model 匹配");
            }
            throw new RuntimeException("生成内容失败: " + message);
        }
    }

    private String callApi(String systemPrompt, String userPrompt) throws Exception {
        String url = buildChatUrl();
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);
        
        JSONArray messages = new JSONArray();
        messages.add(JSON.parseObject("{\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"}"));
        messages.add(JSON.parseObject("{\"role\": \"user\", \"content\": \"" + escapeJson(userPrompt) + "\"}"));
        requestBody.put("messages", messages);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> {
                if (response.getCode() >= 200 && response.getCode() < 300) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject jsonResponse = JSON.parseObject(responseBody);
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty()) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject message = choice.getJSONObject("message");
                        if (message != null) {
                            return message.getString("content");
                        }
                    }
                    throw new RuntimeException("API response format error");
                } else {
                    String errorBody = "";
                    if (response.getEntity() != null) {
                        errorBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                        if (errorBody.length() > 500) {
                            errorBody = errorBody.substring(0, 500);
                        }
                    }
                    throw new RuntimeException("API request failed with status: " + response.getCode() +
                            (StringUtils.hasText(errorBody) ? (", body: " + errorBody) : ""));
                }
            });
        }
    }

    private String buildChatUrl() {
        String base = StringUtils.hasText(baseUrl) ? baseUrl.trim() : "";
        String endpoint = StringUtils.hasText(chatEndpoint) ? chatEndpoint.trim() : "/chat/completions";
        if (base.endsWith("/") && endpoint.startsWith("/")) {
            return base.substring(0, base.length() - 1) + endpoint;
        }
        if (!base.endsWith("/") && !endpoint.startsWith("/")) {
            return base + "/" + endpoint;
        }
        return base + endpoint;
    }

    private String buildFallbackAnswer(String question, String investmentContext, String webContext) {
        String philosophy = StringUtils.hasText(investmentContext) ? investmentContext : "（未设置投资理念）";
        String webHint = StringUtils.hasText(webContext) ? "已获取到部分联网摘要，可结合判断。" : "未获取到可靠联网数据，请谨慎决策。";
        return """
                当前系统未配置可用的大模型密钥，先提供降级分析建议：

                ## 结论摘要
                - 问题：%s
                - 联网状态：%s
                - 建议：先补齐关键财务与估值数据，再决定交易动作。

                ## 执行建议
                1. 按你的投资理念先做一轮筛选，剔除不符合风险偏好的标的。
                2. 优先核对三项硬指标：现金流、估值区间、盈利可持续性。
                3. 若准备建仓，采用分批进场并设置止损位，避免一次性重仓。

                ## 你的投资理念参考
                %s

                > 提示：配置 `AI_API_KEY` 后可恢复真实大模型回答。
                """.formatted(question, webHint, philosophy);
    }

    private boolean isPhilosophyRecallQuestion(String question) {
        if (!StringUtils.hasText(question)) {
            return false;
        }
        String q = question.trim();
        boolean asksPhilosophy = q.contains("投资理念");
        boolean asksRecall = q.contains("是什么")
                || q.contains("完整")
                || q.contains("复述")
                || q.contains("原文")
                || q.contains("全部")
                || q.contains("完整拿出来");
        return asksPhilosophy && asksRecall;
    }

    private boolean isPhilosophySummaryQuestion(String question) {
        if (!StringUtils.hasText(question)) {
            return false;
        }
        String q = question.trim();
        boolean asksPhilosophy = q.contains("投资理念");
        boolean asksSummary = q.contains("总结")
                || q.contains("归纳")
                || q.contains("提炼")
                || q.contains("简化")
                || q.contains("概括");
        return asksPhilosophy && asksSummary;
    }

    private String buildPhilosophySummaryFromContext(String investmentContext) {
        String[] lines = investmentContext.split("\\r?\\n");
        StringBuilder out = new StringBuilder("基于你当前保存的投资理念，这里是简要总结（不改写核心含义）：\n\n");
        int count = 0;
        for (String line : lines) {
            if (!StringUtils.hasText(line)) {
                continue;
            }
            out.append("- ").append(line.trim()).append("\n");
            count++;
            if (count >= 6) {
                break;
            }
        }
        if (count == 0) {
            return "你当前还没有可用于总结的投资理念字段。";
        }
        out.append("\n如需“完整原文”，可以直接问：我的投资理念是什么？请完整复述。");
        return out.toString();
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private String loadSystemPrompt(String promptPath, String fallbackSystemPrompt) {
        Map<String, String> variables = new HashMap<>();
        variables.put("CURRENT_DATE", LocalDate.now().toString());
        variables.put("MODEL_NAME", StringUtils.hasText(model) ? model : "unknown-model");
        try {
            String basePrompt = loadPromptText(BASE_SYSTEM_PROMPT_PATH);
            String scenePrompt = loadPromptText(promptPath);
            String merged = StringUtils.hasText(basePrompt) ? (basePrompt + "\n\n" + scenePrompt) : scenePrompt;
            return renderTemplate(merged, variables);
        } catch (Exception e) {
            log.warn("读取系统提示词失败，使用默认提示词: {}", promptPath, e);
            return fallbackSystemPrompt;
        }
    }

    private String loadPromptText(String promptPath) throws Exception {
        Resource resource = resourceLoader.getResource(promptPath);
        if (!resource.exists()) {
            throw new IllegalStateException("Prompt file not found: " + promptPath);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
    }

    private String renderTemplate(String template, Map<String, String> variables) {
        Matcher matcher = TEMPLATE_VAR_PATTERN.matcher(template);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String val = variables.get(key);
            if (val == null) {
                throw new IllegalStateException("Missing prompt variable: " + key);
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(val));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private Map<String, String> heuristicExtract(String inputText) {
        Map<String, String> map = new HashMap<>();
        String t = inputText == null ? "" : inputText.trim();
        if (!StringUtils.hasText(t)) {
            return map;
        }
        map.put("philosophyDescription", t);
        map.put("strategyNotes", t);
        if (t.contains("低风险") || t.contains("稳健") || t.contains("保守")) {
            map.put("riskPreference", "CONSERVATIVE");
        } else if (t.contains("激进") || t.contains("高风险")) {
            map.put("riskPreference", "AGGRESSIVE");
        } else {
            map.put("riskPreference", "MODERATE");
        }
        if (t.contains("长期") || t.contains("三年") || t.contains("五年")) {
            map.put("investmentHorizon", "LONG");
            map.put("holdingPeriod", "长期持有为主");
        } else if (t.contains("短线") || t.contains("短期")) {
            map.put("investmentHorizon", "SHORT");
            map.put("holdingPeriod", "偏短周期，重视节奏");
        } else {
            map.put("investmentHorizon", "MEDIUM");
            map.put("holdingPeriod", "中期持有，结合景气与估值调整");
        }
        return map;
    }

    private String fallbackDocument(String structuredContext) {
        return """
                # 我的投资理念文档

                ## 总体原则
                %s

                ## 执行框架
                - 以风险控制为前提，优先保证组合稳定性
                - 基于估值与基本面进行仓位和节奏管理
                - 持续复盘，按规则优化而非情绪化交易

                ## 风险声明
                本文档用于投资研究与策略复盘，不构成任何收益承诺或个股保证。
                """.formatted(StringUtils.hasText(structuredContext) ? structuredContext : "暂无已沉淀的结构化理念，请先输入你的投资思路。");
    }

    private String generateMockReport() {
        return """
                ## 一、市场概况
                
                今日A股市场整体呈现震荡上行态势。早盘开盘后，主要指数小幅低开，随后在权重板块带动下逐步回升。上证指数收涨0.45%，深证成指上涨0.62%，创业板指表现相对强势，收涨0.89%。两市成交额较前一交易日有所放大，市场活跃度提升。
                
                ## 二、板块分析
                
                **领涨板块：**
                1. **新能源板块**：受政策利好刺激，光伏、锂电池相关个股表现活跃
                2. **科技板块**：半导体、人工智能概念股延续强势
                3. **医药板块**：创新药概念股集体反弹
                
                **调整板块：**
                1. **银行板块**：受利率预期影响，银行板块小幅调整
                2. **地产板块**：板块内部分化，整体表现偏弱
                
                ## 三、策略建议
                
                1. **短期策略**：建议保持适度仓位，关注市场成交量变化。可适当参与科技成长板块的轮动机会。
                
                2. **中期布局**：重点关注业绩确定性较强的新能源、医药等优质赛道，逢低布局。
                
                3. **风险控制**：设置合理止损位，控制个股仓位，避免追高操作。
                
                *以上为模拟生成内容，实际使用时请配置真实的AI API Key。*
                """;
    }
}
