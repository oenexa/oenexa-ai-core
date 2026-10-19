package com.oenexa.api;

import com.oenexa.application.EnterpriseMultiModelOrchestrator;
import com.oenexa.domain.ChatDomain.ModelProvider;
import com.oenexa.domain.ChatDomain.TaskType;
import com.oenexa.domain.MultiModelDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MultiModelController Zero-Mock Unit Tests")
class MultiModelControllerTest {

    private MultiModelController controller;
    private EnterpriseMultiModelOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new EnterpriseMultiModelOrchestrator();
        controller = new MultiModelController(orchestrator);
    }

    @Test
    @DisplayName("Should list all models through REST endpoint")
    void shouldListModels() {
        ResponseEntity<List<AiModelSpec>> response = controller.listModels();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should retrieve specific model by id or 404")
    void shouldGetModelById() {
        ResponseEntity<AiModelSpec> okResp = controller.getModel("qwen-2.5-coder-32b");
        assertEquals(HttpStatus.OK, okResp.getStatusCode());
        assertNotNull(okResp.getBody());
        assertEquals("Qwen 2.5 Coder 32B", okResp.getBody().name());

        ResponseEntity<AiModelSpec> notFound = controller.getModel("non-existent-model");
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
    }

    @Test
    @DisplayName("Should route task based on prompt and strategy")
    void shouldRouteTask() {
        var req = new MultiModelController.RouteRequest(
                "Audit code for SQL injection vulnerabilities",
                null,
                OptimizationStrategy.AUTO,
                null
        );
        ResponseEntity<RoutingDecisionResult> response = controller.routeModel(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TaskType.SECURITY_AUDIT, response.getBody().detectedTask());
        assertEquals("oenexa-security-sast-v1", response.getBody().selectedModelId());
    }

    @Test
    @DisplayName("Should execute task with smart fallback failover simulation")
    void shouldExecuteWithFallback() {
        var req = new MultiModelController.ExecuteFallbackRequest(
                "conv-test",
                "Write binary search in Java",
                OptimizationStrategy.AUTO,
                null,
                true,
                false
        );
        ResponseEntity<ExecutionWithFallbackResult> response = controller.executeWithFallback(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(FallbackStatus.SECONDARY_FAILOVER, response.getBody().fallbackStatus());
        assertEquals(1, response.getBody().failoverAttempts());
    }

    @Test
    @DisplayName("Should execute multi-model collaboration endpoint")
    void shouldExecuteMultiModelCollaboration() {
        var req = new MultiModelController.CollaborateRequest("Design multi-tenant event stream", "record Event()");
        ResponseEntity<MultiModelCollaborationTurn> response = controller.collaborate(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().contributingModels().size());
        assertTrue(response.getBody().synthesizedFinalResponse().contains("OENEXA Multi-Model Collaborative Synthesis"));
    }
}
