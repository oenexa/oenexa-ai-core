package com.oenexa.application.engine;

import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.ClaudeReasoningTrace;
import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.DeepReasoningTrace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaDeepReasoningEngine Zero-Mock Unit Tests")
class OenexaDeepReasoningEngineTest {

    private final OenexaDeepReasoningEngine engine = new OenexaDeepReasoningEngine();

    @Test
    @DisplayName("Should reject null objective")
    void shouldRejectNullObjective() {
        assertThrows(NullPointerException.class, () -> engine.analyzeWithDeepReasoning(null, "context"));
        assertThrows(NullPointerException.class, () -> engine.analyzeWithClaudeReasoning(null, "context"));
    }

    @Test
    @DisplayName("Should synthesize deep reasoning trace with Tree-of-Thought branches")
    void shouldSynthesizeDeepReasoningTrace() {
        DeepReasoningTrace trace = engine.analyzeWithDeepReasoning("Enterprise Hexagonal Architecture", "class CoreEngine {}");
        assertNotNull(trace);
        assertEquals("Enterprise Hexagonal Architecture", trace.objective());
        assertEquals(3, trace.hypotheses().size());
        assertEquals("Hexagonal Modular Monolith Architecture", trace.selectedBranch().hypothesis());
        assertTrue(trace.tradeOffAnalysis().contains("OENEXA Sovereign"));
        assertTrue(trace.reflectionVerdict().contains("VERIFIED"));
        assertEquals(0.99, trace.calibratedConfidence());
    }

    @Test
    @DisplayName("Should provide backward-compatible ClaudeReasoningTrace")
    void shouldProvideBackwardCompatibleTrace() {
        ClaudeReasoningTrace trace = engine.analyzeWithClaudeReasoning("Legacy Invariant Check", null);
        assertNotNull(trace);
        assertEquals("Legacy Invariant Check", trace.objective());
        assertEquals(3, trace.hypotheses().size());
        assertNotNull(trace.selectedBranch());
        assertEquals(0.99, trace.calibratedConfidence());
    }
}
