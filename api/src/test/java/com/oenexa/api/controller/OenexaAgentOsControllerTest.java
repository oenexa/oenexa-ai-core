package com.oenexa.api.controller;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.application.cognitive.OenexaAgentOperatingSystem;
import com.oenexa.domain.model.agent.OenexaAgentOsDomain.*;
import com.oenexa.domain.model.cognitive.OenexaCognitiveDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAgentOsController Zero-Mock Unit Tests")
class OenexaAgentOsControllerTest {

    private final OenexaAgentOperatingSystem os = new OenexaAgentOperatingSystem();
    private final OenexaAgentOsController controller = new OenexaAgentOsController(os);

    @Test
    @DisplayName("Should execute autonomous goal across all 10 personas")
    void testExecuteGoal() {
        var req = new OenexaAgentOsGoalRequest(
                "Build reactive microservice with zero-mock tests",
                "repo-1",
                "developer",
                true,
                "high",
                List.of(OenexaAgentPersona.ARCHITECT_AGENT, OenexaAgentPersona.QA_AGENT)
        );

        var resp = controller.executeGoal(req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("COMPLETED", resp.getBody().status());
        assertEquals(2, resp.getBody().executedSteps().size());
        assertTrue(resp.getBody().metrics().airGappedVerified());
        assertTrue(resp.getBody().metrics().zeroMockTestVerified());
    }

    @Test
    @DisplayName("Should reject invalid or null goal requests")
    void testInvalidRequests() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeGoal(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeGoal(new OenexaAgentOsGoalRequest("   ", null, null, false, null, null)).getStatusCode());
    }

    @Test
    @DisplayName("Should return pure-own sovereign model catalog")
    void testListModels() {
        var resp = controller.listSupportedModels();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertFalse(resp.getBody().isEmpty());
        assertTrue(resp.getBody().stream().anyMatch(m -> m.get("id").equals("oenexa-neo-gpt-o3-max")));
    }

    @Test
    @DisplayName("Should report engine health status UP")
    void testEngineHealth() {
        var resp = controller.getEngineHealth();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("UP", resp.getBody().get("status"));
        assertEquals(10, resp.getBody().get("supportedPersonas"));
    }
}


