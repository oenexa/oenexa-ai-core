package com.oenexa.application.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Oenexa Performance & Observability Engines Zero-Mock Tests")
class OenexaPerformanceAndObservabilityTest {

    private OenexaPerformanceEngine performanceEngine;
    private OenexaObservabilityEngine observabilityEngine;

    @BeforeEach
    void setUp() {
        performanceEngine = new OenexaPerformanceEngine();
        observabilityEngine = new OenexaObservabilityEngine();
    }

    @Test
    @DisplayName("Should track execution in performance engine with caching")
    void testPerformanceEngine() {
        assertEquals(0, performanceEngine.getCacheSize());

        String result = performanceEngine.trackExecution("key1", () -> "Executed Value");
        assertEquals("Executed Value", result);

        performanceEngine.putCache("key1", "Cached Result");
        assertEquals(1, performanceEngine.getCacheSize());
        assertEquals("Cached Result", performanceEngine.getCache("key1"));

        var snapshot = performanceEngine.captureMetrics("Short input", true, 10);
        assertTrue(snapshot.cacheHit());
        assertEquals(1.2, snapshot.compressionRatio());
        assertEquals(10L, snapshot.durationMs());

        performanceEngine.clearCache();
        assertEquals(0, performanceEngine.getCacheSize());
    }

    @Test
    @DisplayName("Should record distributed tracing spans and track error metrics in observability engine")
    void testObservabilityEngine() {
        assertEquals(0, observabilityEngine.getTotalProcessedCount());

        var spanOk = observabilityEngine.startAndRecordSpan(
                "TEST_ENGINE", "EXECUTE_TEST", 25, "OK", Map.of("env", "staging")
        );
        assertNotNull(spanOk.traceId());
        assertEquals("OK", spanOk.status());
        assertEquals(1, observabilityEngine.getTotalProcessedCount());

        var spanErr = observabilityEngine.startAndRecordSpan(
                "TEST_ENGINE", "EXECUTE_TEST", 50, "ERROR", Map.of()
        );
        assertEquals("ERROR", spanErr.status());
        assertEquals(2, observabilityEngine.getTotalProcessedCount());

        var errorMetrics = observabilityEngine.getErrorMetrics();
        assertTrue(errorMetrics.containsKey("EXECUTE_TEST"));
        assertEquals(1L, errorMetrics.get("EXECUTE_TEST"));

        var summary = observabilityEngine.getSystemTelemetrySummary();
        assertEquals("HEALTHY", summary.get("systemStatus"));
        assertEquals(2L, summary.get("totalDirectivesProcessed"));
    }
}
