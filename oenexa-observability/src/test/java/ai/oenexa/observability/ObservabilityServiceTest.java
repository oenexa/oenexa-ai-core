package ai.oenexa.observability;

import ai.oenexa.observability.domain.ErrorGroup;
import ai.oenexa.observability.domain.IncidentSeverity;
import ai.oenexa.observability.domain.IncidentTimeline;
import ai.oenexa.observability.domain.LogAnalyticsSummary;
import ai.oenexa.observability.domain.LogEvent;
import ai.oenexa.observability.domain.LogLevel;
import ai.oenexa.observability.service.DefaultObservabilityService;
import ai.oenexa.observability.web.ObservabilityController;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OENEXA Observability Service & Controller Tests (Zero-Mock Policy)")
class ObservabilityServiceTest {

    private DefaultObservabilityService observabilityService;
    private ObservabilityController observabilityController;

    @BeforeEach
    void setUp() {
        observabilityService = new DefaultObservabilityService();
        observabilityController = new ObservabilityController(observabilityService);
    }

    @Test
    @DisplayName("ObservabilityModule metadata and initialization")
    void testObservabilityModuleMetadata() {
        ObservabilityModule module = new ObservabilityModule();
        assertEquals("observability", module.id());
        assertEquals("Log Analytics", module.displayName());
        assertTrue(module.capabilities().contains("incident-timeline"));
    }

    @Test
    @DisplayName("LogLevel and IncidentSeverity enum invariants")
    void testEnumInvariants() {
        assertEquals(6, LogLevel.values().length);
        assertEquals(LogLevel.ERROR, LogLevel.valueOf("ERROR"));
        assertEquals(LogLevel.FATAL, LogLevel.valueOf("FATAL"));

        assertEquals(4, IncidentSeverity.values().length);
        assertEquals(IncidentSeverity.P1_CRITICAL, IncidentSeverity.valueOf("P1_CRITICAL"));
    }

    @Test
    @DisplayName("Domain records null safety and fallback invariants")
    void testDomainRecordsEdgeCases() {
        LogEvent event = new LogEvent("log-1", null, LogLevel.INFO, null, null, "message", null, null);
        assertNotNull(event.timestamp());
        assertEquals("default-service", event.service());
        assertEquals("ROOT", event.logger());
        assertEquals("", event.stackTrace());
        assertTrue(event.tags().isEmpty());

        assertThrows(NullPointerException.class, () -> new LogEvent(null, Instant.now(), LogLevel.INFO, "s", "l", "m", "", Map.of()));
        assertThrows(NullPointerException.class, () -> new LogEvent("id", Instant.now(), null, "s", "l", "m", "", Map.of()));
        assertThrows(NullPointerException.class, () -> new LogEvent("id", Instant.now(), LogLevel.INFO, "s", "l", null, "", Map.of()));

        ErrorGroup group = new ErrorGroup("fp-1", "NullPointerException", "NPE in auth", 1, null, null, null);
        assertNotNull(group.firstSeen());
        assertNotNull(group.lastSeen());
        assertTrue(group.affectedServices().isEmpty());

        assertThrows(NullPointerException.class, () -> new ErrorGroup(null, "E", "m", 1, null, null, null));
        assertThrows(NullPointerException.class, () -> new ErrorGroup("fp", null, "m", 1, null, null, null));
        assertThrows(NullPointerException.class, () -> new ErrorGroup("fp", "E", null, 1, null, null, null));

        IncidentTimeline timeline = new IncidentTimeline("inc-1", "Outage", null, null, null, null, null);
        assertEquals(IncidentSeverity.P3_MEDIUM, timeline.severity());
        assertNotNull(timeline.startTime());
        assertTrue(timeline.eventSequence().isEmpty());
        assertTrue(timeline.suggestedRootCause().contains("Root cause under active investigation"));

        assertThrows(NullPointerException.class, () -> new IncidentTimeline(null, "T", IncidentSeverity.P1_CRITICAL, null, null, null, null));
        assertThrows(NullPointerException.class, () -> new IncidentTimeline("i", null, IncidentSeverity.P1_CRITICAL, null, null, null, null));

        LogAnalyticsSummary summary = new LogAnalyticsSummary(0, 0, 0, 0, null, 0.0);
        assertTrue(summary.topErrorGroups().isEmpty());
    }

