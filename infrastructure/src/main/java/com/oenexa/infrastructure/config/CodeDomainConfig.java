package com.oenexa.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enterprise Configuration Layer for Code Intelligence & AST Domain.
 * Configures analysis timeouts, cyclomatic complexity limits, and zero-mock testing enforcement rules.
 */
@Configuration
public class CodeDomainConfig {

    public record CodeProperties(
            int maxFileLines,
            int maxCyclomaticComplexity,
            long analysisTimeoutSeconds,
            boolean zeroMockEnforced
    ) {
        public CodeProperties() {
            this(400, 20, 30L, true);
        }
    }

    @Bean
    public CodeProperties codeProperties() {
        return new CodeProperties();
    }
}
