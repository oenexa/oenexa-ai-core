package com.oenexa.application.cognitive;

import com.oenexa.domain.model.agent.OenexaAgentOsDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAgentOperatingSystem Zero-Mock Unit Tests")
class OenexaAgentOperatingSystemTest {

    private OenexaAgentOperatingSystem agentOs;

    @BeforeEach
    void setUp() {
        agentOs = new OenexaAgentOperatingSystem();
    }

    @Test
    @DisplayName("Should execute goal with all 10 default agent personas")
    void shouldExecuteGoalWithAllPersonas() {
        OenexaAgentOsGoalRequest request = new OenexaAgentOsGoalRequest(
                "Build modern microservice with PostgreSQL and Redis caching",
                "repo-test",
                "user-test",
                true,
                "high",
                List.of()
        );

        OenexaAgentOsGoalResponse response = agentOs.executeGoal(request);

        assertNotNull(response);
        assertEquals("Build modern microservice with PostgreSQL and Redis caching", response.goal());
        assertEquals("COMPLETED", response.status());
        assertEquals(10, response.executedSteps().size(), "Should have 10 steps for 10 personas");
        assertNotNull(response.deliberativeTrace());
        assertFalse(response.synthesizedArtifacts().isEmpty(), "Should produce generated artifacts");
        assertTrue(response.metrics().totalDurationMs() >= 0);
    }

    @Test
    @DisplayName("Should execute goal with subset of selected personas")
    void shouldExecuteGoalWithSelectedPersonas() {
        OenexaAgentOsGoalRequest request = new OenexaAgentOsGoalRequest(
                "Audit cryptographic primitives",
                "repo-sec",
                "user-sec",
                true,
                "medium",
                List.of(OenexaAgentPersona.SECURITY_AGENT, OenexaAgentPersona.QA_AGENT)
        );

        OenexaAgentOsGoalResponse response = agentOs.executeGoal(request);

        assertNotNull(response);
        assertEquals(2, response.executedSteps().size());
        assertEquals(OenexaAgentPersona.SECURITY_AGENT, response.executedSteps().get(0).persona());
        assertEquals(OenexaAgentPersona.QA_AGENT, response.executedSteps().get(1).persona());
    }

    @Test
    @DisplayName("Should reject null goal request")
    void shouldRejectNullRequest() {
        assertThrows(NullPointerException.class, () -> agentOs.executeGoal(null));
    }
}

