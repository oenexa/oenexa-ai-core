package com.oenexa.domain.model.observability;

import com.oenexa.domain.model.observability.IncidentObservabilityDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IncidentObservabilityDomain Invariant Tests (Zero-Mock)")
class IncidentObservabilityDomainTest {

    @Test
    @DisplayName("MetricAnomaly correctly retains metrics and handles nulls")
    void testMetricAnomalyDefaults() {
        MetricAnomaly anomaly = new MetricAnomaly("http_error_rate", 12.5, 5.0, "%", true, null);
        assertEquals("http_error_rate", anomaly.metricName());
        assertEquals(12.5, anomaly.observedValue());
        assertTrue(anomaly.isAnomaly());
        assertEquals("", anomaly.details());
    }

    @Test
    @DisplayName("SloViolation throws NullPointerException when service is null")
    void testSloViolationNullValidation() {
        assertThrows(NullPointerException.class, () ->
                new SloViolation(null, "Availability99.9", 98.2, 99.9, true)
        );
    }

    @Test
    @DisplayName("RootCauseAnalysis clamps confidence and immutably wraps lists")
    void testRootCauseAnalysisClamping() {
        RootCauseAnalysis rca = new RootCauseAnalysis(
                "INC-101",
                "DATABASE_EXHAUSTION",
                "Connection pool maxed out at 500 connections",
                List.of("oenexa-auth", "oenexa-order"),
                List.of("RUNBOOK-DB-001"),
                List.of("Restart pool", "Scale read replicas"),
                1.45
        );

        assertEquals("INC-101", rca.incidentId());
        assertEquals(1.0, rca.confidenceScore(), 0.001);
        assertEquals(2, rca.affectedServices().size());
        assertEquals(1, rca.recommendedRunbooks().size());
    }

    @Test
    @DisplayName("IncidentDossier immutably initializes with default instant")
    void testIncidentDossierCreation() {
        RootCauseAnalysis rca = new RootCauseAnalysis(
                "INC-102",
                "MEMORY_LEAK",
                "Heap exhaustion in JVM container",
                List.of("oenexa-core"),
                List.of("RUNBOOK-JVM-01"),
                List.of("Restart pod"),
                0.95
        );

        IncidentDossier dossier = new IncidentDossier(
                "INC-102",
                "High JVM Memory Pressure",
                "oenexa-ai-core",
                IncidentSeverity.P1_CRITICAL,
                IncidentStatus.OPEN,
                null,
                List.of(new MetricAnomaly("jvm.memory.used", 98.0, 85.0, "%", true, "Critical heap")),
                List.of(new SloViolation("oenexa-ai-core", "Latency-p99", 1200.0, 300.0, true)),
                rca
        );

        assertNotNull(dossier.reportedAt());
        assertEquals(IncidentSeverity.P1_CRITICAL, dossier.severity());
        assertEquals(1, dossier.anomalies().size());
        assertEquals(1, dossier.sloViolations().size());
    }

    @Test
    @DisplayName("IncidentTriageRequest safely captures metadata and empty stack trace")
    void testIncidentTriageRequestDefaults() {
        IncidentTriageRequest request = new IncidentTriageRequest(
                "Gateway 504 Timeout",
                "oenexa-gateway",
                null,
                null,
                null,
                Map.of("region", "eu-west-1")
        );

        assertEquals("", request.stackTrace());
        assertTrue(request.anomalies().isEmpty());
        assertTrue(request.sloViolations().isEmpty());
        assertEquals("eu-west-1", request.metadata().get("region"));
    }
}
