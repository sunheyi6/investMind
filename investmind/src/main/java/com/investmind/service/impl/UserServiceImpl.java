package com.investmind.service.impl;

import com.investmind.dto.UserRequest;
import com.investmind.dto.UserResponse;
import com.investmind.model.User;
import com.investmind.repository.UserMapper;
import com.investmind.service.UserService;
import com.investmind.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public UserResponse.ProfileResponse getCurrentUserProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        return convertToProfileResponse(user);
    }

    @Override
    public UserResponse.ProfileResponse updateCurrentUserProfile(UserRequest.UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查邮箱是否被其他用户使用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userMapper.countByEmail(request.getEmail()) > 0) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
            user.setEmail(request.getEmail());
        }

        // 更新字段
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        userMapper.updateById(user);

        return convertToProfileResponse(user);
    }

    @Override
    public void changePassword(UserRequest.ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        log.info("用户 {} 修改密码成功", user.getUsername());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 检查用户状态
        boolean enabled = user.getStatus() != null && user.getStatus() == 1;

        // 创建权限列表
        String role = user.getUserType() != null ? user.getUserType() : "USER";

        return new SecurityUtils.UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                role,
                enabled,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    /**
     * 转换为ProfileResponse
     */
    private UserResponse.ProfileResponse convertToProfileResponse(User user) {
        return UserResponse.ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .userType(user.getUserType())
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .createdTime(user.getCreatedTime())
                .build();
    }
}
