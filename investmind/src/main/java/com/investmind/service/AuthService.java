package com.investmind.service;

import com.investmind.dto.AuthRequest;
import com.investmind.dto.AuthResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册结果
     */
    AuthResponse.LoginResponse register(AuthRequest.RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（包含token）
     */
    AuthResponse.LoginResponse login(AuthRequest.LoginRequest request);

    /**
     * 刷新访问令牌
     *
     * @param request 刷新请求
     * @return 新的访问令牌
     */
    AuthResponse.RefreshResponse refreshToken(AuthRequest.RefreshRequest request);

    /**
     * 用户登出
     *
     * @param token 访问令牌
     */
    void logout(String token);
}
