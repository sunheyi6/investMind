package com.investmind.service.impl;

import com.investmind.dto.UserRequest;
import com.investmind.dto.UserResponse;
import com.investmind.model.InvestmentPhilosophy;
import com.investmind.repository.InvestmentPhilosophyMapper;
import com.investmind.service.PhilosophyService;
import com.investmind.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 投资理念服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhilosophyServiceImpl implements PhilosophyService {

    private final InvestmentPhilosophyMapper philosophyMapper;

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
        if (request.getStrategyNotes() != null) {
            philosophy.setStrategyNotes(request.getStrategyNotes());
        }
        if (request.getRiskManagement() != null) {
            philosophy.setRiskManagement(request.getRiskManagement());
        }
        if (request.getAiLearningEnabled() != null) {
            philosophy.setAiLearningEnabled(request.getAiLearningEnabled());
        }

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
            philosophyMapper.insert(philosophy);
            log.info("为用户 {} 初始化投资理念配置", userId);
        }
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
                .strategyNotes(philosophy.getStrategyNotes())
                .riskManagement(philosophy.getRiskManagement())
                .aiLearningEnabled(philosophy.getAiLearningEnabled())
                .learningIterations(philosophy.getLearningIterations())
                .updatedTime(philosophy.getUpdatedTime())
                .build();
    }
}
