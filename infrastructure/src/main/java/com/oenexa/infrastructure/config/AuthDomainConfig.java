package com.oenexa.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enterprise Configuration Layer for Authentication & Identity Domain.
 * Configures token lifetime, maximum failed attempts, and security credentials properties.
 */
@Configuration
public class AuthDomainConfig {

    public record AuthProperties(
            long accessTokenExpirySeconds,
            long refreshTokenExpirySeconds,
            int maxFailedAttempts,
            int lockoutDurationMinutes
    ) {
        public AuthProperties() {
            this(900L, 604800L, 5, 15);
        }
    }

    @Bean
    public AuthProperties authProperties() {
        return new AuthProperties();
    }
}