    @Test
    @DisplayName("Ingest telemetry logs and group error fingerprints")
    void testIngestAndGroupErrors() {
        observabilityService.ingestLogs(null);
        observabilityService.ingestLogs(List.of());

        Instant now = Instant.now();
        LogEvent infoLog = new LogEvent("l1", now, LogLevel.INFO, "auth-service", "com.oenexa.auth.AuthController", "Token issued", "", Map.of());
        LogEvent warnLog = new LogEvent("l2", now, LogLevel.WARN, "auth-service", "com.oenexa.auth.TokenService", "Clock skew detected", "", Map.of());
        LogEvent errLog1 = new LogEvent("l3", now, LogLevel.ERROR, "auth-service", "com.oenexa.auth.TokenService", "Invalid JWT signature", "Exception at...", Map.of());
        LogEvent errLog2 = new LogEvent("l4", now.plusSeconds(1), LogLevel.ERROR, "gateway-service", "com.oenexa.auth.TokenService", "Invalid JWT signature", "Exception at...", Map.of());

        observabilityService.ingestLogs(List.of(infoLog, warnLog, errLog1, errLog2));

        List<ErrorGroup> topErrors = observabilityService.getTopErrorGroups(5);
        assertFalse(topErrors.isEmpty());
        ErrorGroup jwtError = topErrors.get(0);
        assertEquals(2, jwtError.occurrenceCount());
        assertTrue(jwtError.affectedServices().contains("auth-service"));
        assertTrue(jwtError.affectedServices().contains("gateway-service"));

        // Effective limit <= 0 test
        assertEquals(1, observabilityService.getTopErrorGroups(0).size());
    }

    @Test
    @DisplayName("Compute analytics summary with service filter and time window")
    void testAnalyticsSummary() {
        Instant t0 = Instant.now().minusSeconds(100);
        Instant t1 = Instant.now();

        observabilityService.ingestLogs(List.of(
                new LogEvent("1", t0, LogLevel.INFO, "api", "Logger", "App started", "", Map.of()),
                new LogEvent("2", t1, LogLevel.ERROR, "api", "Logger", "Db connection dropped", "SqlException...", Map.of()),
                new LogEvent("3", t1, LogLevel.WARN, "api", "Logger", "Slow query", "", Map.of()),
                new LogEvent("4", t1, LogLevel.INFO, "chat", "Logger", "Chat message sent", "", Map.of())
        ));

        LogAnalyticsSummary apiSummary = observabilityService.getAnalyticsSummary("api", t0.minusSeconds(1));
        assertEquals(3, apiSummary.totalLogs());
        assertEquals(1, apiSummary.errorCount());
        assertEquals(1, apiSummary.warnCount());
        assertTrue(apiSummary.errorRatePercent() > 30.0);

        LogAnalyticsSummary allSummary = observabilityService.getAnalyticsSummary(null, null);
        assertEquals(4, allSummary.totalLogs());
    }

    @Test
    @DisplayName("Correlate incident timeline and suggest root cause")
    void testCorrelateIncident() {
        Instant start = Instant.now().minusSeconds(60);
        Instant end = Instant.now().plusSeconds(60);

        observabilityService.ingestLogs(List.of(
                new LogEvent("inc-log-1", Instant.now(), LogLevel.WARN, "db-service", "DbPool", "Connection pool exhausted", "", Map.of()),
                new LogEvent("inc-log-2", Instant.now(), LogLevel.ERROR, "db-service", "DbPool", "Timeout acquiring PostgreSQL connection", "Timeout...", Map.of())
        ));

        IncidentTimeline timeline = observabilityService.correlateIncident("Database Outage", start, end, IncidentSeverity.P1_CRITICAL);
        assertEquals("Database Outage", timeline.title());
        assertEquals(IncidentSeverity.P1_CRITICAL, timeline.severity());
        assertEquals(2, timeline.eventSequence().size());
        assertTrue(timeline.suggestedRootCause().contains("Timeout acquiring PostgreSQL connection"));

        // No logs incident test
        IncidentTimeline emptyTimeline = observabilityService.correlateIncident("Empty Window", Instant.now().plusSeconds(1000), Instant.now().plusSeconds(2000), null);
        assertTrue(emptyTimeline.suggestedRootCause().contains("No critical error signatures detected"));
    }

    @Test
    @DisplayName("ObservabilityController endpoints return valid HTTP responses")
    void testControllerEndpoints() {
        ResponseEntity<Map<String, Integer>> ingResp = observabilityController.ingest(List.of(
                new LogEvent("c1", Instant.now(), LogLevel.INFO, "ctrl", "L", "ok", "", Map.of())
        ));
        assertEquals(200, ingResp.getStatusCode().value());
        assertEquals(1, ingResp.getBody().get("ingested"));

        ResponseEntity<LogAnalyticsSummary> aResp = observabilityController.analytics("ctrl", Instant.now().minusSeconds(10).toString());
        assertEquals(200, aResp.getStatusCode().value());
        assertNotNull(aResp.getBody());

        ResponseEntity<List<ErrorGroup>> eResp = observabilityController.getErrors(5);
        assertEquals(200, eResp.getStatusCode().value());
        assertNotNull(eResp.getBody());

        ResponseEntity<IncidentTimeline> incResp = observabilityController.correlateIncident(Map.of(
                "title", "Controller Incident",
                "severity", "P2_HIGH",
                "startTime", Instant.now().minusSeconds(300).toString(),
                "endTime", Instant.now().toString()
        ));
        assertEquals(200, incResp.getStatusCode().value());
        assertNotNull(incResp.getBody());
    }
}
