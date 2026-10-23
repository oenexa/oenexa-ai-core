package com.oenexa.application.service.agent;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.agent.AutonomousAgentEcosystemDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAgentThreadOrchestratorService Zero-Mock Unit Tests")
class OenexaAgentThreadOrchestratorServiceTest {

    private final OenexaAgentThreadOrchestratorService orchestratorService = new OenexaAgentThreadOrchestratorService();

    @Test
    @DisplayName("Should dispatch parallel max speed swarm using virtual threads and achieve parallel speedup")
    void shouldDispatchParallelMaxSpeedSwarm() {
        var request = new OenexaAgentThreadOrchestratorService.SwarmExecutionRequest(
                "Build Distributed Payment Engine",
                "High availability payment processing with zero-mock tests",
                SwarmExecutionStrategy.PARALLEL_MAX_SPEED,
                List.of(
                        EnterpriseAgentType.ARCHITECT,
                        EnterpriseAgentType.SOFTWARE_ENGINEER,
                        EnterpriseAgentType.SECURITY,
                        EnterpriseAgentType.QA,
                        EnterpriseAgentType.DEVOPS,
                        EnterpriseAgentType.DATA_ENGINEER
                )
        );

        ConcurrentAgentSwarmResult result = orchestratorService.dispatchConcurrentSwarm(request);

        assertNotNull(result);
        assertNotNull(result.swarmId());
        assertEquals("Build Distributed Payment Engine", result.goalTitle());
        assertEquals(6, result.activeThreadCount());
        assertTrue(result.allPassed());

        // Concurrency validation
        assertEquals("PARALLEL_MAX_SPEED", result.concurrencyMode());
        assertTrue(result.sequentialDurationMs() >= result.wallClockDurationMs(),
                "Sequential duration should be greater than or equal to parallel wall-clock duration");
        assertTrue(result.speedupFactor() >= 1.0, "Speedup factor should be at least 1.0");

        // Verify thread execution records
        List<AgentThreadExecutionRecord> threads = result.threadRecords();
        assertEquals(6, threads.size());

        for (AgentThreadExecutionRecord tr : threads) {
            assertTrue(tr.threadName().startsWith("oenexa-vthread-"));
            assertEquals(AgentThreadState.COMPLETED, tr.state());
            assertEquals("Parallel Swarm", tr.stageName());
            assertTrue(tr.executionDurationMs() > 0);
            assertFalse(tr.outputResult().isBlank());
            assertTrue(tr.memoryAllocatedKb() > 0);
        }

        // Verify artifacts
        assertFalse(result.consolidatedArtifacts().isEmpty());
        assertTrue(result.consolidatedArtifacts().contains("DomainService.java"));
        assertTrue(result.consolidatedArtifacts().contains("ArchitectureContext.puml"));

        // Query by swarm ID
        var queried = orchestratorService.getSwarmResult(result.swarmId());
        assertTrue(queried.isPresent());
        assertEquals(result.swarmId(), queried.get().swarmId());

        var activeThreads = orchestratorService.getActiveThreads(result.swarmId());
        assertEquals(6, activeThreads.size());
    }

    @Test
    @DisplayName("Should dispatch phased pipeline swarm with dependency ordered stages")
    void shouldDispatchPhasedPipelineSwarm() {
        var request = new OenexaAgentThreadOrchestratorService.SwarmExecutionRequest(
                "Design and Secure High-Throughput Event Streaming",
                "Phased multi-agent execution pipeline",
                SwarmExecutionStrategy.PHASED_PIPELINE,
                List.of(
                        EnterpriseAgentType.ARCHITECT,
                        EnterpriseAgentType.SOFTWARE_ENGINEER,
                        EnterpriseAgentType.SECURITY,
                        EnterpriseAgentType.QA,
                        EnterpriseAgentType.DEVOPS
                )
        );

        ConcurrentAgentSwarmResult result = orchestratorService.dispatchConcurrentSwarm(request);

        assertNotNull(result);
        assertEquals("PHASED_PIPELINE", result.concurrencyMode());
        assertEquals(5, result.activeThreadCount());
        assertTrue(result.allPassed());

        // Verify that different stages are recorded
        boolean hasStage1 = result.threadRecords().stream().anyMatch(t -> t.stageName().contains("Stage 1"));
        boolean hasStage2 = result.threadRecords().stream().anyMatch(t -> t.stageName().contains("Stage 2"));
        boolean hasStage3 = result.threadRecords().stream().anyMatch(t -> t.stageName().contains("Stage 3"));

        assertTrue(hasStage1, "Should contain Stage 1 records");
        assertTrue(hasStage2, "Should contain Stage 2 records");
        assertTrue(hasStage3, "Should contain Stage 3 records");
    }

    @Test
    @DisplayName("Should handle empty agents request with default swarm")
    void shouldHandleDefaultAgentsFallback() {
        var request = new OenexaAgentThreadOrchestratorService.SwarmExecutionRequest(null, null, null);
        ConcurrentAgentSwarmResult result = orchestratorService.dispatchConcurrentSwarm(request);

        assertNotNull(result);
        assertEquals("Autonomous System Delivery", result.goalTitle());
        assertEquals(6, result.activeThreadCount());
    }
}


