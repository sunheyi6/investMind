package com.investmind.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 认证响应 DTO
 */
public class AuthResponse {

    /**
     * 登录响应
     */
    @Data
    @Builder
    public static class LoginResponse {
        /**
         * 访问Token
         */
        private String accessToken;

        /**
         * 刷新Token
         */
        private String refreshToken;

        /**
         * Token类型
         */
        private String tokenType;

        /**
         * 访问Token过期时间（秒）
         */
        private Long expiresIn;

        /**
         * 用户信息
         */
        private UserInfo user;
    }

    /**
     * 用户信息
     */
    @Data
    @Builder
    public static class UserInfo {
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
         * 头像URL
         */
        private String avatar;

        /**
         * 用户类型
         */
        private String userType;

        /**
         * 最后登录时间
         */
        private LocalDateTime lastLoginTime;
    }

    /**
     * 刷新Token响应
     */
    @Data
    @Builder
    public static class RefreshResponse {
        /**
         * 新的访问Token
         */
        private String accessToken;

        /**
         * Token类型
         */
        private String tokenType;

        /**
         * 访问Token过期时间（秒）
         */
        private Long expiresIn;
    }
}
