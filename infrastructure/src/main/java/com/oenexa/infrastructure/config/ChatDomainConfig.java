package com.oenexa.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enterprise Configuration Layer for Chat & Conversation Domain.
 * Configures default model, max context window length, and streaming parameters.
 */
@Configuration
public class ChatDomainConfig {

    public record ChatProperties(
            String defaultModel,
            int maxPromptLength,
            int maxHistoryMessages,
            boolean streamingEnabled
    ) {
        public ChatProperties() {
            this("gemini-2.5-flash", 32_000, 50, true);
        }
    }

    @Bean
    public ChatProperties chatProperties() {
        return new ChatProperties();
    }
}
