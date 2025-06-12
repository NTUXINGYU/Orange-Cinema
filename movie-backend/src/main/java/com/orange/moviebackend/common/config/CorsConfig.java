package com.orange.moviebackend.common.config;

import com.orange.moviebackend.common.utils.PathUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays; // 建议具体列出，而不是用 Collections.singletonList("*")
import java.util.Collections;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许你的前端源。当 allowCredentials 为 true 时，不能使用 "*"
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:8080"));
        // 允许所有常用的HTTP方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // 允许所有请求头
        config.setAllowedHeaders(Collections.singletonList("*")); // 或者具体列出如 "Authorization", "Content-Type", "Token" 等
        // 允许发送凭证 (如 cookies)
        config.setAllowCredentials(true);
        // 预检请求的有效期
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 对所有路径应用此CORS配置

        return new CorsFilter(source);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = PathUtils.getClassLoadRootPath() + "/images/";
        registry.addResourceHandler("/images/**").
                addResourceLocations("file:" + path);
    }
}