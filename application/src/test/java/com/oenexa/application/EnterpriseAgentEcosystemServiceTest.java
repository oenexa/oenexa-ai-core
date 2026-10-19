package com.oenexa.application;

import com.oenexa.domain.AutonomousAgentEcosystemDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseAgentEcosystemService Zero-Mock Unit Tests")
class EnterpriseAgentEcosystemServiceTest {

    private EnterpriseAgentEcosystemService service;

    @BeforeEach
    void setUp() {
        service = new EnterpriseAgentEcosystemService();
    }

    @Test
    @DisplayName("Should list all 8 enterprise agent profiles")
    void shouldListAllEightAgents() {
        List<AgentProfile> agents = service.listAgents();
        assertEquals(8, agents.size());
        assertTrue(agents.stream().anyMatch(a -> a.type() == EnterpriseAgentType.SOFTWARE_ENGINEER));
        assertTrue(agents.stream().anyMatch(a -> a.type() == EnterpriseAgentType.DEVOPS));
        assertTrue(agents.stream().anyMatch(a -> a.type() == EnterpriseAgentType.SECURITY));
        assertTrue(agents.stream().anyMatch(a -> a.type() == EnterpriseAgentType.FINANCE));
        assertTrue(agents.stream().anyMatch(a -> a.type() == EnterpriseAgentType.RESEARCH));
        assertTrue(agents.stream().anyMatch(a -> a.type() == EnterpriseAgentType.MARKETING));
        assertTrue(agents.stream().anyMatch(a -> a.type() == EnterpriseAgentType.LEGAL_ASSISTANT));
        assertTrue(agents.stream().anyMatch(a -> a.type() == EnterpriseAgentType.CUSTOMER_SUPPORT));
    }

    @Test
    @DisplayName("Should plan goal across all requested agents")
    void shouldPlanGoalAcrossAgents() {
        AgentGoalExecutionPlan plan = service.planGoal(
                "Build Production SaaS",
                "Deploy scalable cloud microservice with zero mocks",
                null
        );

        assertNotNull(plan);
        assertEquals(8, plan.steps().size());
        assertEquals("PLANNING_COMPLETE", plan.currentPhase());
        assertFalse(plan.completed());
    }

    @Test
    @DisplayName("Should execute multi-step plan with self-reflection, collaboration, and memory")
    void shouldExecutePlanSuccessfully() {
        AgentGoalExecutionPlan initialPlan = service.planGoal("Build Oenexa Core", "Sovereign platform delivery", null);
        AgentGoalExecutionPlan completedPlan = service.executePlan(initialPlan.planId());

        assertNotNull(completedPlan);
        assertTrue(completedPlan.completed());
        assertEquals("COMPLETED", completedPlan.currentPhase());
        assertEquals(8, completedPlan.steps().size());
        assertEquals(7, completedPlan.collaborationLog().size());
        assertEquals(8, completedPlan.learnedMemories().size());

        // Verify each step has self-reflection
        for (AgentPlanStep step : completedPlan.steps()) {
            assertEquals(ExecutionStatus.SUCCESS, step.status());
            assertNotNull(step.reflection());
            assertTrue(step.reflection().passed());
            assertTrue(step.reflection().confidenceScore() >= 90);
        }
    }

    @Test
    @DisplayName("Should reflect on step output and detect mock violations")
    void shouldDetectMockViolationInReflection() {
        AgentReflectionResult passResult = service.reflectOnStep("s-1", "public record User(UUID id) {}");
        assertTrue(passResult.passed());
        assertEquals(98, passResult.confidenceScore());

        AgentReflectionResult failResult = service.reflectOnStep("s-2", "when(mockRepo.find()).thenReturn(user)");
        assertFalse(failResult.passed());
        assertEquals(65, failResult.confidenceScore());
        assertTrue(failResult.critique().contains("violation"));
    }
}
