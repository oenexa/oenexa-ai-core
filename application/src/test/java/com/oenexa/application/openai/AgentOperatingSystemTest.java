package com.oenexa.application.openai;

import com.oenexa.domain.OpenAiDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AgentOperatingSystem Zero-Mock Unit Tests")
class AgentOperatingSystemTest {

    private final AgentOperatingSystem agentOs = new AgentOperatingSystem();

    @Test
    @DisplayName("Should execute autonomous goal across all 10 specialized agent personas")
    void testExecuteGoalAllPersonas() {
        AgentOsGoalRequest request = new AgentOsGoalRequest(
                "Build reactive payment gateway with zero-mock tests and K8s deployment",
                "repo-payments",
                "principal-architect",
                true,
                "high",
                List.of(AgentPersona.values())
        );

        AgentOsGoalResponse response = agentOs.executeGoal(request);

        assertNotNull(response);
        assertNotNull(response.executionId());
        assertEquals("COMPLETED", response.status());
        assertEquals(10, response.executedSteps().size());
        assertTrue(response.synthesizedArtifacts().size() >= 3);

        // Verify metrics
        var metrics = response.metrics();
        assertTrue(metrics.airGappedVerified());
        assertTrue(metrics.zeroMockTestVerified());
        assertTrue(metrics.confidenceScore() >= 0.99);
        assertTrue(metrics.tokensGenerated() > 1000);

        // Verify QA and DevOps tool outputs
        assertTrue(response.executedSteps().stream().anyMatch(s -> s.persona() == AgentPersona.QA_AGENT && s.outputSnippet().contains("Zero-Mock Test Synthesis")));
        assertTrue(response.executedSteps().stream().anyMatch(s -> s.persona() == AgentPersona.DEVOPS_AGENT && s.outputSnippet().contains("Multi-stage Dockerfile")));
    }

    @Test
    @DisplayName("Should execute autonomous goal with subset of personas and default configs")
    void testExecuteGoalSubsetPersonas() {
        AgentOsGoalRequest request = new AgentOsGoalRequest(
                "Perform architectural audit",
                null,
                null,
                false,
                "low",
                List.of(AgentPersona.ARCHITECT_AGENT, AgentPersona.SECURITY_AGENT)
        );

        AgentOsGoalResponse response = agentOs.executeGoal(request);
        assertEquals(2, response.executedSteps().size());
        assertEquals(AgentPersona.ARCHITECT_AGENT, response.executedSteps().get(0).persona());
        assertEquals(AgentPersona.SECURITY_AGENT, response.executedSteps().get(1).persona());
    }

    @Test
    @DisplayName("Should throw NullPointerException for null inputs and validate getters")
    void testNullGuardsAndGetters() {
        assertThrows(NullPointerException.class, () -> agentOs.executeGoal(null));
        assertThrows(NullPointerException.class, () -> new AgentOperatingSystem(null, new OpenAiToolExecutionEngine()));
        assertThrows(NullPointerException.class, () -> new AgentOperatingSystem(new DeliberativeReasoningEngine(), null));

        assertNotNull(agentOs.getDeliberativeReasoningEngine());
        assertNotNull(agentOs.getToolExecutionEngine());
    }
}
