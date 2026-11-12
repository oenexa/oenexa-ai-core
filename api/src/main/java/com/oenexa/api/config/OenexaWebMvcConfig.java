package com.oenexa.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Enterprise Web MVC Configuration for OENEXA REST APIs.
 * Configures cross-origin resource sharing (CORS) with explicit trusted origin patterns.
 */
@Configuration
public class OenexaWebMvcConfig implements WebMvcConfigurer {

    @Value("${oenexa.cors.allowed-origins:https://*.oenexa.com,https://oenexa.com,http://localhost:*,http://127.0.0.1:*}")
    private String[] allowedOriginPatterns;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Correlation-Id", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
