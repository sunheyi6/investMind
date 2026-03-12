package com.investmind.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.investmind.dto.ApiResponse;
import com.investmind.dto.ReportRequest;
import com.investmind.dto.ReportResponse;
import com.investmind.service.InvestReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 投资报告控制器
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class InvestReportController {

    private final InvestReportService investReportService;

    /**
     * AI 生成报告
     */
    @PostMapping("/generate")
    public ApiResponse<ReportResponse.GenerateResult> generateReport(
            @Valid @RequestBody ReportRequest.GenerateRequest request) {
        
        // 如果未指定日期，默认今天
        if (request.getReportDate() == null) {
            request.setReportDate(LocalDate.now());
        }
        
        ReportResponse.GenerateResult result = investReportService.generateReport(request);
        String message = Boolean.TRUE.equals(result.getIsNew()) ? "报告生成成功" : "报告已存在";
        return ApiResponse.success(message, result);
    }

    /**
     * 人工修正报告
     */
    @PostMapping("/correct")
    public ApiResponse<ReportResponse.CorrectResult> correctReport(
            @Valid @RequestBody ReportRequest.CorrectRequest request) {
        ReportResponse.CorrectResult result = investReportService.correctReport(request);
        return ApiResponse.success("修正成功", result);
    }

    /**
     * 获取报告详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ReportResponse.Detail> getReportDetail(@PathVariable Long id) {
        ReportResponse.Detail detail = investReportService.getReportDetail(id);
        if (detail == null) {
            return ApiResponse.error(404, "报告不存在");
        }
        return ApiResponse.success(detail);
    }

    /**
     * 根据日期获取报告
     */
    @GetMapping("/date/{date}")
    public ApiResponse<ReportResponse.Detail> getReportByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        ReportResponse.Detail detail = investReportService.getReportByDate(date);
        if (detail == null) {
            return ApiResponse.error(404, "该日期暂无报告");
        }
        return ApiResponse.success(detail);
    }

    /**
     * 分页查询报告列表
     */
    @PostMapping("/list")
    public ApiResponse<IPage<ReportResponse.ListItem>> listReports(
            @RequestBody ReportRequest.QueryRequest request) {
        IPage<ReportResponse.ListItem> page = investReportService.listReports(request);
        return ApiResponse.success(page);
    }

    /**
     * 获取今日报告（快捷接口）
     */
    @GetMapping("/today")
    public ApiResponse<ReportResponse.Detail> getTodayReport() {
        return getReportByDate(LocalDate.now());
    }

    /**
     * 生成今日报告（快捷接口）
     */
    @PostMapping("/today/generate")
    public ApiResponse<ReportResponse.GenerateResult> generateTodayReport(
            @RequestParam(required = false) Boolean useHistoricalContent) {
        ReportRequest.GenerateRequest request = new ReportRequest.GenerateRequest();
        request.setReportDate(LocalDate.now());
        request.setUseHistoricalContent(useHistoricalContent != null ? useHistoricalContent : true);
        return generateReport(request);
    }
}
