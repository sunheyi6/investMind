package com.investmind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智投进化平台启动类
 */
@SpringBootApplication
@EnableScheduling
public class InvestMindApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestMindApplication.class, args);
        System.out.println("========================================");
        System.out.println("  InvestMind 智投进化平台启动成功!");
        System.out.println("  API地址: http://localhost:8080/api");
        System.out.println("========================================");
    }
}
