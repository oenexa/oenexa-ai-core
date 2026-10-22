package com.oenexa.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaLiveLlmGatewayService Zero-Mock Unit Tests")
class OenexaLiveLlmGatewayServiceTest {

    private final OenexaLiveLlmGatewayService gateway = new OenexaLiveLlmGatewayService();

    @Test
    @DisplayName("Should return dynamic actionable fallback when no external API key is present")
    void shouldReturnActionableFallback() {
        var response = gateway.generate(
                "Design a distributed ledger with event-sourcing and CQRS",
                "claude-3-5-sonnet",
                null,
                null
        );

        assertNotNull(response);
        assertNotNull(response.reply());
        assertTrue(response.reply().contains("OENEXA AI"));
        assertTrue(response.reply().contains("Hexagonal"));
        assertNotNull(response.thoughtProcess());
        assertNotNull(response.provider());
    }

    @Test
    @DisplayName("Should handle empty prompt gracefully")
    void shouldHandleEmptyPromptGracefully() {
        var response = gateway.generate("", "gpt-4o", null, null);
        assertNotNull(response);
        assertFalse(response.isLive());
        assertEquals("NONE", response.provider());
    }
}
