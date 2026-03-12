package com.investmind.service;

import com.investmind.model.InvestReport;

import java.util.List;

/**
 * 向量服务接口
 * 处理向量数据库操作
 */
public interface VectorService {

    /**
     * 将报告内容向量化并存储
     */
    boolean vectorizeReport(InvestReport report);

    /**
     * 批量向量化报告
     */
    int batchVectorizeReports(List<InvestReport> reports);

    /**
     * 检索相似内容
     */
    List<String> searchSimilarContent(String query, int topK);

    /**
     * 删除向量
     */
    boolean deleteVector(String vectorId);

    /**
     * 检查向量数据库连接
     */
    boolean checkConnection();
}
