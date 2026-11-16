package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.gateway.AiGatewayDtos.*;
import com.oenexa.application.gateway.AiGatewayService;
import com.oenexa.application.gateway.PromptGovernancePolicy;
import com.oenexa.application.service.orchestration.EnterpriseMultiModelOrchestrator;
import com.oenexa.domain.model.gateway.AiGatewayDomain.PolicyAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiGatewayController Zero-Mock Unit Tests")
class AiGatewayControllerTest {

    private AiGatewayController controller;
    private AiGatewayService gatewayService;

    @BeforeEach
    void setUp() {
        PromptGovernancePolicy promptGovernance = new PromptGovernancePolicy();
        EnterpriseMultiModelOrchestrator modelOrchestrator = new EnterpriseMultiModelOrchestrator();
        gatewayService = new AiGatewayService(promptGovernance, modelOrchestrator);
        controller = new AiGatewayController(gatewayService);
    }

    @Test
    @DisplayName("Should evaluate and route directive via REST endpoint")
    void shouldEvaluateAndRouteDirective() {
        EvaluateGatewayRequest request = new EvaluateGatewayRequest(
                "tenant-finance",
                "Explain Java 25 scoped values",
                "gemini-2.5-flash",
                "SPEED_OPTIMIZED"
        );

        ResponseEntity<ApiResponse<GatewayEvaluationResponse>> response = controller.evaluateDirective("tenant-finance", request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());

        GatewayEvaluationResponse data = response.getBody().data();
        assertNotNull(data);
        assertEquals("tenant-finance", data.tenantId());
        assertEquals("gemini-2.5-flash", data.requestedModel());
        assertEquals(PolicyAction.ALLOW, data.action());
    }

    @Test
    @DisplayName("Should retrieve and update tenant gateway policy")
    void shouldRetrieveAndUpdatePolicy() {
        // 1. Get default policy
        ResponseEntity<ApiResponse<GatewayPolicyResponse>> getResp = controller.getPolicy("tenant-cloud");
        assertNotNull(getResp.getBody());
        GatewayPolicyResponse defaultPol = getResp.getBody().data();
        assertEquals(120, defaultPol.requestsPerMinuteLimit());

        // 2. Update policy
        UpdatePolicyRequest updateReq = new UpdatePolicyRequest(
                250,
                500_000,
                1500.0,
                true,
                List.of("gemini-2.5-flash", "claude-3-5-sonnet")
        );

        ResponseEntity<ApiResponse<GatewayPolicyResponse>> putResp = controller.updatePolicy("tenant-cloud", updateReq);
        assertNotNull(putResp.getBody());
        GatewayPolicyResponse updatedPol = putResp.getBody().data();

        assertEquals(250, updatedPol.requestsPerMinuteLimit());
        assertEquals(500_000, updatedPol.tokensPerMinuteLimit());
        assertEquals(1500.0, updatedPol.monthlyBudgetUsd());
        assertEquals(2, updatedPol.approvedModels().size());
    }

    @Test
    @DisplayName("Should list all available models through gateway")
    void shouldListAvailableModels() {
        var response = controller.listModels();
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertFalse(response.getBody().data().isEmpty());
    }
}
