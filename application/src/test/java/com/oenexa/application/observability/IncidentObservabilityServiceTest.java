package com.oenexa.application.observability;

import com.oenexa.domain.model.observability.IncidentObservabilityDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IncidentObservabilityService Zero-Mock Unit Tests")
class IncidentObservabilityServiceTest {

    private IncidentObservabilityService service;

    @BeforeEach
    void setUp() {
        service = new IncidentObservabilityService();
    }

    @Test
    @DisplayName("Should triage OutOfMemoryError as P1_CRITICAL with JVM runbooks")
    void testOomCriticalTriage() {
        IncidentTriageRequest request = new IncidentTriageRequest(
                "OOMKilled in trading engine",
                "oenexa-trading-engine",
                "java.lang.OutOfMemoryError: Java heap space at com.oenexa.OrderBook.match(OrderBook.java:142)",
                List.of(new MetricAnomaly("jvm.memory.used", 98.5, 80.0, "%", true, "Exceeded heap limits")),
                List.of(new SloViolation("oenexa-trading-engine", "Latency-p99", 5000.0, 50.0, true)),
                Map.of("cluster", "prod-eu-1")
        );

        IncidentTriageResponse response = service.triageIncident(request);

        assertNotNull(response);
        assertEquals(IncidentSeverity.P1_CRITICAL, response.calculatedSeverity());
        assertEquals("JVM_RESOURCE_EXHAUSTION", response.rca().failureCategory());
        assertTrue(response.rca().confidenceScore() >= 0.90);
        assertTrue(response.rca().recommendedRunbooks().stream().anyMatch(r -> r.contains("JVM-001")));
        assertTrue(response.rca().immediateActionItems().stream().anyMatch(i -> i.contains("Incident Commander")));

        Optional<IncidentDossier> dossier = service.getDossier(response.incidentId());
        assertTrue(dossier.isPresent());
        assertEquals(IncidentStatus.TRIAGED, dossier.get().status());
    }

    @Test
    @DisplayName("Should triage database connection timeout as P2_HIGH with DB runbooks")
    void testDatabaseTimeoutTriage() {
        IncidentTriageRequest request = new IncidentTriageRequest(
                "HikariCP connection timeout",
                "oenexa-account-service",
                "org.postgresql.util.PSQLException: Connection refused (ConnectionRefused)",
                List.of(new MetricAnomaly("db.pool.active", 50.0, 48.0, "conns", true, "Pool exhausted")),
                List.of(new SloViolation("oenexa-account-service", "Availability", 97.5, 99.9, true)),
                Map.of()
        );

        IncidentTriageResponse response = service.triageIncident(request);

        assertNotNull(response);
        assertTrue(response.calculatedSeverity() == IncidentSeverity.P1_CRITICAL || response.calculatedSeverity() == IncidentSeverity.P2_HIGH);
        assertEquals("DATABASE_INFRASTRUCTURE", response.rca().failureCategory());
        assertTrue(response.rca().recommendedRunbooks().stream().anyMatch(r -> r.contains("DB-002")));
    }

    @Test
    @DisplayName("Should list all dossiers from in-memory registry")
    void testListDossiers() {
        IncidentTriageRequest req1 = new IncidentTriageRequest(
                "Warning CPU Spike",
                "oenexa-ai-core",
                "",
                List.of(new MetricAnomaly("cpu.usage", 75.0, 70.0, "%", true, "Spike")),
                List.of(),
                Map.of()
        );

        service.triageIncident(req1);
        List<IncidentDossier> dossiers = service.listDossiers();

        assertFalse(dossiers.isEmpty());
        assertEquals(1, dossiers.size());
    }
}
