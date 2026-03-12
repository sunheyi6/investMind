package com.investmind.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.investmind.dto.ReportRequest;
import com.investmind.dto.ReportResponse;
import com.investmind.model.InvestReport;

import java.time.LocalDate;
import java.util.List;

/**
 * 投资报告 Service 接口
 */
public interface InvestReportService extends IService<InvestReport> {

    /**
     * AI 生成报告
     */
    ReportResponse.GenerateResult generateReport(ReportRequest.GenerateRequest request);

    /**
     * 人工修正报告
     */
    ReportResponse.CorrectResult correctReport(ReportRequest.CorrectRequest request);

    /**
     * 获取报告详情
     */
    ReportResponse.Detail getReportDetail(Long id);

    /**
     * 分页查询报告列表
     */
    IPage<ReportResponse.ListItem> listReports(ReportRequest.QueryRequest request);

    /**
     * 获取指定日期的报告
     */
    ReportResponse.Detail getReportByDate(LocalDate date);

    /**
     * 获取需要向量化的报告
     */
    List<InvestReport> getReportsNeedVectorize(Long userId);

    /**
     * 标记报告已向量化
     */
    void markAsVectorized(Long reportId, Long userId);

    /**
     * 获取可用于学习的报告
     */
    List<InvestReport> getReportsForLearning(Long userId);

    /**
     * 标记报告已用于学习
     */
    void markAsUsedForLearning(Long reportId, Long userId);
}
