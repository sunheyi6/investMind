package com.investmind.service;

import com.investmind.dto.ReportRequest;

import java.util.List;

/**
 * 大模型服务接口
 * 用于生成投资报告和获取文本向量
 */
public interface LLMService {

    /**
     * 生成投资报告
     */
    String generateReport(ReportRequest.GenerateRequest request);

    /**
     * 生成投资报告（指定用户）
     */
    String generateReport(ReportRequest.GenerateRequest request, Long userId);

    /**
     * 获取文本的向量表示 (用于存入向量数据库)
     */
    List<Float> getEmbedding(String text);

    /**
     * 根据历史优质内容生成增强提示词
     */
    String buildEnhancedPrompt(String basePrompt, List<String> historicalContent);
}
