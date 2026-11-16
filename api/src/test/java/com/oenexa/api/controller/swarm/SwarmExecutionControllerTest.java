package com.oenexa.api.controller.swarm;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.swarm.SwarmDtos.ExecuteSwarmRequestDto;
import com.oenexa.api.dto.swarm.SwarmDtos.SwarmExecutionPlanResponseDto;
import com.oenexa.api.dto.swarm.SwarmDtos.SwarmTelemetryResponseDto;
import com.oenexa.application.swarm.AutonomousSwarmExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SwarmExecutionController Zero-Mock Unit Tests")
class SwarmExecutionControllerTest {

    private SwarmExecutionController controller;
    private AutonomousSwarmExecutionService service;

    @BeforeEach
    void setUp() {
        service = new AutonomousSwarmExecutionService();
        controller = new SwarmExecutionController(service);
    }

    @Test
    @DisplayName("Should execute swarm plan and return 200 OK with synthesized artifacts")
    void testExecuteSwarmPlan() {
        ExecuteSwarmRequestDto requestDto = new ExecuteSwarmRequestDto(
                "Build Post-Quantum Encryption Gateway",
                "PHASED_PIPELINE",
                List.of("SYSTEM_ARCHITECT", "SOFTWARE_ENGINEER", "SECURITY_AUDITOR"),
                "oenexa-security",
                Map.of("crypto", "ML-DSA-65")
        );

        ResponseEntity<ApiResponse<SwarmExecutionPlanResponseDto>> response = controller.executeSwarmPlan(requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());

        SwarmExecutionPlanResponseDto plan = response.getBody().data();
        assertNotNull(plan);
        assertTrue(plan.planId().startsWith("swarm-plan-"));
        assertEquals("COMPLETED", plan.overallStatus());
        assertEquals(3, plan.tasks().size());
        assertEquals(3, plan.synthesizedArtifacts().size());
        assertTrue(plan.speedupFactor() > 1.0);
    }

    @Test
    @DisplayName("Should retrieve existing plan status and return 404 for missing plan")
    void testGetPlanStatus() {
        ExecuteSwarmRequestDto requestDto = new ExecuteSwarmRequestDto(
                "Micro-service Migration",
                "PARALLEL_MAX_SPEED",
                List.of("SOFTWARE_ENGINEER"),
                "oenexa-node",
                Map.of()
        );

        ResponseEntity<ApiResponse<SwarmExecutionPlanResponseDto>> createResponse = controller.executeSwarmPlan(requestDto);
        String planId = createResponse.getBody().data().planId();

        // 200 OK for existing
        ResponseEntity<ApiResponse<SwarmExecutionPlanResponseDto>> statusResponse = controller.getPlanStatus(planId);
        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
        assertEquals(planId, statusResponse.getBody().data().planId());

        // 404 Not Found for missing
        ResponseEntity<ApiResponse<SwarmExecutionPlanResponseDto>> missingResponse = controller.getPlanStatus("unknown-plan-999");
        assertEquals(HttpStatus.NOT_FOUND, missingResponse.getStatusCode());
        assertFalse(missingResponse.getBody().success());
    }

    @Test
    @DisplayName("Should retrieve existing plan telemetry and return 404 for missing telemetry")
    void testGetPlanTelemetry() {
        ExecuteSwarmRequestDto requestDto = new ExecuteSwarmRequestDto(
                "Telemetry Benchmark",
                "PARALLEL_MAX_SPEED",
                List.of("QA_VERIFICATION"),
                "oenexa-ai-core",
                Map.of()
        );

        ResponseEntity<ApiResponse<SwarmExecutionPlanResponseDto>> createResponse = controller.executeSwarmPlan(requestDto);
        String planId = createResponse.getBody().data().planId();

        // 200 OK
        ResponseEntity<ApiResponse<SwarmTelemetryResponseDto>> telemetryResponse = controller.getPlanTelemetry(planId);
        assertEquals(HttpStatus.OK, telemetryResponse.getStatusCode());
        assertEquals(planId, telemetryResponse.getBody().data().planId());
        assertEquals(1, telemetryResponse.getBody().data().completedTasks());

        // 404 Not Found
        ResponseEntity<ApiResponse<SwarmTelemetryResponseDto>> missingResponse = controller.getPlanTelemetry("missing-id");
        assertEquals(HttpStatus.NOT_FOUND, missingResponse.getStatusCode());
    }
}
