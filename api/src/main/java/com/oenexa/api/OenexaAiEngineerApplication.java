package com.oenexa.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Application Entry Point for OENEXA AI Engineer.
 * Scans all components across com.oenexa.
 */
@SpringBootApplication(scanBasePackages = "com.oenexa")
public class OenexaAiEngineerApplication {

    static void main(String[] args) {
        SpringApplication.run(OenexaAiEngineerApplication.class, args);
    }
}
