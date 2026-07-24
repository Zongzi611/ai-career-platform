package com.careersail;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * CareerSail 大学生职业规划智能体 — 应用入口
 */
@SpringBootApplication
@MapperScan("com.careersail.mapper")
@EnableAsync
@EnableCaching
public class CareerSailApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareerSailApplication.class, args);
    }
}
