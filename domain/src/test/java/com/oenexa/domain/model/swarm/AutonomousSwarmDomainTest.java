package com.oenexa.domain.model.swarm;

import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmExecutionPlan;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmExecutionRequest;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmPersona;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmStrategy;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmTaskNode;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmTaskStatus;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmTelemetryRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AutonomousSwarmDomain Zero-Mock Unit Tests")
class AutonomousSwarmDomainTest {

    @Test
    @DisplayName("Should instantiate SwarmTaskNode and support state progression")
    void testSwarmTaskNodeLifecycle() {
        SwarmTaskNode task = new SwarmTaskNode(
                "task-101",
                SwarmPersona.SOFTWARE_ENGINEER,
                "Synthesize Service",
                "Generate Hexagonal service class",
                List.of("task-100"),
                1,
                SwarmTaskStatus.QUEUED,
                "",
                0L
        );

        assertEquals("task-101", task.taskId());
        assertEquals(SwarmPersona.SOFTWARE_ENGINEER, task.persona());
        assertEquals(SwarmTaskStatus.QUEUED, task.status());
        assertEquals(1, task.dependencies().size());

        SwarmTaskNode updated = task.withStatus(SwarmTaskStatus.COMPLETED, "public class OrderService {}", 125L);
        assertEquals(SwarmTaskStatus.COMPLETED, updated.status());
        assertEquals("public class OrderService {}", updated.outputArtifact());
        assertEquals(125L, updated.durationMs());
    }

    @Test
    @DisplayName("Should apply defaults and immutability to SwarmExecutionRequest")
    void testSwarmExecutionRequestDefaults() {
        SwarmExecutionRequest req = new SwarmExecutionRequest(
                "Implement Enterprise CQRS Order System",
                null,
                null,
                null,
                null
        );

        assertEquals("Implement Enterprise CQRS Order System", req.goal());
        assertEquals(SwarmStrategy.PHASED_PIPELINE, req.strategy());
        assertEquals(3, req.requiredPersonas().size());
        assertEquals("oenexa-ai-core", req.contextRepository());
        assertTrue(req.parameters().isEmpty());

        assertThrows(NullPointerException.class, () -> new SwarmExecutionRequest(null, null, null, null, null));
    }

    @Test
    @DisplayName("Should construct SwarmExecutionPlan and calculate metrics")
    void testSwarmExecutionPlan() {
        SwarmTaskNode task1 = new SwarmTaskNode("t-1", SwarmPersona.SYSTEM_ARCHITECT, "C4 Architecture", "Design C4", List.of(), 0, SwarmTaskStatus.COMPLETED, "context.puml", 80L);
        SwarmTaskNode task2 = new SwarmTaskNode("t-2", SwarmPersona.SOFTWARE_ENGINEER, "Core Service", "Write service", List.of("t-1"), 1, SwarmTaskStatus.COMPLETED, "Service.java", 120L);

        SwarmExecutionPlan plan = new SwarmExecutionPlan(
                "plan-99",
                "Microservices Migration",
                SwarmStrategy.PARALLEL_MAX_SPEED,
                List.of(task1, task2),
                SwarmTaskStatus.COMPLETED,
                Instant.now(),
                Instant.now().plusMillis(130),
                4.8,
                2,
                130L,
                200L,
                Map.of("context.puml", "@startuml ...", "Service.java", "public class ...")
        );

        assertEquals("plan-99", plan.planId());
        assertEquals(2, plan.tasks().size());
        assertEquals(SwarmTaskStatus.COMPLETED, plan.overallStatus());
        assertEquals(4.8, plan.speedupFactor());
        assertEquals(2, plan.activeVirtualThreads());
        assertEquals(2, plan.synthesizedArtifacts().size());
    }

    @Test
    @DisplayName("Should validate SwarmTelemetryRecord")
    void testSwarmTelemetryRecord() {
        SwarmTelemetryRecord telemetry = new SwarmTelemetryRecord("plan-99", 6, 2560.5, 5.2, 5, 5);
        assertEquals("plan-99", telemetry.planId());
        assertEquals(6, telemetry.activeThreads());
        assertEquals(2560.5, telemetry.heapMemoryKb());
        assertEquals(5.2, telemetry.concurrencySpeedup());
        assertEquals(5, telemetry.completedTasks());
        assertEquals(5, telemetry.totalTasks());
    }

    @Test
    @DisplayName("Should verify persona roles and capabilities")
    void testSwarmPersonaAttributes() {
        for (SwarmPersona persona : SwarmPersona.values()) {
            assertNotNull(persona.roleId());
            assertNotNull(persona.capability());
            assertFalse(persona.roleId().isBlank());
        }
    }
}
