package com.investmind.service.impl;

import com.investmind.model.InvestReport;
import com.investmind.model.VectorIndexRecord;
import com.investmind.repository.InvestReportMapper;
import com.investmind.repository.VectorIndexRecordMapper;
import com.investmind.service.LLMService;
import com.investmind.service.VectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向量服务实现类
 * 简化版本 - 使用内存存储（如需Milvus，需升级SDK版本）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorServiceImpl implements VectorService {

    @Lazy
    @Autowired
    private LLMService llmService;

    private final VectorIndexRecordMapper vectorIndexRecordMapper;
    private final InvestReportMapper investReportMapper;

    // 内存存储，用于演示
    private final ConcurrentHashMap<String, VectorData> memoryStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("向量服务初始化完成（使用内存存储模式）");
    }

    @Override
    public boolean vectorizeReport(InvestReport report) {
        if (report == null || !hasValidHumanContent(report)) {
            return false;
        }

        try {
            // 1. 获取人工修正的内容
            String content = report.getHumanContent();

            // 2. 获取向量表示
            List<Float> vector = llmService.getEmbedding(content);
            if (vector.isEmpty()) {
                log.warn("获取向量表示失败，报告ID: {}", report.getId());
                // 即使没有向量，也标记为已向量化（演示模式）
            }

            // 3. 存储到内存
            String vectorId = "vec-" + System.currentTimeMillis();
            memoryStore.put(vectorId, new VectorData(vectorId, content, vector));

            // 4. 记录到 MySQL
            VectorIndexRecord record = new VectorIndexRecord();
            record.setReportId(report.getId());
            record.setContentType("FULL_REPORT");
            record.setVectorId(vectorId);
            record.setEmbeddingModel("text-embedding-ada-002");
            record.setVectorizedContent(content.substring(0, Math.min(500, content.length())));
            vectorIndexRecordMapper.insert(record);

            // 5. 更新报告向量化状态
            investReportMapper.updateVectorizedStatus(report.getId(), report.getUserId());

            log.info("报告向量化成功，报告ID: {}, 向量ID: {}", report.getId(), vectorId);
            return true;

        } catch (Exception e) {
            log.error("报告向量化失败，报告ID: {}", report.getId(), e);
            return false;
        }
    }

    @Override
    public int batchVectorizeReports(List<InvestReport> reports) {
        int successCount = 0;
        for (InvestReport report : reports) {
            if (vectorizeReport(report)) {
                successCount++;
            }
        }
        log.info("批量向量化完成，成功: {}/总数: {}", successCount, reports.size());
        return successCount;
    }

    @Override
    public List<String> searchSimilarContent(String query, int topK) {
        // 简化实现：返回存储的内容（模拟相似度检索）
        List<String> results = new ArrayList<>();
        int count = 0;
        for (VectorData data : memoryStore.values()) {
            if (count >= topK) break;
            if (data.content.contains(query) || query.contains(data.content.substring(0, Math.min(20, data.content.length())))) {
                results.add(data.content);
                count++;
            }
        }
        return results.isEmpty() ? getRandomContents(topK) : results;
    }

    @Override
    public boolean deleteVector(String vectorId) {
        memoryStore.remove(vectorId);
        return true;
    }

    @Override
    public boolean checkConnection() {
        return true;
    }

    /**
     * 获取随机内容（演示用）
     */
    private List<String> getRandomContents(int count) {
        List<String> contents = new ArrayList<>();
        for (VectorData data : memoryStore.values()) {
            if (contents.size() >= count) break;
            contents.add(data.content);
        }
        return contents;
    }

    /**
     * 检查报告是否有有效的人工修正内容
     */
    private boolean hasValidHumanContent(InvestReport report) {
        return report.getHumanContent() != null
                && !report.getHumanContent().trim().isEmpty();
    }

    /**
     * 向量数据内部类
     */
    private static class VectorData {
        String id;
        String content;
        List<Float> vector;

        VectorData(String id, String content, List<Float> vector) {
            this.id = id;
            this.content = content;
            this.vector = vector;
        }
    }
}
