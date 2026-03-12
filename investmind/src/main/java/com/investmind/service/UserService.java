package com.investmind.service;

import com.investmind.dto.UserRequest;
import com.investmind.dto.UserResponse;
import com.investmind.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户服务接口
 */
public interface UserService extends UserDetailsService {

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户
     */
    User getByUsername(String username);

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户
     */
    User getById(Long id);

    /**
     * 获取当前登录用户的资料
     *
     * @return 用户资料
     */
    UserResponse.ProfileResponse getCurrentUserProfile();

    /**
     * 更新当前用户资料
     *
     * @param request 更新请求
     * @return 更新后的资料
     */
    UserResponse.ProfileResponse updateCurrentUserProfile(UserRequest.UpdateProfileRequest request);

    /**
     * 修改密码
     *
     * @param request 修改密码请求
     */
    void changePassword(UserRequest.ChangePasswordRequest request);
}
