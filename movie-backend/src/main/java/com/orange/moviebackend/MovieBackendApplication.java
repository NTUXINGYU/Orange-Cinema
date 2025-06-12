package com.orange.moviebackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // [核心新增] 1. 导入 Java 8 时间模块
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
@MapperScan("com.orange.moviebackend.mapper")
public class MovieBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieBackendApplication.class, args);
        log.info("==================Project backend started successfully=======================");
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }
}