package com.oenexa.domain.model.intelligence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaIntelligenceDomain Sovereign Invariant Tests")
class OenexaIntelligenceDomainTest {

    @Test
    @DisplayName("Should create OenexaPrompt with defensive defaults and original naming")
    void shouldCreateOenexaPrompt() {
        UUID id = UUID.randomUUID();
        OenexaIntelligenceDomain.OenexaPrompt prompt = new OenexaIntelligenceDomain.OenexaPrompt(
                id,
                "sovereign-engineer",
                "oenexa-core",
                "Analyze clean hexagonal modular invariants",
                null,
                null,
                null
        );

        assertEquals(id, prompt.promptId());
        assertEquals("sovereign-engineer", prompt.userId());
        assertEquals("OENEXA-CNE-32B", prompt.sovereignEnginePreference());
        assertTrue(prompt.contextFiles().isEmpty());
        assertNotNull(prompt.timestamp());
    }

    @Test
    @DisplayName("Should reject OenexaPrompt when mandatory fields are null")
    void shouldRejectNullPromptFields() {
        assertThrows(NullPointerException.class, () ->
                new OenexaIntelligenceDomain.OenexaPrompt(null, "u", "w", "q", null, null, null));
    }

    @Test
    @DisplayName("Should create OenexaIntelligenceResponse enforcing direct answer first and structured reasoning")
    void shouldCreateOenexaIntelligenceResponse() {
        UUID responseId = UUID.randomUUID();
        UUID promptId = UUID.randomUUID();

        OenexaIntelligenceDomain.OenexaReasoningTrace trace = new OenexaIntelligenceDomain.OenexaReasoningTrace(
                List.of("1. Understand Intent", "2. Analyze Context", "3. Reason", "4. Validate"),
                "Sovereign Invariants Satisfied",
                true,
                38L
        );

        OenexaIntelligenceDomain.OenexaIntelligenceResponse response = new OenexaIntelligenceDomain.OenexaIntelligenceResponse(
                responseId,
                promptId,
                "Direct Answer: Hexagonal modular architecture enforces zero framework lock-in.",
                trace,
                new OenexaIntelligenceDomain.OenexaValidationReport(true, 100, List.of("Rule 1", "Rule 2")),
                List.of("Recommendation 1", "Recommendation 2"),
                "Step 1 -> Step 2 -> Step 3",
                List.of(new OenexaIntelligenceDomain.OenexaArtifact("art-1", "Domain.java", "src/Domain.java", "java", "CREATE", "record D() {}", "")),
                "OENEXA-HRS-70B",
                Instant.now()
        );

        assertTrue(response.directAnswer().startsWith("Direct Answer:"));
        assertEquals(4, response.reasoningTrace().analyticalSteps().size());
        assertEquals(100, response.validationReport().invariantScore());
        assertEquals(2, response.actionableRecommendations().size());
        assertEquals("OENEXA-HRS-70B", response.activeNeuralEngine());
    }
}
