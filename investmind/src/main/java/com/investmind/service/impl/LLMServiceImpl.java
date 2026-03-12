package com.investmind.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.investmind.dto.ReportRequest;
import com.investmind.service.LLMService;
import com.investmind.service.VectorService;
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

    @Lazy
    @Autowired
    private VectorService vectorService;

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
            String url = baseUrl + "/embeddings";
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("input", text);
            requestBody.put("model", "text-embedding-ada-002"); // 或其他 embedding 模型

            httpPost.setEntity(new StringEntity(JSON.toJSONString(requestBody), StandardCharsets.UTF_8));

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(httpPost)) {
                
                String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JSONObject json = JSON.parseObject(result);
                
                if (json.containsKey("data")) {
                    JSONObject data = json.getJSONArray("data").getJSONObject(0);
                    List<Float> embedding = new ArrayList<>();
                    for (Object val : data.getJSONArray("embedding")) {
                        embedding.add(((Number) val).floatValue());
                    }
                    return embedding;
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
            String url = baseUrl + "/chat/completions";
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);

            httpPost.setEntity(new StringEntity(JSON.toJSONString(requestBody), StandardCharsets.UTF_8));

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(httpPost)) {
                
                String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JSONObject json = JSON.parseObject(result);
                
                if (json.containsKey("choices")) {
                    return json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                } else if (json.containsKey("error")) {
                    log.error("API调用失败: {}", json.getJSONObject("error").getString("message"));
                    throw new RuntimeException("AI服务调用失败: " + json.getJSONObject("error").getString("message"));
                }
            }
        } catch (Exception e) {
            log.error("调用大模型API失败", e);
            throw new RuntimeException("生成报告失败: " + e.getMessage());
        }
        
        return generateMockReport();
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
