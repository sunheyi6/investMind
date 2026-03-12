package com.investmind.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 投资报告实体类
 * 存储AI生成的原始报告和人工修正版本
 */
@Data
@TableName("invest_report")
public class InvestReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID,0为系统公共报告
     */
    private Long userId;

    /**
     * 报告日期
     */
    private LocalDate reportDate;

    /**
     * 报告类型: DAILY-日报, WEEKLY-周报, MONTHLY-月报
     */
    private String reportType;

    /**
     * AI生成的原始报告内容(Markdown格式)
     */
    private String aiContent;

    /**
     * AI生成时间
     */
    private LocalDateTime aiGenerateTime;

    /**
     * 人工修正后的报告内容(Markdown格式)
     */
    private String humanContent;

    /**
     * 人工修正时间
     */
    private LocalDateTime humanCorrectTime;

    /**
     * 修正人
     */
    private String correctedBy;

    /**
     * 是否已向量化: 0-否, 1-是
     */
    private Integer isVectorized;

    /**
     * 向量化时间
     */
    private LocalDateTime vectorizedTime;

    /**
     * 是否已用于模型学习: 0-否, 1-是
     */
    private Integer isUsedForLearning;

    /**
     * 用于学习的时间
     */
    private LocalDateTime learningTime;

    /**
     * 报告质量评分(0.00-5.00)
     */
    private BigDecimal qualityScore;

    /**
     * 标签,逗号分隔
     */
    private String tags;

    /**
     * 市场分析摘要
     */
    private String marketAnalysis;

    /**
     * 策略建议摘要
     */
    private String strategyAdvice;

    /**
     * 是否删除: 0-否, 1-是
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
