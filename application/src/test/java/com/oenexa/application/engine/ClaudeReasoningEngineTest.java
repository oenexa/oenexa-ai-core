package com.oenexa.application.engine;

import com.oenexa.domain.TargetCapabilitiesDomain.ClaudeReasoningTrace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClaudeReasoningEngineTest {

    private final ClaudeReasoningEngine engine = new ClaudeReasoningEngine();

    @Test
    void shouldThrowExceptionWhenObjectiveIsNull() {
        assertThrows(NullPointerException.class, () -> engine.analyzeWithClaudeReasoning(null, "context"));
    }

    @Test
    void shouldAnalyzeObjectiveWithNullCodeContext() {
        ClaudeReasoningTrace trace = engine.analyzeWithClaudeReasoning("Evaluate Monolith Architecture", null);

        assertNotNull(trace);
        assertEquals("Evaluate Monolith Architecture", trace.objective());
        assertEquals(3, trace.hypotheses().size());
        assertEquals("Hexagonal Modular Monolith Architecture", trace.selectedBranch().hypothesis());
        assertTrue(trace.tradeOffAnalysis().contains("0 characters of codebase context"));
        assertTrue(trace.reflectionVerdict().contains("VERIFIED"));
        assertEquals(0.99, trace.calibratedConfidence());
        assertNotNull(trace.traceId());
        assertNotNull(trace.timestamp());
    }

    @Test
    void shouldAnalyzeObjectiveWithProvidedCodeContext() {
        String sampleContext = "public record CleanDomainRecord(String id) {}";
        ClaudeReasoningTrace trace = engine.analyzeWithClaudeReasoning("Evaluate Monolith Architecture", sampleContext);

        assertNotNull(trace);
        assertTrue(trace.tradeOffAnalysis().contains(sampleContext.length() + " characters of codebase context"));
        assertEquals("Hexagonal Modular Monolith Architecture", trace.selectedBranch().hypothesis());
        assertEquals(0.965, trace.selectedBranch().confidenceScore());
    }
}
