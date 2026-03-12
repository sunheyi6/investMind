package com.investmind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 报告请求 DTO
 */
public class ReportRequest {

    /**
     * 生成报告请求
     */
    @Data
    public static class GenerateRequest {
        /**
         * 报告日期
         */
        @NotNull(message = "报告日期不能为空")
        private LocalDate reportDate;

        /**
         * 报告类型
         */
        private String reportType = "DAILY";

        /**
         * 自定义提示词（可选）
         */
        private String customPrompt;

        /**
         * 是否使用历史优质内容增强
         */
        private Boolean useHistoricalContent = true;
    }

    /**
     * 修正报告请求
     */
    @Data
    public static class CorrectRequest {
        /**
         * 报告ID
         */
        @NotNull(message = "报告ID不能为空")
        private Long reportId;

        /**
         * 修正后的内容
         */
        @NotBlank(message = "修正内容不能为空")
        private String humanContent;

        /**
         * 修正人
         */
        private String correctedBy;

        /**
         * 修正原因
         */
        private String correctionReason;

        /**
         * 质量评分
         */
        private BigDecimal qualityScore;

        /**
         * 标签
         */
        private String tags;
    }

    /**
     * 查询报告请求
     */
    @Data
    public static class QueryRequest {
        /**
         * 报告类型
         */
        private String reportType;

        /**
         * 开始日期
         */
        private LocalDate startDate;

        /**
         * 结束日期
         */
        private LocalDate endDate;

        /**
         * 页码
         */
        private Integer pageNum = 1;

        /**
         * 每页大小
         */
        private Integer pageSize = 10;
    }
}
