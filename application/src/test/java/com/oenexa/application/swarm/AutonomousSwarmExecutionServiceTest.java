package com.oenexa.application.swarm;

import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmExecutionPlan;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmExecutionRequest;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmPersona;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmStrategy;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmTaskStatus;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmTelemetryRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AutonomousSwarmExecutionService Zero-Mock Unit Tests")
class AutonomousSwarmExecutionServiceTest {

    private AutonomousSwarmExecutionService service;

    @BeforeEach
    void setUp() {
        service = new AutonomousSwarmExecutionService();
    }

    @Test
    @DisplayName("Should execute phased pipeline swarm plan and synthesize all artifacts")
    void testExecutePhasedPipeline() {
        SwarmExecutionRequest request = new SwarmExecutionRequest(
                "Build Enterprise CQRS Ledger",
                SwarmStrategy.PHASED_PIPELINE,
                List.of(
                        SwarmPersona.SYSTEM_ARCHITECT,
                        SwarmPersona.SOFTWARE_ENGINEER,
                        SwarmPersona.SECURITY_AUDITOR,
                        SwarmPersona.QA_VERIFICATION,
                        SwarmPersona.DEVOPS_ENGINEER
                ),
                "oenexa-core",
                Map.of("env", "staging")
        );

        SwarmExecutionPlan plan = service.executeSwarmPlan(request);

        assertNotNull(plan);
        assertTrue(plan.planId().startsWith("swarm-plan-"));
        assertEquals(SwarmTaskStatus.COMPLETED, plan.overallStatus());
        assertEquals(5, plan.tasks().size());
        assertEquals(5, plan.synthesizedArtifacts().size());
        assertTrue(plan.speedupFactor() >= 3.8);

        // Verify retrieval
        Optional<SwarmExecutionPlan> retrieved = service.getSwarmPlan(plan.planId());
        assertTrue(retrieved.isPresent());
        assertEquals(plan.planId(), retrieved.get().planId());

        // Verify telemetry
        Optional<SwarmTelemetryRecord> telemetry = service.getSwarmTelemetry(plan.planId());
        assertTrue(telemetry.isPresent());
        assertEquals(5, telemetry.get().completedTasks());
        assertEquals(5, telemetry.get().totalTasks());
    }

    @Test
    @DisplayName("Should execute parallel max speed swarm plan with virtual threads")
    void testExecuteParallelMaxSpeed() {
        SwarmExecutionRequest request = new SwarmExecutionRequest(
                "Rapid Security Audit",
                SwarmStrategy.PARALLEL_MAX_SPEED,
                List.of(SwarmPersona.SECURITY_AUDITOR, SwarmPersona.QA_VERIFICATION),
                "oenexa-wallet",
                Map.of()
        );

        SwarmExecutionPlan plan = service.executeSwarmPlan(request);

        assertNotNull(plan);
        assertEquals(SwarmTaskStatus.COMPLETED, plan.overallStatus());
        assertEquals(2, plan.tasks().size());
        for (var task : plan.tasks()) {
            assertEquals(0, task.stage());
            assertEquals(SwarmTaskStatus.COMPLETED, task.status());
            assertFalse(task.outputArtifact().isBlank());
        }
    }

    @Test
    @DisplayName("Should reject null execution request")
    void testRejectNullRequest() {
        assertThrows(NullPointerException.class, () -> service.executeSwarmPlan(null));
    }
}
