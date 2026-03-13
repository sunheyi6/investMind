package com.investmind.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.investmind.dto.ReportRequest;
import com.investmind.service.LLMService;
import com.investmind.service.VectorService;
import com.investmind.service.WebResearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 大模型服务实现类
 * 这里以 OpenAI 格式为例，可根据实际情况替换为其他模型
 */
@Slf4j
@Service
public class LLMServiceImpl implements LLMService {

    @Value("${investmind.ai.api-key:}")
    private String apiKey;

    @Value("${investmind.ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${investmind.ai.model:gpt-4}")
    private String model;

    @Value("${investmind.ai.max-tokens:1000}")
    private Integer maxTokens;

    @Value("${investmind.ai.temperature:0.7}")
    private Double temperature;

    @Value("${investmind.ai.chat-endpoint:/chat/completions}")
    private String chatEndpoint;

    @Value("${investmind.ai.embedding-endpoint:/embeddings}")
    private String embeddingEndpoint;

    @Value("${investmind.ai.embedding-model:text-embedding-ada-002}")
    private String embeddingModel;

    @Lazy
    @Autowired
    private VectorService vectorService;

    @Autowired
    private WebResearchService webResearchService;

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
        // 构建提示词
        String prompt = buildPrompt(request, userId);

        // 调用大模型API
        return callLLMApi(prompt);
    }

    @Override
    public List<Float> getEmbedding(String text) {
        try {
            String url = buildUrl(baseUrl, embeddingEndpoint);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("input", text);
            requestBody.put("model", embeddingModel); // 或其他 embedding 模型

            httpPost.setEntity(new StringEntity(JSON.toJSONString(requestBody), StandardCharsets.UTF_8));

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getCode();
                String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JSONObject json = JSON.parseObject(result);

                if (json.containsKey("data")) {
                    JSONObject data = json.getJSONArray("data").getJSONObject(0);
                    List<Float> embedding = new ArrayList<>();
                    for (Object val : data.getJSONArray("embedding")) {
                        embedding.add(((Number) val).floatValue());
                    }
                    return embedding;
                } else {
                    log.warn("Embedding接口返回异常，status={}, body={}", statusCode, result);
                }
            }
        } catch (Exception e) {
            log.error("获取向量表示失败", e);
        }
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
        String webContext = webResearchService.research(question);
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位严格执行用户投资体系的投研助手，请使用中文回答。");
        prompt.append("\n核心原则：必须以用户投资理念为第一优先级，不能给与理念冲突的建议。");
        prompt.append("\n输出格式要求：");
        prompt.append("\n8.1 关键结论（3-5条，量化表达）");
        prompt.append("\n8.2 关键数据与证据（列出来源线索，避免空泛）");
        prompt.append("\n8.3 操作建议（必须给出价格区间+仓位+触发条件）");
        prompt.append("\n8.4 可比标的对比（至少1个可比对象）");
        prompt.append("\n8.5 一句话总结");
        prompt.append("\n并在结尾增加：数据置信度声明 + 风险提示。");
        prompt.append("\n禁止输出空泛宏观描述，必须落到可执行层。");
        if (StringUtils.hasText(investmentContext)) {
            prompt.append("\n\n【用户投资理念】\n").append(investmentContext);
        }
        if (StringUtils.hasText(webContext)) {
            prompt.append("\n\n【互联网检索摘要（最新）】\n").append(webContext);
        } else {
            prompt.append("\n\n【互联网检索摘要（最新）】\n未获取到可靠联网数据，必须在回答中明确数据不足，不得编造。");
        }
        prompt.append("\n\n【用户问题】\n").append(question);
        return callLLMApi(prompt.toString());
    }

    @Override
    public Map<String, String> extractPhilosophyFields(String inputText, String currentContext) {
        if (!StringUtils.hasText(apiKey) || "your-api-key".equals(apiKey)) {
            return heuristicExtract(inputText);
        }

        String prompt = """
                你是投资理念结构化助手。请将用户输入解析为JSON对象，只输出JSON，不要任何额外文本。
                可选字段键如下：
                coreInvestmentPhilosophy, stockSelectionCriteria, valuationLogic,
                positionManagementRules, sellConditions, riskPreference, holdingPeriod,
                investmentHorizon, industryRestrictions, preferredSectors, avoidSectors,
                riskManagement, strategyNotes, philosophyDescription

                规则：
                1) 只返回有明确信息的字段
                2) 字段值使用中文短段落
                3) 不要返回null

                当前已有投资理念：
                """ + (StringUtils.hasText(currentContext) ? currentContext : "无") + """

                用户最新输入：
                """ + inputText;

        String raw = callLLMApi(prompt);
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
        if (!StringUtils.hasText(apiKey) || "your-api-key".equals(apiKey)) {
            return fallbackDocument(structuredContext);
        }

        String prompt = """
                你是投资研究写作助手。请把给定结构化字段整理成一篇完整、专业、连贯的投资理念文档。
                要求：
                1) 使用Markdown标题结构
                2) 不要编造不存在的信息
                3) 语言简洁专业
                4) 明确风险边界与执行纪律

                结构化内容：
                """ + structuredContext;
        return callLLMApi(prompt);
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(ReportRequest.GenerateRequest request, Long userId) {
        StringBuilder prompt = new StringBuilder();

        // 如果有自定义提示词则使用，否则使用默认
        if (StringUtils.hasText(request.getCustomPrompt())) {
            prompt.append(request.getCustomPrompt());
        } else {
            prompt.append(DEFAULT_PROMPT);
        }

        // 添加日期信息
        prompt.append("\n\n报告日期: ").append(request.getReportDate());
        prompt.append("\n报告类型: ").append(request.getReportType());

        // 如果使用历史内容增强，则检索相似内容
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

        // TODO: 第三阶段将融入用户投资理念和学习模式

        return prompt.toString();
    }

    /**
     * 调用大模型API
     */
    private String callLLMApi(String prompt) {
        // 如果没有配置API Key，返回模拟数据（开发测试用）
        if (!StringUtils.hasText(apiKey) || "your-api-key".equals(apiKey)) {
            log.warn("未配置AI API Key，返回模拟数据");
            return generateMockReport();
        }

        try {
            String url = buildUrl(baseUrl, chatEndpoint);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            requestBody.put("stream", false);
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是专业投资研究助手，输出中文、结构化、审慎并包含风险提示。");
            messages.add(systemMessage);
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            requestBody.put("messages", messages);

            httpPost.setEntity(new StringEntity(JSON.toJSONString(requestBody), StandardCharsets.UTF_8));

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getCode();
                String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JSONObject json = JSON.parseObject(result);

                if (json.containsKey("choices")) {
                    JSONObject message = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message");
                    String content = extractMessageContent(message);
                    if (StringUtils.hasText(content)) {
                        return content;
                    }
                    log.warn("LLM响应中 choices 存在但无法解析内容，status={}, body={}", statusCode, result);
                } else if (json.containsKey("error")) {
                    String msg = json.getJSONObject("error").getString("message");
                    log.error("API调用失败，status={}, message={}", statusCode, msg);
                    throw new RuntimeException("AI服务调用失败(" + statusCode + "): " + msg);
                } else {
                    log.error("API调用失败，status={}, body={}", statusCode, result);
                    throw new RuntimeException("AI服务调用失败(" + statusCode + "): 返回格式不支持");
                }
            }
        } catch (Exception e) {
            log.error("调用大模型API失败", e);
            throw new RuntimeException("生成报告失败: " + e.getMessage());
        }
        
        return generateMockReport();
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

    private String buildUrl(String rawBaseUrl, String rawEndpoint) {
        String b = StringUtils.hasText(rawBaseUrl) ? rawBaseUrl.trim() : "";
        String e = StringUtils.hasText(rawEndpoint) ? rawEndpoint.trim() : "/chat/completions";
        while (b.endsWith("/")) {
            b = b.substring(0, b.length() - 1);
        }
        if (!e.startsWith("/")) {
            e = "/" + e;
        }
        return b + e;
    }

    private String extractMessageContent(JSONObject message) {
        if (message == null) {
            return null;
        }
        Object contentObj = message.get("content");
        if (contentObj instanceof String str) {
            return str;
        }
        if (contentObj instanceof com.alibaba.fastjson2.JSONArray arr) {
            StringJoiner joiner = new StringJoiner("\n");
            for (int i = 0; i < arr.size(); i++) {
                Object item = arr.get(i);
                if (item instanceof JSONObject part) {
                    String text = part.getString("text");
                    if (!StringUtils.hasText(text)) {
                        Object innerText = part.get("content");
                        if (innerText instanceof String s && StringUtils.hasText(s)) {
                            text = s;
                        }
                    }
                    if (StringUtils.hasText(text)) {
                        joiner.add(text);
                    }
                }
            }
            String out = joiner.toString();
            return StringUtils.hasText(out) ? out : null;
        }
        return null;
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

    /**
     * 生成模拟报告（用于开发和测试）
     */
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
