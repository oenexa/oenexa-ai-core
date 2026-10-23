package com.oenexa.api;

import com.oenexa.application.openai.AgentOperatingSystem;
import com.oenexa.domain.OpenAiAgentOsDomain.*;
import com.oenexa.domain.OpenAiDomain.*;
import com.oenexa.domain.OpenAiReasoningDomain.*;
import com.oenexa.domain.OpenAiToolProtocolDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenAiAutonomousAgentController Zero-Mock Unit Tests")
class OpenAiAutonomousAgentControllerTest {

    private final AgentOperatingSystem agentOs = new AgentOperatingSystem();
    private final OpenAiAutonomousAgentController controller = new OpenAiAutonomousAgentController(agentOs);

    @Test
    @DisplayName("Should execute goal and return complete Agent OS response")
    void testExecuteGoal() {
        AgentOsGoalRequest req = new AgentOsGoalRequest(
                "Refactor monolith into reactive hexagonal services",
                "repo-core",
                "lead-engineer",
                true,
                "high",
                List.of(AgentPersona.ARCHITECT_AGENT, AgentPersona.BACKEND_AGENT, AgentPersona.QA_AGENT)
        );

        var resp = controller.executeGoal(req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("COMPLETED", resp.getBody().status());
        assertEquals(3, resp.getBody().executedSteps().size());
        assertTrue(resp.getBody().metrics().airGappedVerified());
    }

    @Test
    @DisplayName("Should return BAD_REQUEST when goal request is invalid")
    void testInvalidGoalRequests() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeGoal(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeGoal(new AgentOsGoalRequest("", null, null, true, null, null)).getStatusCode());
    }

    @Test
    @DisplayName("Should list supported models and return engine health")
    void testModelsAndHealth() {
        var modelsResp = controller.listSupportedModels();
        assertEquals(HttpStatus.OK, modelsResp.getStatusCode());
        assertTrue(modelsResp.getBody().size() >= 5);

        var healthResp = controller.getEngineHealth();
        assertEquals(HttpStatus.OK, healthResp.getStatusCode());
        assertEquals("UP", healthResp.getBody().get("status"));
        assertEquals(10, healthResp.getBody().get("supportedPersonas"));
        assertTrue((Boolean) healthResp.getBody().get("zeroMockEnforced"));
    }

    @Test
    @DisplayName("Should throw NullPointerException for null constructor dependencies")
    void testNullGuard() {
        assertThrows(NullPointerException.class, () -> new OpenAiAutonomousAgentController(null));
    }
}
