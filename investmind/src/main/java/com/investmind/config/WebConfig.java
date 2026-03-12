package com.investmind.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 注意：CORS配置已移至SecurityConfig，由Spring Security统一管理
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS配置已移至SecurityConfig
}
