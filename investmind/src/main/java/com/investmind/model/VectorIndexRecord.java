package com.investmind.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 向量索引记录实体类
 * 记录哪些内容已存入向量数据库
 */
@Data
@TableName("vector_index_record")
public class VectorIndexRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的报告ID
     */
    private Long reportId;

    /**
     * 内容类型: FULL_REPORT-完整报告, MARKET_ANALYSIS-市场分析, STRATEGY-策略建议
     */
    private String contentType;

    /**
     * 向量数据库中的ID
     */
    private String vectorId;

    /**
     * 使用的embedding模型
     */
    private String embeddingModel;

    /**
     * 被向量化的内容
     */
    private String vectorizedContent;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
