package com.investmind.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户请求 DTO
 */
public class UserRequest {

    /**
     * 更新用户资料请求
     */
    @Data
    public static class UpdateProfileRequest {
        /**
         * 昵称
         */
        @Size(max = 64, message = "昵称长度不能超过64")
        private String nickname;

        /**
         * 邮箱
         */
        @Email(message = "邮箱格式不正确")
        private String email;

        /**
         * 手机号
         */
        @Size(max = 20, message = "手机号长度不能超过20")
        private String phone;

        /**
         * 头像URL
         */
        @Size(max = 256, message = "头像URL长度不能超过256")
        private String avatar;
    }

    /**
     * 修改密码请求
     */
    @Data
    public static class ChangePasswordRequest {
        /**
         * 旧密码
         */
        @Size(min = 6, max = 128, message = "密码长度必须在6-128之间")
        private String oldPassword;

        /**
         * 新密码
         */
        @Size(min = 6, max = 128, message = "密码长度必须在6-128之间")
        private String newPassword;
    }

    /**
     * 更新投资理念配置请求
     */
    @Data
    public static class UpdatePhilosophyRequest {
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
    }
}
