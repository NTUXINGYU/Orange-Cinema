package com.orange.moviebackend;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@MapperScan("com.orange.moviebackend.mapper")
public class MovieBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(MovieBackendApplication.class, args);
        log.info("==================Project backend started successfully=======================");
    }
}
