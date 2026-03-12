package com.investmind.controller;

import com.investmind.dto.ApiResponse;
import com.investmind.model.InvestReport;
import com.investmind.service.InvestReportService;
import com.investmind.service.VectorService;
import com.investmind.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 向量管理控制器
 */
@RestController
@RequestMapping("/vectors")
@RequiredArgsConstructor
public class VectorController {

    private final VectorService vectorService;
    private final InvestReportService investReportService;

    /**
     * 检查向量数据库连接
     */
    @GetMapping("/health")
    public ApiResponse<Boolean> checkHealth() {
        boolean healthy = vectorService.checkConnection();
        return ApiResponse.success(healthy ? "连接正常" : "连接失败", healthy);
    }

    /**
     * 获取需要向量化的报告
     */
    @GetMapping("/pending")
    public ApiResponse<List<InvestReport>> getPendingReports() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<InvestReport> reports = investReportService.getReportsNeedVectorize(userId);
        return ApiResponse.success("查询成功，共 " + reports.size() + " 条待向量化报告", reports);
    }

    /**
     * 向量化指定报告
     */
    @PostMapping("/report/{reportId}")
    public ApiResponse<Boolean> vectorizeReport(@PathVariable Long reportId) {
        Long userId = SecurityUtils.getCurrentUserId();
        InvestReport report = investReportService.getById(reportId);
        if (report == null) {
            return ApiResponse.error(404, "报告不存在");
        }

        // 检查是否是自己的报告
        if (!userId.equals(report.getUserId())) {
            return ApiResponse.error(403, "无权访问此报告");
        }

        boolean success = vectorService.vectorizeReport(report);
        if (success) {
            return ApiResponse.success("向量化成功", true);
        } else {
            return ApiResponse.error("向量化失败");
        }
    }

    /**
     * 批量向量化所有待处理报告
     */
    @PostMapping("/batch")
    public ApiResponse<Integer> batchVectorize() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<InvestReport> reports = investReportService.getReportsNeedVectorize(userId);
        if (reports.isEmpty()) {
            return ApiResponse.success("没有待向量化的报告", 0);
        }

        int count = vectorService.batchVectorizeReports(reports);
        return ApiResponse.success("批量向量化完成，成功 " + count + " 条", count);
    }

    /**
     * 检索相似内容
     */
    @GetMapping("/search")
    public ApiResponse<List<String>> searchSimilar(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        List<String> results = vectorService.searchSimilarContent(query, topK);
        return ApiResponse.success("检索成功，找到 " + results.size() + " 条相似内容", results);
    }
}
