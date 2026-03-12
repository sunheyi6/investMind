package com.investmind.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户响应 DTO
 */
public class UserResponse {

    /**
     * 用户资料响应
     */
    @Data
    @Builder
    public static class ProfileResponse {
        /**
         * 用户ID
         */
        private Long id;

        /**
         * 用户名
         */
        private String username;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 头像URL
         */
        private String avatar;

        /**
         * 用户类型
         */
        private String userType;

        /**
         * 状态
         */
        private Integer status;

        /**
         * 最后登录时间
         */
        private LocalDateTime lastLoginTime;

        /**
         * 创建时间
         */
        private LocalDateTime createdTime;
    }

    /**
     * 投资理念配置响应
     */
    @Data
    @Builder
    public static class PhilosophyResponse {
        /**
         * 配置ID
         */
        private Long id;

        /**
         * 用户ID
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
         * 偏好行业
         */
        private String preferredSectors;

        /**
         * 回避行业
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
         * 更新时间
         */
        private LocalDateTime updatedTime;
    }
}
