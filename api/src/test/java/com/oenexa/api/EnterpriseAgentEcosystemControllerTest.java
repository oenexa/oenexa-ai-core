package com.oenexa.api;

import com.oenexa.application.EnterpriseAgentEcosystemService;
import com.oenexa.application.OenexaAgentThreadOrchestratorService;
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
        controller = new EnterpriseAgentEcosystemController(
                new EnterpriseAgentEcosystemService(),
                new OenexaAgentThreadOrchestratorService()
        );
    }

    @Test
    @DisplayName("Should test default no-arg constructor")
    void shouldTestNoArgConstructor() {
        EnterpriseAgentEcosystemController defaultCtrl = new EnterpriseAgentEcosystemController();
        assertNotNull(defaultCtrl);
        assertEquals(12, defaultCtrl.listAgents().getBody().size());
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

        // Null execute request fallback
        assertNotNull(controller.executePlan(null).getBody());
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

        // Null reflect request fallback
        assertNotNull(controller.reflectOnStep(null).getBody());
    }

    @Test
    @DisplayName("Should execute concurrent multi-agent swarm via REST endpoint")
    void shouldExecuteConcurrentSwarm() {
        var req = new OenexaAgentThreadOrchestratorService.SwarmExecutionRequest(
                "Build Distributed Cash Register",
                "Execute multi-agent concurrent tasks",
                List.of(EnterpriseAgentType.ARCHITECT, EnterpriseAgentType.SOFTWARE_ENGINEER, EnterpriseAgentType.SECURITY)
        );

        ResponseEntity<ConcurrentAgentSwarmResult> response = controller.executeConcurrentSwarm(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().activeThreadCount());
        assertTrue(response.getBody().allPassed());

        // Test querying active threads
        ResponseEntity<List<AgentThreadExecutionRecord>> threadResp = controller.getActiveThreads(response.getBody().swarmId());
        assertEquals(HttpStatus.OK, threadResp.getStatusCode());
        assertNotNull(threadResp.getBody());
        assertEquals(3, threadResp.getBody().size());

        // Test querying active threads with null parameter
        assertNotNull(controller.getActiveThreads(null).getBody());

        // Test querying by swarm ID
        ResponseEntity<ConcurrentAgentSwarmResult> getResp = controller.getSwarmResult(response.getBody().swarmId());
        assertEquals(HttpStatus.OK, getResp.getStatusCode());
        assertNotNull(getResp.getBody());

        // Query missing swarm ID
        ResponseEntity<ConcurrentAgentSwarmResult> missingResp = controller.getSwarmResult("missing-id");
        assertEquals(HttpStatus.NOT_FOUND, missingResp.getStatusCode());

        // Null swarm execution request fallback
        assertNotNull(controller.executeConcurrentSwarm(null).getBody());
    }
}
