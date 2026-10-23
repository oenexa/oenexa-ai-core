package com.oenexa.api;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.application.core.OenexaAiGatewayEngine;
import com.oenexa.application.core.OenexaCoreBrain;
import com.oenexa.application.core.OenexaObservabilityEngine;
import com.oenexa.application.core.OenexaSecurityEngine;
import com.oenexa.domain.OenexaCoreBrainDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaCoreBrainController Zero-Mock Unit Tests")
class OenexaCoreBrainControllerTest {

    private OenexaCoreBrain coreBrain;
    private OenexaAiGatewayEngine gatewayEngine;
    private OenexaSecurityEngine securityEngine;
    private OenexaObservabilityEngine observabilityEngine;
    private OenexaCoreBrainController controller;

    @BeforeEach
    void setUp() {
        securityEngine = new OenexaSecurityEngine();
        gatewayEngine = new OenexaAiGatewayEngine(securityEngine);
        observabilityEngine = new OenexaObservabilityEngine();
        coreBrain = new OenexaCoreBrain();
        controller = new OenexaCoreBrainController(coreBrain, gatewayEngine, securityEngine, observabilityEngine);
    }

    @Test
    @DisplayName("Should enforce null safety checks on constructor")
    void testConstructorInvariants() {
        assertThrows(NullPointerException.class, () -> new OenexaCoreBrainController(null, gatewayEngine, securityEngine, observabilityEngine));
        assertThrows(NullPointerException.class, () -> new OenexaCoreBrainController(coreBrain, null, securityEngine, observabilityEngine));
        assertThrows(NullPointerException.class, () -> new OenexaCoreBrainController(coreBrain, gatewayEngine, null, observabilityEngine));
        assertThrows(NullPointerException.class, () -> new OenexaCoreBrainController(coreBrain, gatewayEngine, securityEngine, null));
    }

    @Test
    @DisplayName("Should execute directive and return 200 OK with complete synthesis")
    void shouldExecuteDirectiveSuccessfully() {
        var directive = CoreBrainDirective.of("Architect microservices event bus");

        ResponseEntity<CoreBrainSynthesis> response = controller.executeDirective(directive);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("COMPLETED", response.getBody().status());
        assertEquals("oenexa-neo-gpt-o3-max", response.getBody().routingDecision().selectedModel());
    }

    @Test
    @DisplayName("Should reject invalid or null directive with 400 Bad Request")
    void shouldRejectInvalidDirectives() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeDirective(null).getStatusCode());
        assertThrows(IllegalArgumentException.class, () -> CoreBrainDirective.of("   "));
    }

    @Test
    @DisplayName("Should return all 12 core modules status with 200 OK")
    void shouldReturnCoreModulesStatus() {
        ResponseEntity<List<CoreModuleInfo>> response = controller.getModulesStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(12, response.getBody().size());
    }

    @Test
    @DisplayName("Should evaluate gateway route and return 200 OK")
    void shouldEvaluateGatewayRoute() {
        var directive = CoreBrainDirective.of("Route to sovereign LLM");
        ResponseEntity<GatewayRoutingDecision> response = controller.evaluateRoute(directive);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().securityCleared());

        assertEquals(HttpStatus.BAD_REQUEST, controller.evaluateRoute(null).getStatusCode());
    }

    @Test
    @DisplayName("Should return telemetry summary and security audit trails")
    void shouldReturnTelemetryAndSecurityAudit() {
        ResponseEntity<Map<String, Object>> telemetry = controller.getTelemetrySummary();
        assertEquals(HttpStatus.OK, telemetry.getStatusCode());
        assertNotNull(telemetry.getBody());
        assertEquals("HEALTHY", telemetry.getBody().get("systemStatus"));

        ResponseEntity<List<String>> audit = controller.getSecurityAuditTrail("default");
        assertEquals(HttpStatus.OK, audit.getStatusCode());
        assertNotNull(audit.getBody());
    }
}
