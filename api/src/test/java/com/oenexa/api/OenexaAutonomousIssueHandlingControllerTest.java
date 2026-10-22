package com.oenexa.api;

import com.oenexa.application.cognitive.OenexaAutonomousLifecycleOrchestrator;
import com.oenexa.application.cognitive.OenexaAutonomousProblemSolverEngine;
import com.oenexa.domain.OenexaAutonomousProblemSolverDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAutonomousIssueHandlingController Zero-Mock Unit Tests")
class OenexaAutonomousIssueHandlingControllerTest {

    private OenexaAutonomousIssueHandlingController controller;
    private OenexaAutonomousProblemSolverEngine solverEngine;
    private OenexaAutonomousLifecycleOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        solverEngine = new OenexaAutonomousProblemSolverEngine();
        orchestrator = new OenexaAutonomousLifecycleOrchestrator(solverEngine, new com.oenexa.application.cognitive.OenexaDeliberativeReasoningEngine());
        controller = new OenexaAutonomousIssueHandlingController(orchestrator, solverEngine);
    }

    @Test
    @DisplayName("Should test no-arg constructor and parameter validation")
    void shouldTestConstructors() {
        OenexaAutonomousIssueHandlingController defaultCtrl = new OenexaAutonomousIssueHandlingController();
        assertNotNull(defaultCtrl);

        assertThrows(NullPointerException.class, () -> new OenexaAutonomousIssueHandlingController(null, solverEngine));
        assertThrows(NullPointerException.class, () -> new OenexaAutonomousIssueHandlingController(orchestrator, null));
    }

    @Test
    @DisplayName("Should execute lifecycle with default payload when request is null")
    void shouldExecuteLifecycleWithNullRequest() {
        ResponseEntity<AutonomousLifecycleResult> response = controller.executeLifecycle(null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().successful());
    }

    @Test
    @DisplayName("Should execute lifecycle with custom prompt and confidence threshold")
    void shouldExecuteLifecycleWithCustomPayload() {
        var req = new OenexaAutonomousIssueHandlingController.LifecycleExecutionRequest("Fix NullPointerException in UserRegistry", 0.95);
        ResponseEntity<AutonomousLifecycleResult> response = controller.executeLifecycle(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(DetectionCategory.ERROR, response.getBody().detect().category());
        assertTrue(response.getBody().analyze().rootCause().contains("null verification"));
        assertTrue(response.getBody().resolve().validated());
    }

    @Test
    @DisplayName("Should return telemetry metrics successfully")
    void shouldReturnTelemetry() {
        ResponseEntity<Map<String, Object>> response = controller.getTelemetry();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("processedCount"));
        assertTrue(body.containsKey("repeatedErrorsPrevented"));
        assertTrue(body.containsKey("averageLatencyMs"));
        assertTrue(body.containsKey("registeredPatternsCount"));
        assertEquals("ONLINE_SELF_IMPROVING", body.get("status"));
    }

    @Test
    @DisplayName("Should record feedback and update pattern outcome")
    void shouldRecordFeedback() {
        // Null request should return 400 Bad Request
        ResponseEntity<ReasoningPattern> badResp = controller.recordFeedback(null);
        assertEquals(HttpStatus.BAD_REQUEST, badResp.getStatusCode());

        // Null patternId should return 400 Bad Request
        ResponseEntity<ReasoningPattern> badResp2 = controller.recordFeedback(new OenexaAutonomousIssueHandlingController.FeedbackRequest(null, true));
        assertEquals(HttpStatus.BAD_REQUEST, badResp2.getStatusCode());

        // Valid pattern feedback
        var req = new OenexaAutonomousIssueHandlingController.FeedbackRequest("PAT-ZERO-MOCK", true);
        ResponseEntity<ReasoningPattern> response = controller.recordFeedback(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PAT-ZERO-MOCK", response.getBody().patternId());
        assertTrue(response.getBody().successCount() > 0);
    }
}
