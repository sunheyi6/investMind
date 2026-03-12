package com.investmind.controller;

import com.investmind.dto.ApiResponse;
import com.investmind.dto.AuthRequest;
import com.investmind.dto.AuthResponse;
import com.investmind.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户注册、登录、登出等认证相关请求
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse.LoginResponse> register(
            @Valid @RequestBody AuthRequest.RegisterRequest request) {
        AuthResponse.LoginResponse response = authService.register(request);
        return ApiResponse.success("注册成功", response);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse.LoginResponse> login(
            @Valid @RequestBody AuthRequest.LoginRequest request) {
        AuthResponse.LoginResponse response = authService.login(request);
        return ApiResponse.success("登录成功", response);
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse.RefreshResponse> refreshToken(
            @Valid @RequestBody AuthRequest.RefreshRequest request) {
        AuthResponse.RefreshResponse response = authService.refreshToken(request);
        return ApiResponse.success("刷新成功", response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        authService.logout(token);
        return ApiResponse.success("登出成功", null);
    }

    /**
     * 从请求中提取token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
