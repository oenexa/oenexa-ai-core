package com.oenexa.api;

import com.oenexa.application.EnterpriseAgentEcosystemService;
import com.oenexa.domain.AutonomousAgentEcosystemDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseAgentEcosystemController Zero-Mock Unit Tests")
class EnterpriseAgentEcosystemControllerTest {

    private EnterpriseAgentEcosystemController controller;

    @BeforeEach
    void setUp() {
        controller = new EnterpriseAgentEcosystemController(new EnterpriseAgentEcosystemService());
    }

    @Test
    @DisplayName("Should list all agents via REST endpoint")
    void shouldListAgents() {
        ResponseEntity<List<AgentProfile>> response = controller.listAgents();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(12, response.getBody().size());
    }

    @Test
    @DisplayName("Should plan goal via REST endpoint")
    void shouldPlanGoal() {
        var req = new EnterpriseAgentEcosystemController.PlanGoalRequest("Build Microservices", "Distributed event pipeline", null);
        ResponseEntity<AgentGoalExecutionPlan> response = controller.planGoal(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10, response.getBody().steps().size());
        assertEquals("PLANNING_COMPLETE", response.getBody().currentPhase());
    }

    @Test
    @DisplayName("Should execute plan via REST endpoint")
    void shouldExecutePlan() {
        var plan = controller.planGoal(null).getBody();
        assertNotNull(plan);

        var execReq = new EnterpriseAgentEcosystemController.ExecutePlanRequest(plan.planId());
        ResponseEntity<AgentGoalExecutionPlan> response = controller.executePlan(execReq);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().completed());
        assertEquals("COMPLETED", response.getBody().currentPhase());
    }

    @Test
    @DisplayName("Should perform self-reflection on agent step via REST endpoint")
    void shouldReflectOnStep() {
        var req = new EnterpriseAgentEcosystemController.ReflectRequest("step-1", "public record SafeResult() {}");
        ResponseEntity<AgentReflectionResult> response = controller.reflectOnStep(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().passed());
        assertEquals(98, response.getBody().confidenceScore());
    }
}
