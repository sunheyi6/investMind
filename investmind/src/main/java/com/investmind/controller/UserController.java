package com.investmind.controller;

import com.investmind.dto.ApiResponse;
import com.investmind.dto.UserRequest;
import com.investmind.dto.UserResponse;
import com.investmind.service.PhilosophyService;
import com.investmind.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 处理用户资料、投资理念配置等相关请求
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PhilosophyService philosophyService;

    /**
     * 获取当前用户资料
     */
    @GetMapping("/profile")
    public ApiResponse<UserResponse.ProfileResponse> getProfile() {
        UserResponse.ProfileResponse profile = userService.getCurrentUserProfile();
        return ApiResponse.success(profile);
    }

    /**
     * 更新当前用户资料
     */
    @PutMapping("/profile")
    public ApiResponse<UserResponse.ProfileResponse> updateProfile(
            @Valid @RequestBody UserRequest.UpdateProfileRequest request) {
        UserResponse.ProfileResponse profile = userService.updateCurrentUserProfile(request);
        return ApiResponse.success("更新成功", profile);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody UserRequest.ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.success("密码修改成功", null);
    }

    /**
     * 获取当前用户的投资理念配置
     */
    @GetMapping("/philosophy")
    public ApiResponse<UserResponse.PhilosophyResponse> getPhilosophy() {
        UserResponse.PhilosophyResponse philosophy = philosophyService.getCurrentUserPhilosophy();
        return ApiResponse.success(philosophy);
    }

    /**
     * 更新当前用户的投资理念配置
     */
    @PutMapping("/philosophy")
    public ApiResponse<UserResponse.PhilosophyResponse> updatePhilosophy(
            @Valid @RequestBody UserRequest.UpdatePhilosophyRequest request) {
        UserResponse.PhilosophyResponse philosophy = philosophyService.updateCurrentUserPhilosophy(request);
        return ApiResponse.success("投资理念配置更新成功", philosophy);
    }
}
