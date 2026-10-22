package com.oenexa.application;

import com.oenexa.domain.AutonomousAgentEcosystemDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAgentThreadOrchestratorService Zero-Mock Unit Tests")
class OenexaAgentThreadOrchestratorServiceTest {

    private final OenexaAgentThreadOrchestratorService orchestratorService = new OenexaAgentThreadOrchestratorService();

    @Test
    @DisplayName("Should dispatch concurrent agent swarm using virtual threads and achieve parallel speedup")
    void shouldDispatchConcurrentAgentSwarm() {
        var request = new OenexaAgentThreadOrchestratorService.SwarmExecutionRequest(
                "Build Distributed Payment Engine",
                "High availability payment processing with zero-mock tests",
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
        assertEquals("JAVA_25_STRUCTURED_VIRTUAL_THREADS", result.concurrencyMode());
        assertTrue(result.sequentialDurationMs() >= result.wallClockDurationMs(),
                "Sequential duration should be greater than or equal to parallel wall-clock duration");
        assertTrue(result.speedupFactor() >= 1.0, "Speedup factor should be at least 1.0");

        // Verify thread execution records
        List<AgentThreadExecutionRecord> threads = result.threadRecords();
        assertEquals(6, threads.size());

        for (AgentThreadExecutionRecord tr : threads) {
            assertTrue(tr.threadName().startsWith("oenexa-vthread-"));
            assertEquals(AgentThreadState.COMPLETED, tr.state());
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
    @DisplayName("Should handle empty agents request with default swarm")
    void shouldHandleDefaultAgentsFallback() {
        var request = new OenexaAgentThreadOrchestratorService.SwarmExecutionRequest(null, null, null);
        ConcurrentAgentSwarmResult result = orchestratorService.dispatchConcurrentSwarm(request);

        assertNotNull(result);
        assertEquals("Autonomous System Delivery", result.goalTitle());
        assertEquals(6, result.activeThreadCount());
    }
}
