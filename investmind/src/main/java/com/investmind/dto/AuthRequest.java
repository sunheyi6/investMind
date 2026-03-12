package com.investmind.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 认证请求 DTO
 */
public class AuthRequest {

    /**
     * 注册请求
     */
    @Data
    public static class RegisterRequest {
        /**
         * 用户名
         */
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 64, message = "用户名长度必须在3-64之间")
        private String username;

        /**
         * 密码
         */
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 128, message = "密码长度必须在6-128之间")
        private String password;

        /**
         * 邮箱
         */
        @Email(message = "邮箱格式不正确")
        private String email;

        /**
         * 昵称
         */
        private String nickname;
    }

    /**
     * 登录请求
     */
    @Data
    public static class LoginRequest {
        /**
         * 用户名
         */
        @NotBlank(message = "用户名不能为空")
        private String username;

        /**
         * 密码
         */
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    /**
     * 刷新Token请求
     */
    @Data
    public static class RefreshRequest {
        /**
         * 刷新Token
         */
        @NotBlank(message = "刷新Token不能为空")
        private String refreshToken;
    }
}
