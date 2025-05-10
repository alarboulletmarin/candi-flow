package com.candiflow.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8082", "http://127.0.0.1:8082", 
                               "http://localhost:19006", "http://127.0.0.1:19006", 
                               "http://localhost:19000", "http://127.0.0.1:19000", 
                               "http://localhost:35207", "http://127.0.0.1:35207",
                               "http://localhost:8081", "http://127.0.0.1:8081",
                               "http://localhost:40843", "http://127.0.0.1:40843")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
