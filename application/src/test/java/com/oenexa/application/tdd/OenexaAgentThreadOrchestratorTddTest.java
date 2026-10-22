package com.oenexa.application.tdd;

import com.oenexa.application.OenexaAgentThreadOrchestratorService;
import com.oenexa.application.OenexaAgentThreadOrchestratorService.SwarmExecutionRequest;
import com.oenexa.domain.AutonomousAgentEcosystemDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Contract Specification for OENEXA Virtual Thread Agent Orchestrator.
 * Verifies thread dispatching, concurrent swarm execution, and worker isolation.
 */
@Tag("tdd")
@DisplayName("OENEXA Agent Thread Orchestrator - TDD Specifications")
public class OenexaAgentThreadOrchestratorTddTest {

    private OenexaAgentThreadOrchestratorService orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new OenexaAgentThreadOrchestratorService();
    }

    @Test
    @DisplayName("TDD-1: Concurrent Swarm Execution Invariant Across Virtual Threads")
    void testConcurrentSwarmExecutionInvariant() {
        // Arrange
        SwarmExecutionRequest request = new SwarmExecutionRequest(
                "Build Distributed Cache Layer",
                "Orchestrate architect, coder, and security auditor simultaneously",
                List.of(
                        EnterpriseAgentType.SOFTWARE_ENGINEER,
                        EnterpriseAgentType.SECURITY,
                        EnterpriseAgentType.QA
                )
        );

        // Act
        ConcurrentAgentSwarmResult result = orchestrator.dispatchConcurrentSwarm(request);

        // Assert
        assertNotNull(result, "Swarm result must not be null");
        assertNotNull(result.swarmId(), "Swarm must be assigned an ID");
        assertEquals(3, result.threadRecords().size(), "Each agent must produce an execution record");
        assertTrue(result.threadRecords().stream()
                        .allMatch(r -> r.state() == AgentThreadState.COMPLETED),
                "All dispatched agent threads must complete successfully");
    }

    @Test
    @DisplayName("TDD-2: Default Agent Swarm Selection Contract")
    void testDefaultSwarmSelectionContract() {
        // Act
        List<EnterpriseAgentType> defaultAgents = SwarmExecutionRequest.getDefaultSwarmAgents();

        // Assert: Red-Green Invariant
        assertNotNull(defaultAgents);
        assertFalse(defaultAgents.isEmpty());
        assertTrue(defaultAgents.contains(EnterpriseAgentType.SOFTWARE_ENGINEER));
        assertTrue(defaultAgents.contains(EnterpriseAgentType.SECURITY));
    }

    @Test
    @DisplayName("TDD-3: Execution History and Swarm Result Retrieval Contract")
    void testSwarmResultRetrievalContract() {
        // Arrange
        SwarmExecutionRequest request = new SwarmExecutionRequest("Perform Static Analysis");
        ConcurrentAgentSwarmResult executed = orchestrator.dispatchConcurrentSwarm(request);

        // Act
        var retrieved = orchestrator.getSwarmResult(executed.swarmId());

        // Assert
        assertTrue(retrieved.isPresent(), "Executed swarm must be retrievable by ID");
        assertEquals(executed.swarmId(), retrieved.get().swarmId());
        assertEquals(executed.goalTitle(), retrieved.get().goalTitle());
    }
}
