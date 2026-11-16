package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.observability.IncidentObservabilityDtos.*;
import com.oenexa.application.observability.IncidentObservabilityService;
import com.oenexa.domain.model.observability.IncidentObservabilityDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IncidentObservabilityController Zero-Mock Unit Tests")
class IncidentObservabilityControllerTest {

    private IncidentObservabilityService service;
    private IncidentObservabilityController controller;

    @BeforeEach
    void setUp() {
        service = new IncidentObservabilityService();
        controller = new IncidentObservabilityController(service);
    }

    @Test
    @DisplayName("Should triage incident, return P1_CRITICAL and persist dossier")
    void testTriageEndpoint() {
        IncidentTriageRequestDto requestDto = new IncidentTriageRequestDto(
                "Critical Trading OutOfMemory",
                "oenexa-trading",
                "java.lang.OutOfMemoryError: Java heap space",
                List.of(new MetricAnomalyDto("jvm.heap", 99.0, 85.0, "%", true, "Critical heap")),
                List.of(new SloViolationDto("oenexa-trading", "p99", 2500.0, 50.0, true)),
                Map.of("env", "production")
        );

        ResponseEntity<ApiResponse<IncidentTriageResponseDto>> response = controller.triageIncident(requestDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().success());

        IncidentTriageResponseDto data = response.getBody().data();
        assertNotNull(data.incidentId());
        assertEquals(IncidentSeverity.P1_CRITICAL, data.calculatedSeverity());
        assertEquals("JVM_RESOURCE_EXHAUSTION", data.rca().failureCategory());
        assertTrue(data.rca().recommendedRunbooks().stream().anyMatch(r -> r.contains("JVM-001")));

        // Verify dossier retrieval
        ResponseEntity<ApiResponse<IncidentDossierDto>> dossierResponse = controller.getIncidentDossier(data.incidentId());
        assertEquals(200, dossierResponse.getStatusCode().value());
        assertTrue(dossierResponse.getBody().success());
        assertEquals("Critical Trading OutOfMemory", dossierResponse.getBody().data().title());
    }

    @Test
    @DisplayName("Should return 404 for non-existent incident")
    void testGetNonExistentIncident() {
        ResponseEntity<ApiResponse<IncidentDossierDto>> response = controller.getIncidentDossier("INC-9999");
        assertEquals(404, response.getStatusCode().value());
        assertFalse(response.getBody().success());
    }

    @Test
    @DisplayName("Should list all triaged incidents")
    void testListIncidents() {
        IncidentTriageRequestDto req = new IncidentTriageRequestDto(
                "Network Glitch",
                "oenexa-edge",
                "",
                List.of(),
                List.of(),
                Map.of()
        );

        controller.triageIncident(req);

        ResponseEntity<ApiResponse<List<IncidentDossierDto>>> response = controller.listIncidents();
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().success());
        assertFalse(response.getBody().data().isEmpty());
    }
}
