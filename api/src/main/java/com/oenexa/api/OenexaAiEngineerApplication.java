package com.oenexa.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Application Entry Point.
 * Why: Standard execution bootstrap for OENEXA AI Engineer.
 * How: SpringApplication.run scanning com.oenexa packages.
 */
@SpringBootApplication(scanBasePackages = "com.oenexa")
public class OenexaAiEngineerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OenexaAiEngineerApplication.class, args);
    }
}
