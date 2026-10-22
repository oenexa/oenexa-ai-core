package com.oenexa.domain;

import com.oenexa.domain.AutonomousAgentEcosystemDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AutonomousAgentEcosystemDomain Zero-Mock Unit Tests")
class AutonomousAgentEcosystemDomainTest {

    @Test
    @DisplayName("Should verify all Enterprise Agent types exist")
    void shouldVerifyEightAgentTypes() {
        assertEquals(12, EnterpriseAgentType.values().length);
        assertNotNull(EnterpriseAgentType.valueOf("SOFTWARE_ENGINEER"));
        assertNotNull(EnterpriseAgentType.valueOf("ARCHITECT"));
        assertNotNull(EnterpriseAgentType.valueOf("DEVOPS"));
        assertNotNull(EnterpriseAgentType.valueOf("SECURITY"));
        assertNotNull(EnterpriseAgentType.valueOf("RESEARCH"));
        assertNotNull(EnterpriseAgentType.valueOf("PRODUCT_MANAGER"));
        assertNotNull(EnterpriseAgentType.valueOf("DATA_ENGINEER"));
        assertNotNull(EnterpriseAgentType.valueOf("QA"));
        assertNotNull(EnterpriseAgentType.valueOf("FINANCE"));
        assertNotNull(EnterpriseAgentType.valueOf("CUSTOMER_SUPPORT"));
        assertNotNull(EnterpriseAgentType.valueOf("MARKETING"));
        assertNotNull(EnterpriseAgentType.valueOf("LEGAL_ASSISTANT"));
    }

    @Test
    @DisplayName("Should validate AgentProfile contracts")
    void shouldValidateAgentProfile() {
        var profile = new AgentProfile(
                EnterpriseAgentType.FINANCE,
                "Finance & Cost Agent",
                "Monitors token usage, cloud infrastructure billing, and budget ROI",
                List.of("Token Billing", "ROI Forecasting", "Cost Optimization"),
                List.of("cost_calculator", "billing_analyzer"),
                "deepseek-v3"
        );

        assertEquals(EnterpriseAgentType.FINANCE, profile.type());
        assertEquals("Finance & Cost Agent", profile.displayName());
        assertEquals(2, profile.toolsAvailable().size());
    }

    @Test
    @DisplayName("Should validate AgentGoalExecutionPlan contracts")
    void shouldValidateExecutionPlan() {
        var reflection = new AgentReflectionResult("s-1", true, 98, "Code meets all invariants", "None");
        var step = new AgentPlanStep("s-1", EnterpriseAgentType.SOFTWARE_ENGINEER, "Implement sealed records", "code_generator", ExecutionStatus.SUCCESS, "Code output", reflection, 0);
        var collab = new AgentCollaborationEvent("e-1", EnterpriseAgentType.RESEARCH, EnterpriseAgentType.SOFTWARE_ENGINEER, "Handoff paper findings", null);

        var plan = new AgentGoalExecutionPlan(
                "p-1",
                "Build Event Stream Engine",
                "High throughput streaming",
                "EXECUTION",
                List.of(step),
                List.of(collab),
                List.of("Learned user prefers zero-mock JUnit 5"),
                true,
                null,
                null
        );

        assertEquals("p-1", plan.planId());
        assertTrue(plan.completed());
        assertEquals(1, plan.steps().size());
        assertEquals(1, plan.collaborationLog().size());
        assertEquals(1, plan.learnedMemories().size());
    }

    @Test
    @DisplayName("Should validate AgentThreadExecutionRecord and ConcurrentAgentSwarmResult")
    void shouldValidateConcurrentAgentSwarmRecords() {
        var threadRecord = new AgentThreadExecutionRecord(
                "vthread-101",
                "oenexa-worker-ARCHITECT-1",
                EnterpriseAgentType.ARCHITECT,
                "Model C4 system boundaries in parallel",
                AgentThreadState.COMPLETED,
                System.currentTimeMillis(),
                32L,
                "C4 model synthesized with zero circular dependencies",
                512
        );

        assertEquals("vthread-101", threadRecord.threadId());
        assertEquals(EnterpriseAgentType.ARCHITECT, threadRecord.agentType());
        assertEquals(AgentThreadState.COMPLETED, threadRecord.state());
        assertEquals(32L, threadRecord.executionDurationMs());

        var swarmResult = new ConcurrentAgentSwarmResult(
                "swarm-999",
                "Build Resilient E-Commerce Platform",
                "JAVA_25_STRUCTURED_VIRTUAL_THREADS",
                6,
                85L,
                480L,
                5.65,
                List.of(threadRecord),
                List.of("OrderService.java", "Architecture.puml"),
                true
        );

        assertEquals("swarm-999", swarmResult.swarmId());
        assertEquals(6, swarmResult.activeThreadCount());
        assertEquals(85L, swarmResult.wallClockDurationMs());
        assertEquals(480L, swarmResult.sequentialDurationMs());
        assertTrue(swarmResult.speedupFactor() > 5.0);
        assertTrue(swarmResult.allPassed());
    }
}
