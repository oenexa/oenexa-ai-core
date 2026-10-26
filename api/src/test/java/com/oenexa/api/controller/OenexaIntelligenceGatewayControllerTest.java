package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.application.intelligence.OenexaIntelligenceCoreEngines.*;
import com.oenexa.domain.model.intelligence.OenexaIntelligenceDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaIntelligenceGatewayController Zero-Mock Integration Tests")
class OenexaIntelligenceGatewayControllerTest {

    private OenexaIntelligenceGatewayController controller;

    @BeforeEach
    void setUp() {
        var contextEngine = new ContextEngine();
        var reasoningEngine = new ReasoningEngine();
        var memoryEngine = new MemoryEngine();
        var knowledgeEngine = new KnowledgeEngine();
        var taskEngine = new TaskEngine();
        var workflowEngine = new WorkflowEngine();
        var projectEngine = new ProjectEngine();
        var communicationEngine = new CommunicationEngine();
        var securityEngine = new SecurityEngine();
        var monitoringEngine = new MonitoringEngine();
        var learningEngine = new LearningEngine();

        var intelligenceEngine = new IntelligenceEngine(
                contextEngine,
                reasoningEngine,
                memoryEngine,
                knowledgeEngine,
                taskEngine,
                workflowEngine,
                projectEngine,
                communicationEngine,
                securityEngine,
                monitoringEngine,
                learningEngine
        );

        controller = new OenexaIntelligenceGatewayController(
                intelligenceEngine,
                projectEngine,
                memoryEngine,
                monitoringEngine
        );
    }

    @Test
    @DisplayName("Should process directive through 7-step OENEXA loop and return direct answer first")
    void shouldProcessDirective() {
        var request = new OenexaIntelligenceGatewayController.ProcessDirectiveRequest(
                "Fix NullPointerException in User registration",
                "oenexa-ai-core",
                "OENEXA-CNE-32B",
                List.of("UserService.java")
        );

        ResponseEntity<ApiResponse<OenexaIntelligenceResponse>> response = controller.processDirective("engineer-1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());

        OenexaIntelligenceResponse data = response.getBody().data();
        assertTrue(data.directAnswer().startsWith("Direct Answer:"));
        assertTrue(data.validationReport().compliant());
        assertEquals(100, data.validationReport().invariantScore());
        assertFalse(data.artifacts().isEmpty());
    }

    @Test
    @DisplayName("Should execute task via Oenexa Task Engine")
    void shouldExecuteTask() {
        var request = new OenexaIntelligenceGatewayController.ExecuteTaskRequest(
                "OENEXA-TaskEngine-v1",
                "./gradlew test"
        );

        ResponseEntity<ApiResponse<OenexaTaskExecution>> response = controller.executeTask(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertTrue(response.getBody().data().isSuccessful());
    }

    @Test
    @DisplayName("Should retrieve registered OENEXA workspaces")
    void shouldListWorkspaces() {
        ResponseEntity<ApiResponse<List<String>>> response = controller.listWorkspaces();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().data().contains("oenexa-ai-core"));
    }

    @Test
    @DisplayName("Should retrieve system health telemetry")
    void shouldGetHealth() {
        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.getSystemHealth();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("HEALTHY", response.getBody().data().get("status"));
        assertEquals("v3.0.0-SOVEREIGN", response.getBody().data().get("version"));
    }
}
