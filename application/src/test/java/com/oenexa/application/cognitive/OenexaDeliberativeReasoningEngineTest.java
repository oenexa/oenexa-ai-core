package com.oenexa.application.cognitive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaDeliberativeReasoningEngine Zero-Mock Unit Tests")
class OenexaDeliberativeReasoningEngineTest {

    private OenexaDeliberativeReasoningEngine engine;

    @BeforeEach
    void setUp() {
        engine = new OenexaDeliberativeReasoningEngine();
    }

    @Test
    @DisplayName("Should execute 5-stage deliberative reasoning with high effort")
    void shouldExecuteDeliberativeReasoningHighEffort() {
        var result = engine.reason("Architect a distributed zero-copy cache for Spring Boot 4", "high");

        assertNotNull(result);
        assertNotNull(result.finalSynthesis());
        assertNotNull(result.reasoningTraceMarkdown());
        assertNotNull(result.trace());
        assertEquals(0.99, result.confidenceScore());
        assertFalse(result.candidateHypotheses().isEmpty());
        assertTrue(result.reasoningTraceMarkdown().contains("Stage 1: Intent Understanding"));
        assertTrue(result.reasoningTraceMarkdown().contains("Stage 4: Self-Critique Engine"));
        assertTrue(result.finalSynthesis().contains("Architectural Decision Record"));
    }

    @Test
    @DisplayName("Should execute deliberative reasoning with default and low effort levels")
    void shouldExecuteDeliberativeReasoningOtherEffortLevels() {
        var mediumRes = engine.reason("Implement reactive event bus", "medium");
        assertEquals(0.98, mediumRes.confidenceScore());

        var lowRes = engine.reason("Format log output", "low");
        assertEquals(0.95, lowRes.confidenceScore());

        var defaultRes = engine.reason("Quick sanity check", null);
        assertEquals(0.98, defaultRes.confidenceScore());
    }

    @Test
    @DisplayName("Should reject null user prompt with NullPointerException")
    void shouldRejectNullPrompt() {
        assertThrows(NullPointerException.class, () -> engine.reason(null, "medium"));
    }
}
