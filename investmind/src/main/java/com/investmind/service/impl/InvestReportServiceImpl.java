package com.investmind.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.investmind.dto.ReportRequest;
import com.investmind.dto.ReportResponse;
import com.investmind.model.InvestReport;
import com.investmind.model.ReportCorrectionLog;
import com.investmind.repository.InvestReportMapper;
import com.investmind.repository.ReportCorrectionLogMapper;
import com.investmind.service.InvestReportService;
import com.investmind.service.LLMService;
import com.investmind.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 投资报告 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvestReportServiceImpl extends ServiceImpl<InvestReportMapper, InvestReport> implements InvestReportService {

    private final InvestReportMapper investReportMapper;
    private final ReportCorrectionLogMapper correctionLogMapper;
    private final LLMService llmService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportResponse.GenerateResult generateReport(ReportRequest.GenerateRequest request) {
        // 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 检查是否已存在该日期的报告
        InvestReport existingReport = investReportMapper.selectByUserIdAndDateAndType(userId, request.getReportDate(), request.getReportType());

        if (existingReport != null && StringUtils.hasText(existingReport.getAiContent())) {
            // 已存在报告，直接返回
            log.info("报告已存在，用户ID: {}, 日期: {}, 类型: {}", userId, request.getReportDate(), request.getReportType());
            return ReportResponse.GenerateResult.builder()
                    .reportId(existingReport.getId())
                    .content(existingReport.getAiContent())
                    .reportDate(existingReport.getReportDate())
                    .reportType(existingReport.getReportType())
                    .generateTime(existingReport.getAiGenerateTime())
                    .isNew(false)
                    .build();
        }

        // 调用大模型生成报告
        String content = llmService.generateReport(request, userId);

        // 保存或更新报告
        InvestReport report;
        if (existingReport != null) {
            report = existingReport;
        } else {
            report = new InvestReport();
            report.setUserId(userId);
            report.setReportDate(request.getReportDate());
            report.setReportType(request.getReportType());
        }

        report.setAiContent(content);
        report.setAiGenerateTime(LocalDateTime.now());
        report.setIsVectorized(0);
        report.setIsUsedForLearning(0);

        this.saveOrUpdate(report);

        log.info("报告生成成功，ID: {}, 用户ID: {}, 日期: {}", report.getId(), userId, request.getReportDate());

        return ReportResponse.GenerateResult.builder()
                .reportId(report.getId())
                .content(content)
                .reportDate(report.getReportDate())
                .reportType(report.getReportType())
                .generateTime(report.getAiGenerateTime())
                .isNew(true)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportResponse.CorrectResult correctReport(ReportRequest.CorrectRequest request) {
        // 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        InvestReport report = this.getById(request.getReportId());
        if (report == null) {
            throw new RuntimeException("报告不存在: " + request.getReportId());
        }

        // 检查是否是自己的报告
        if (!userId.equals(report.getUserId())) {
            throw new RuntimeException("无权修改此报告");
        }

        String oldContent = report.getHumanContent();
        String newContent = request.getHumanContent();

        // 获取当前用户名
        String currentUsername = SecurityUtils.getCurrentUsername();

        // 记录修正日志
        ReportCorrectionLog logEntry = new ReportCorrectionLog();
        logEntry.setReportId(request.getReportId());
        logEntry.setFieldName("human_content");
        logEntry.setOldValue(oldContent);
        logEntry.setNewValue(newContent);
        logEntry.setCorrectedBy(currentUsername);
        logEntry.setCorrectionReason(request.getCorrectionReason());
        logEntry.setCorrectTime(LocalDateTime.now());
        correctionLogMapper.insert(logEntry);

        // 更新报告
        report.setHumanContent(newContent);
        report.setHumanCorrectTime(LocalDateTime.now());
        report.setCorrectedBy(currentUsername);
        report.setQualityScore(request.getQualityScore());
        report.setTags(request.getTags());
        // 修正后重置向量化状态
        report.setIsVectorized(0);
        report.setVectorizedTime(null);

        this.updateById(report);

        log.info("报告修正成功，ID: {}, 用户ID: {}, 修正人: {}", request.getReportId(), userId, currentUsername);

        return ReportResponse.CorrectResult.builder()
                .reportId(request.getReportId())
                .message("修正成功")
                .correctTime(LocalDateTime.now())
                .build();
    }

    @Override
    public ReportResponse.Detail getReportDetail(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        InvestReport report = this.getById(id);
        if (report == null) {
            return null;
        }

        // 检查是否是自己的报告
        if (!userId.equals(report.getUserId())) {
            throw new RuntimeException("无权查看此报告");
        }

        return convertToDetail(report);
    }

    @Override
    public IPage<ReportResponse.ListItem> listReports(ReportRequest.QueryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Page<InvestReport> page = new Page<>(request.getPageNum(), request.getPageSize());

        IPage<InvestReport> reportPage = investReportMapper.selectReportPage(
                page,
                userId,
                request.getReportType(),
                request.getStartDate(),
                request.getEndDate()
        );

        return reportPage.convert(this::convertToListItem);
    }

    @Override
    public ReportResponse.Detail getReportByDate(LocalDate date) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        InvestReport report = investReportMapper.selectByDate(userId, date);
        if (report == null) {
            return null;
        }
        return convertToDetail(report);
    }

    @Override
    public List<InvestReport> getReportsNeedVectorize(Long userId) {
        return investReportMapper.selectNeedVectorizeReports(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsVectorized(Long reportId, Long userId) {
        investReportMapper.updateVectorizedStatus(reportId, userId);
    }

    @Override
    public List<InvestReport> getReportsForLearning(Long userId) {
        return investReportMapper.selectReportsForLearning(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsUsedForLearning(Long reportId, Long userId) {
        investReportMapper.updateLearningStatus(reportId, userId);
    }

    /**
     * 转换为详情响应
     */
    private ReportResponse.Detail convertToDetail(InvestReport report) {
        return ReportResponse.Detail.builder()
                .id(report.getId())
                .reportDate(report.getReportDate())
                .reportType(report.getReportType())
                .reportTypeName(getReportTypeName(report.getReportType()))
                .aiContent(report.getAiContent())
                .aiGenerateTime(report.getAiGenerateTime())
                .humanContent(report.getHumanContent())
                .humanCorrectTime(report.getHumanCorrectTime())
                .correctedBy(report.getCorrectedBy())
                .isVectorized(report.getIsVectorized())
                .vectorizedTime(report.getVectorizedTime())
                .isUsedForLearning(report.getIsUsedForLearning())
                .learningTime(report.getLearningTime())
                .qualityScore(report.getQualityScore())
                .tags(report.getTags())
                .marketAnalysis(report.getMarketAnalysis())
                .strategyAdvice(report.getStrategyAdvice())
                .createdTime(report.getCreatedTime())
                .updatedTime(report.getUpdatedTime())
                .build();
    }

    /**
     * 转换为列表项响应
     */
    private ReportResponse.ListItem convertToListItem(InvestReport report) {
        String content = report.getHumanContent() != null ? report.getHumanContent() : report.getAiContent();
        String summary = content != null && content.length() > 100 
                ? content.substring(0, 100) + "..." 
                : content;

        return ReportResponse.ListItem.builder()
                .id(report.getId())
                .reportDate(report.getReportDate())
                .reportType(report.getReportType())
                .reportTypeName(getReportTypeName(report.getReportType()))
                .summary(summary)
                .isCorrected(report.getHumanContent() != null ? 1 : 0)
                .isVectorized(report.getIsVectorized())
                .isUsedForLearning(report.getIsUsedForLearning())
                .qualityScore(report.getQualityScore())
                .createdTime(report.getCreatedTime())
                .build();
    }

    /**
     * 获取报告类型名称
     */
    private String getReportTypeName(String reportType) {
        return switch (reportType) {
            case "DAILY" -> "日报";
            case "WEEKLY" -> "周报";
            case "MONTHLY" -> "月报";
            default -> "未知";
        };
    }
}
