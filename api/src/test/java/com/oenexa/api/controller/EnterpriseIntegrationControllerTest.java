package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.integration.IntegrationDtos.*;
import com.oenexa.application.integration.EnterpriseIntegrationService;
import com.oenexa.domain.model.integration.IntegrationDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseIntegrationController Zero-Mock Unit Tests")
class EnterpriseIntegrationControllerTest {

    private EnterpriseIntegrationService service;
    private EnterpriseIntegrationController controller;

    @BeforeEach
    void setUp() {
        service = new EnterpriseIntegrationService();
        controller = new EnterpriseIntegrationController(service);
    }

    @Test
    @DisplayName("Should process GitHub webhook and return 200 OK with processing action")
    void testProcessWebhookEndpoint() {
        WebhookPayloadDto payloadDto = new WebhookPayloadDto(
                "DEL-GH-77",
                WebhookEventType.PULL_REQUEST,
                "oenexa/oenexa-node",
                "feat/pqc-signatures",
                "lead-crypto-eng",
                Map.of("prTitle", "Implement ML-DSA-65 signatures")
        );

        ResponseEntity<ApiResponse<WebhookProcessingResultDto>> response = controller.processWebhook("github", payloadDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().success());

        WebhookProcessingResultDto result = response.getBody().data();
        assertEquals("DEL-GH-77", result.deliveryId());
        assertTrue(result.processedSuccessfully());
        assertEquals("AUTOMATED_PR_REVIEW_AND_SAST_SCAN", result.actionTaken());
    }

    @Test
    @DisplayName("Should inspect local repository workspace via REST endpoint")
    void testInspectLocalRepositoryEndpoint() {
        LocalRepositoryInspectRequestDto requestDto = new LocalRepositoryInspectRequestDto(
                ".",
                List.of(),
                List.of(".git", ".gradle"),
                3
        );

        ResponseEntity<ApiResponse<LocalRepositoryInspectResponseDto>> response = controller.inspectLocalRepository(requestDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().success());

        LocalRepositoryInspectResponseDto data = response.getBody().data();
        assertNotNull(data.repositoryName());
        assertFalse(data.detectedLanguages().isEmpty());
    }

    @Test
    @DisplayName("Should list integration configs and retrieve single config")
    void testListAndGetConfigs() {
        ResponseEntity<ApiResponse<List<IntegrationConfigDto>>> response = controller.listConfigs();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().success());
        assertFalse(response.getBody().data().isEmpty());

        ResponseEntity<ApiResponse<IntegrationConfigDto>> singleConfig = controller.getConfig("cfg-github-01");
        assertEquals(200, singleConfig.getStatusCode().value());
        assertTrue(singleConfig.getBody().success());
        assertEquals(IntegrationProvider.GITHUB, singleConfig.getBody().data().provider());
    }

    @Test
    @DisplayName("Should return 404 for missing integration config")
    void testGetMissingConfig() {
        ResponseEntity<ApiResponse<IntegrationConfigDto>> response = controller.getConfig("cfg-non-existent");
        assertEquals(404, response.getStatusCode().value());
        assertFalse(response.getBody().success());
    }
}
