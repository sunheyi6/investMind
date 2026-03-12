package com.investmind.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报告响应 DTO
 */
public class ReportResponse {

    /**
     * 报告详情响应
     */
    @Data
    @Builder
    public static class Detail {
        private Long id;
        private LocalDate reportDate;
        private String reportType;
        private String reportTypeName;
        private String aiContent;
        private LocalDateTime aiGenerateTime;
        private String humanContent;
        private LocalDateTime humanCorrectTime;
        private String correctedBy;
        private Integer isVectorized;
        private LocalDateTime vectorizedTime;
        private Integer isUsedForLearning;
        private LocalDateTime learningTime;
        private BigDecimal qualityScore;
        private String tags;
        private String marketAnalysis;
        private String strategyAdvice;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;
    }

    /**
     * 报告列表项响应
     */
    @Data
    @Builder
    public static class ListItem {
        private Long id;
        private LocalDate reportDate;
        private String reportType;
        private String reportTypeName;
        private String summary;
        private Integer isCorrected;
        private Integer isVectorized;
        private Integer isUsedForLearning;
        private BigDecimal qualityScore;
        private LocalDateTime createdTime;
    }

    /**
     * AI生成结果响应
     */
    @Data
    @Builder
    public static class GenerateResult {
        private Long reportId;
        private String content;
        private LocalDate reportDate;
        private String reportType;
        private LocalDateTime generateTime;
        private Boolean isNew;
    }

    /**
     * 修正结果响应
     */
    @Data
    @Builder
    public static class CorrectResult {
        private Long reportId;
        private String message;
        private LocalDateTime correctTime;
    }
}
