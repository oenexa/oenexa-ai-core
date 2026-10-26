package com.oenexa.application.engine;

import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.CopilotCompletionRequest;
import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.CopilotCompletionResponse;
import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.IntelligentCompletionRequest;
import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.IntelligentCompletionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaIntelligentCompletionEngine Zero-Mock Unit Tests")
class OenexaIntelligentCompletionEngineTest {

    private final OenexaIntelligentCompletionEngine engine = new OenexaIntelligentCompletionEngine();

    @Test
    @DisplayName("Should reject null request")
    void shouldRejectNullRequest() {
        assertThrows(NullPointerException.class, () -> engine.generateInlineCompletion(null));
        assertThrows(NullPointerException.class, () -> engine.generateCopilotInlineCompletion(null));
    }

    @Test
    @DisplayName("Should generate inline ghost text for record definition")
    void shouldGenerateGhostTextForRecord() {
        IntelligentCompletionRequest req = new IntelligentCompletionRequest("public record Solution", "Solution.java");
        IntelligentCompletionResponse resp = engine.generateInlineCompletion(req);

        assertNotNull(resp);
        assertFalse(resp.suggestions().isEmpty());
        assertTrue(resp.ghostText().contains("record"));
        assertEquals(0.98, resp.confidenceScore());
    }

    @Test
    @DisplayName("Should generate test case ghost text when void/test keyword is detected")
    void shouldGenerateGhostTextForTest() {
        IntelligentCompletionRequest req = new IntelligentCompletionRequest("@Test void testInvariants", "Test.java");
        IntelligentCompletionResponse resp = engine.generateInlineCompletion(req);

        assertNotNull(resp);
        assertTrue(resp.ghostText().contains("@Test"));
    }

    @Test
    @DisplayName("Should provide backward-compatible CopilotCompletionResponse")
    void shouldProvideCopilotCompletion() {
        CopilotCompletionRequest req = new CopilotCompletionRequest("Objects.require", "src/Main.java");
        CopilotCompletionResponse resp = engine.generateCopilotInlineCompletion(req);

        assertNotNull(resp);
        assertTrue(resp.ghostText().contains("Objects.requireNonNull"));
    }
}
