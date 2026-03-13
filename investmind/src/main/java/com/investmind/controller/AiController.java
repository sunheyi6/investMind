package com.investmind.controller;

import com.investmind.dto.AiChatRequest;
import com.investmind.dto.AiChatResponse;
import com.investmind.dto.ApiResponse;
import com.investmind.dto.UserResponse;
import com.investmind.service.LLMService;
import com.investmind.service.PhilosophyService;
import com.investmind.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final LLMService llmService;
    private final PhilosophyService philosophyService;

    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录");
        }

        UserResponse.PhilosophyResponse philosophy = philosophyService.getPhilosophyByUserId(userId);
        String context = buildInvestmentContext(philosophy);
        String answer = llmService.answerQuestion(request.getQuestion(), context);

        return ApiResponse.success(AiChatResponse.builder()
                .question(request.getQuestion())
                .answer(answer)
                .investmentContext(context)
                .build());
    }

    private String buildInvestmentContext(UserResponse.PhilosophyResponse p) {
        if (p == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        append(sb, "风险偏好", p.getRiskPreference());
        append(sb, "核心投资哲学", p.getCoreInvestmentPhilosophy());
        append(sb, "选股标准", p.getStockSelectionCriteria());
        append(sb, "估值逻辑", p.getValuationLogic());
        append(sb, "仓位管理规则", p.getPositionManagementRules());
        append(sb, "卖出条件", p.getSellConditions());
        append(sb, "持有周期", p.getHoldingPeriod());
        append(sb, "行业限制", p.getIndustryRestrictions());
        append(sb, "投资风格", p.getInvestmentStyle());
        append(sb, "投资期限", p.getInvestmentHorizon());
        append(sb, "偏好行业", p.getPreferredSectors());
        append(sb, "回避行业", p.getAvoidSectors());
        append(sb, "理念描述", p.getPhilosophyDescription());
        append(sb, "策略备注", p.getStrategyNotes());
        append(sb, "风险管理", p.getRiskManagement());
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
}
