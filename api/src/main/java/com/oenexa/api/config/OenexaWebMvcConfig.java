package com.oenexa.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Enterprise Web MVC Configuration for OENEXA REST APIs.
 * Configures cross-origin resource sharing (CORS) and async virtual thread support.
 */
@Configuration
public class OenexaWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Correlation-Id", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
