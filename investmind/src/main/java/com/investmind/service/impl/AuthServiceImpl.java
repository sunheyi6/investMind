package com.investmind.service.impl;

import com.investmind.dto.AuthRequest;
import com.investmind.dto.AuthResponse;
import com.investmind.model.InvestmentPhilosophy;
import com.investmind.model.User;
import com.investmind.repository.InvestmentPhilosophyMapper;
import com.investmind.repository.UserMapper;
import com.investmind.service.AuthService;
import com.investmind.util.JwtUtils;
import com.investmind.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final InvestmentPhilosophyMapper philosophyMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse.LoginResponse register(AuthRequest.RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && userMapper.countByEmail(request.getEmail()) > 0) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setStatus(1);
        user.setUserType("USER");

        userMapper.insert(user);

        // 创建默认投资理念配置
        InvestmentPhilosophy philosophy = new InvestmentPhilosophy();
        philosophy.setUserId(user.getId());
        philosophy.setAiLearningEnabled(1);
        philosophy.setLearningIterations(0);
        philosophyMapper.insert(philosophy);

        log.info("用户注册成功: {}", request.getUsername());

        // 生成token
        return generateLoginResponse(user);
    }

    @Override
    public AuthResponse.LoginResponse login(AuthRequest.LoginRequest request) {
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户信息
            User user = userMapper.selectByUsername(request.getUsername());
            if (user == null) {
                throw new BadCredentialsException("用户不存在");
            }

            // 检查用户状态
            if (user.getStatus() != 1) {
                throw new RuntimeException("账号已被禁用");
            }

            // 更新登录信息
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);

            log.info("用户登录成功: {}", request.getUsername());

            return generateLoginResponse(user);
        } catch (BadCredentialsException e) {
            throw new RuntimeException("用户名或密码错误");
        }
    }

    @Override
    public AuthResponse.RefreshResponse refreshToken(AuthRequest.RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // 验证刷新令牌
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new RuntimeException("刷新令牌无效或已过期");
        }

        // 验证令牌类型
        String tokenType = jwtUtils.getTokenType(refreshToken);
        if (!"REFRESH".equals(tokenType)) {
            throw new RuntimeException("无效的令牌类型");
        }

        // 获取用户信息
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        String username = jwtUtils.getUsernameFromToken(refreshToken);

        // 验证用户是否存在且有效
        User user = userMapper.selectById(userId);
        if (user == null || !user.getUsername().equals(username) || user.getStatus() != 1) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        // 生成新的访问令牌
        String newAccessToken = jwtUtils.generateAccessToken(userId, username);

        return AuthResponse.RefreshResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getAccessTokenExpiration())
                .build();
    }

    @Override
    public void logout(String token) {
        // 这里可以实现token黑名单逻辑
        // 简单实现：将token加入Redis黑名单，设置过期时间为token剩余时间
        log.info("用户登出");
        SecurityContextHolder.clearContext();
    }

    /**
     * 生成登录响应
     */
    private AuthResponse.LoginResponse generateLoginResponse(User user) {
        // 生成访问令牌和刷新令牌
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        // 构建用户信息
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .userType(user.getUserType())
                .lastLoginTime(user.getLastLoginTime())
                .build();

        return AuthResponse.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getAccessTokenExpiration())
                .user(userInfo)
                .build();
    }
}
