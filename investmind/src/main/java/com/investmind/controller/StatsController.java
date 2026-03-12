package com.investmind.controller;

import com.investmind.dto.ApiResponse;
import com.investmind.model.InvestReport;
import com.investmind.service.InvestReportService;
import com.investmind.util.SecurityUtils;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final InvestReportService investReportService;

    /**
     * 获取统计数据概览
     */
    @GetMapping("/overview")
    public ApiResponse<StatsOverview> getOverview() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录");
        }

        // 这里可以添加更多统计逻辑
        StatsOverview overview = StatsOverview.builder()
                .totalReports(investReportService.lambdaQuery()
                        .eq(InvestReport::getUserId, userId)
                        .count())
                .correctedReports(investReportService.lambdaQuery()
                        .eq(InvestReport::getUserId, userId)
                        .isNotNull(InvestReport::getHumanContent)
                        .count())
                .vectorizedReports(investReportService.lambdaQuery()
                        .eq(InvestReport::getUserId, userId)
                        .eq(InvestReport::getIsVectorized, 1)
                        .count())
                .learningReports(investReportService.lambdaQuery()
                        .eq(InvestReport::getUserId, userId)
                        .eq(InvestReport::getIsUsedForLearning, 1)
                        .count())
                .build();

        return ApiResponse.success(overview);
    }

    /**
     * 获取模型学习进度
     */
    @GetMapping("/learning-progress")
    public ApiResponse<Map<String, Object>> getLearningProgress() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录");
        }

        Map<String, Object> progress = new HashMap<>();

        long totalCorrected = investReportService.lambdaQuery()
                .eq(InvestReport::getUserId, userId)
                .isNotNull(InvestReport::getHumanContent)
                .count();

        long vectorized = investReportService.lambdaQuery()
                .eq(InvestReport::getUserId, userId)
                .eq(InvestReport::getIsVectorized, 1)
                .count();

        long learned = investReportService.lambdaQuery()
                .eq(InvestReport::getUserId, userId)
                .eq(InvestReport::getIsUsedForLearning, 1)
                .count();

        progress.put("totalCorrected", totalCorrected);
        progress.put("vectorized", vectorized);
        progress.put("learned", learned);
        progress.put("vectorizeProgress", totalCorrected > 0 ?
                Math.round((double) vectorized / totalCorrected * 100) : 0);
        progress.put("learnProgress", totalCorrected > 0 ?
                Math.round((double) learned / totalCorrected * 100) : 0);

        return ApiResponse.success(progress);
    }

    @Data
    @Builder
    public static class StatsOverview {
        private Long totalReports;
        private Long correctedReports;
        private Long vectorizedReports;
        private Long learningReports;
    }
}
