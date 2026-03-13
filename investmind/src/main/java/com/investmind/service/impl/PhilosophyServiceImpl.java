package com.investmind.service.impl;

import com.investmind.dto.UserRequest;
import com.investmind.dto.UserResponse;
import com.investmind.model.InvestmentPhilosophy;
import com.investmind.repository.InvestmentPhilosophyMapper;
import com.investmind.service.PhilosophyService;
import com.investmind.service.LLMService;
import com.investmind.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 投资理念服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhilosophyServiceImpl implements PhilosophyService {

    private final InvestmentPhilosophyMapper philosophyMapper;
    private final LLMService llmService;

    @Override
    public UserResponse.PhilosophyResponse getCurrentUserPhilosophy() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        return getPhilosophyByUserId(userId);
    }

    @Override
    public UserResponse.PhilosophyResponse getPhilosophyByUserId(Long userId) {
        InvestmentPhilosophy philosophy = philosophyMapper.selectByUserId(userId);

        if (philosophy == null) {
            // 如果没有配置，创建默认配置
            initializePhilosophy(userId);
            philosophy = philosophyMapper.selectByUserId(userId);
        }

        return convertToResponse(philosophy);
    }

    @Override
    public UserResponse.PhilosophyResponse updateCurrentUserPhilosophy(UserRequest.UpdatePhilosophyRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        InvestmentPhilosophy philosophy = philosophyMapper.selectByUserId(userId);

        // 如果不存在，创建新的
        if (philosophy == null) {
            philosophy = new InvestmentPhilosophy();
            philosophy.setUserId(userId);
            philosophy.setAiLearningEnabled(1);
            philosophy.setLearningIterations(0);
            philosophy.setVersionNo(1);
        }

        // 更新字段
        if (request.getRiskPreference() != null) {
            philosophy.setRiskPreference(request.getRiskPreference());
        }
        if (request.getInvestmentStyle() != null) {
            philosophy.setInvestmentStyle(request.getInvestmentStyle());
        }
        if (request.getInvestmentHorizon() != null) {
            philosophy.setInvestmentHorizon(request.getInvestmentHorizon());
        }
        if (request.getPreferredSectors() != null) {
            philosophy.setPreferredSectors(request.getPreferredSectors());
        }
        if (request.getAvoidSectors() != null) {
            philosophy.setAvoidSectors(request.getAvoidSectors());
        }
        if (request.getPhilosophyDescription() != null) {
            philosophy.setPhilosophyDescription(request.getPhilosophyDescription());
        }
        if (request.getCoreInvestmentPhilosophy() != null) {
            philosophy.setCoreInvestmentPhilosophy(request.getCoreInvestmentPhilosophy());
        }
        if (request.getStockSelectionCriteria() != null) {
            philosophy.setStockSelectionCriteria(request.getStockSelectionCriteria());
        }
        if (request.getValuationLogic() != null) {
            philosophy.setValuationLogic(request.getValuationLogic());
        }
        if (request.getPositionManagementRules() != null) {
            philosophy.setPositionManagementRules(request.getPositionManagementRules());
        }
        if (request.getSellConditions() != null) {
            philosophy.setSellConditions(request.getSellConditions());
        }
        if (request.getHoldingPeriod() != null) {
            philosophy.setHoldingPeriod(request.getHoldingPeriod());
        }
        if (request.getIndustryRestrictions() != null) {
            philosophy.setIndustryRestrictions(request.getIndustryRestrictions());
        }
        if (request.getStrategyNotes() != null) {
            philosophy.setStrategyNotes(request.getStrategyNotes());
        }
        if (request.getRiskManagement() != null) {
            philosophy.setRiskManagement(request.getRiskManagement());
        }
        if (request.getAiLearningEnabled() != null) {
            philosophy.setAiLearningEnabled(request.getAiLearningEnabled());
        }
        philosophy.setVersionNo((philosophy.getVersionNo() == null ? 0 : philosophy.getVersionNo()) + 1);

        if (philosophy.getId() == null) {
            philosophyMapper.insert(philosophy);
        } else {
            philosophyMapper.updateById(philosophy);
        }

        log.info("用户 {} 更新投资理念配置", userId);

        return convertToResponse(philosophy);
    }

    @Override
    public void initializePhilosophy(Long userId) {
        if (philosophyMapper.countByUserId(userId) == 0) {
            InvestmentPhilosophy philosophy = new InvestmentPhilosophy();
            philosophy.setUserId(userId);
            philosophy.setAiLearningEnabled(1);
            philosophy.setLearningIterations(0);
            philosophy.setVersionNo(1);
            philosophyMapper.insert(philosophy);
            log.info("为用户 {} 初始化投资理念配置", userId);
        }
    }

    @Override
    public UserResponse.PhilosophyResponse ingestNaturalLanguage(String inputText) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        if (!StringUtils.hasText(inputText)) {
            throw new RuntimeException("输入内容不能为空");
        }

        InvestmentPhilosophy philosophy = philosophyMapper.selectByUserId(userId);
        if (philosophy == null) {
            initializePhilosophy(userId);
            philosophy = philosophyMapper.selectByUserId(userId);
        }

        String current = buildStructuredContext(philosophy);
        Map<String, String> parsed = llmService.extractPhilosophyFields(inputText, current);

        applyParsedFields(philosophy, parsed);
        philosophy.setLearningIterations((philosophy.getLearningIterations() == null ? 0 : philosophy.getLearningIterations()) + 1);
        philosophy.setVersionNo((philosophy.getVersionNo() == null ? 0 : philosophy.getVersionNo()) + 1);
        philosophyMapper.updateById(philosophy);

        return convertToResponse(philosophy);
    }

    @Override
    public UserResponse.PhilosophyDocumentResponse generateCurrentUserDocument() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        InvestmentPhilosophy philosophy = philosophyMapper.selectByUserId(userId);
        if (philosophy == null) {
            initializePhilosophy(userId);
            philosophy = philosophyMapper.selectByUserId(userId);
        }

        String context = buildStructuredContext(philosophy);
        String doc = llmService.generatePhilosophyDocument(context);

        return UserResponse.PhilosophyDocumentResponse.builder()
                .versionNo(philosophy.getVersionNo())
                .updatedTime(philosophy.getUpdatedTime())
                .documentMarkdown(doc)
                .build();
    }

    private void applyParsedFields(InvestmentPhilosophy p, Map<String, String> m) {
        if (m == null || m.isEmpty()) {
            return;
        }
        setIfPresent(m, "riskPreference", p::setRiskPreference);
        setIfPresent(m, "investmentStyle", p::setInvestmentStyle);
        setIfPresent(m, "investmentHorizon", p::setInvestmentHorizon);
        setIfPresent(m, "preferredSectors", p::setPreferredSectors);
        setIfPresent(m, "avoidSectors", p::setAvoidSectors);
        setIfPresent(m, "philosophyDescription", p::setPhilosophyDescription);
        setIfPresent(m, "strategyNotes", p::setStrategyNotes);
        setIfPresent(m, "riskManagement", p::setRiskManagement);
        setIfPresent(m, "coreInvestmentPhilosophy", p::setCoreInvestmentPhilosophy);
        setIfPresent(m, "stockSelectionCriteria", p::setStockSelectionCriteria);
        setIfPresent(m, "valuationLogic", p::setValuationLogic);
        setIfPresent(m, "positionManagementRules", p::setPositionManagementRules);
        setIfPresent(m, "sellConditions", p::setSellConditions);
        setIfPresent(m, "holdingPeriod", p::setHoldingPeriod);
        setIfPresent(m, "industryRestrictions", p::setIndustryRestrictions);
    }

    private void setIfPresent(Map<String, String> map, String key, java.util.function.Consumer<String> setter) {
        String val = map.get(key);
        if (StringUtils.hasText(val)) {
            setter.accept(val.trim());
        }
    }

    private String buildStructuredContext(InvestmentPhilosophy p) {
        StringBuilder sb = new StringBuilder();
        append(sb, "核心投资哲学", p.getCoreInvestmentPhilosophy());
        append(sb, "选股标准", p.getStockSelectionCriteria());
        append(sb, "估值逻辑", p.getValuationLogic());
        append(sb, "仓位管理规则", p.getPositionManagementRules());
        append(sb, "卖出条件", p.getSellConditions());
        append(sb, "风险偏好", p.getRiskPreference());
        append(sb, "持有周期", p.getHoldingPeriod());
        append(sb, "投资期限", p.getInvestmentHorizon());
        append(sb, "行业限制", p.getIndustryRestrictions());
        append(sb, "偏好行业", p.getPreferredSectors());
        append(sb, "回避行业", p.getAvoidSectors());
        append(sb, "风险管理", p.getRiskManagement());
        append(sb, "策略备注", p.getStrategyNotes());
        append(sb, "理念描述", p.getPhilosophyDescription());
        return sb.toString();
    }

    private void append(StringBuilder sb, String key, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append("\n");
        }
        sb.append(key).append("：").append(value);
    }

    /**
     * 转换为响应对象
     */
    private UserResponse.PhilosophyResponse convertToResponse(InvestmentPhilosophy philosophy) {
        return UserResponse.PhilosophyResponse.builder()
                .id(philosophy.getId())
                .userId(philosophy.getUserId())
                .riskPreference(philosophy.getRiskPreference())
                .investmentStyle(philosophy.getInvestmentStyle())
                .investmentHorizon(philosophy.getInvestmentHorizon())
                .preferredSectors(philosophy.getPreferredSectors())
                .avoidSectors(philosophy.getAvoidSectors())
                .philosophyDescription(philosophy.getPhilosophyDescription())
                .coreInvestmentPhilosophy(philosophy.getCoreInvestmentPhilosophy())
                .stockSelectionCriteria(philosophy.getStockSelectionCriteria())
                .valuationLogic(philosophy.getValuationLogic())
                .positionManagementRules(philosophy.getPositionManagementRules())
                .sellConditions(philosophy.getSellConditions())
                .holdingPeriod(philosophy.getHoldingPeriod())
                .industryRestrictions(philosophy.getIndustryRestrictions())
                .strategyNotes(philosophy.getStrategyNotes())
                .riskManagement(philosophy.getRiskManagement())
                .aiLearningEnabled(philosophy.getAiLearningEnabled())
                .learningIterations(philosophy.getLearningIterations())
                .versionNo(philosophy.getVersionNo())
                .updatedTime(philosophy.getUpdatedTime())
                .build();
    }
}
