package com.investmind.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户投资理念配置实体类
 * 存储用户的投资偏好和理念配置
 */
@Data
@TableName("user_investment_philosophy")
public class InvestmentPhilosophy {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 风险偏好: CONSERVATIVE/MODERATE/AGGRESSIVE
     */
    private String riskPreference;

    /**
     * 投资风格: VALUE/GROWTH/BLEND
     */
    private String investmentStyle;

    /**
     * 投资期限: SHORT/MEDIUM/LONG
     */
    private String investmentHorizon;

    /**
     * 偏好行业,逗号分隔
     */
    private String preferredSectors;

    /**
     * 回避行业,逗号分隔
     */
    private String avoidSectors;

    /**
     * 投资理念详细描述
     */
    private String philosophyDescription;

    /**
     * 策略备注
     */
    private String strategyNotes;

    /**
     * 风险管理方法
     */
    private String riskManagement;

    /**
     * 是否允许AI学习
     */
    private Integer aiLearningEnabled;

    /**
     * 学习迭代次数
     */
    private Integer learningIterations;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
