package com.investmind.service;

import com.investmind.dto.UserRequest;
import com.investmind.dto.UserResponse;

/**
 * 投资理念服务接口
 */
public interface PhilosophyService {

    /**
     * 获取当前用户的投资理念配置
     *
     * @return 投资理念配置
     */
    UserResponse.PhilosophyResponse getCurrentUserPhilosophy();

    /**
     * 根据用户ID获取投资理念配置
     *
     * @param userId 用户ID
     * @return 投资理念配置
     */
    UserResponse.PhilosophyResponse getPhilosophyByUserId(Long userId);

    /**
     * 更新当前用户的投资理念配置
     *
     * @param request 更新请求
     * @return 更新后的配置
     */
    UserResponse.PhilosophyResponse updateCurrentUserPhilosophy(UserRequest.UpdatePhilosophyRequest request);

    /**
     * 初始化用户的投资理念配置
     *
     * @param userId 用户ID
     */
    void initializePhilosophy(Long userId);

    /**
     * 用自然语言迭代更新理念
     */
    UserResponse.PhilosophyResponse ingestNaturalLanguage(String inputText);

    /**
     * 生成当前用户的投资理念文档
     */
    UserResponse.PhilosophyDocumentResponse generateCurrentUserDocument();
}
