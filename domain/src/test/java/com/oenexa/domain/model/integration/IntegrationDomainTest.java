package com.oenexa.domain.model.integration;

import com.oenexa.domain.model.integration.IntegrationDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IntegrationDomain Invariant Tests (Zero-Mock)")
class IntegrationDomainTest {

    @Test
    @DisplayName("IntegrationConfig handles null settings and defaults cleanly")
    void testIntegrationConfigDefaults() {
        IntegrationConfig config = new IntegrationConfig(
                "INT-GH-01",
                IntegrationProvider.GITHUB,
                "https://api.github.com",
                true,
                null
        );

        assertEquals("INT-GH-01", config.integrationId());
        assertEquals(IntegrationProvider.GITHUB, config.provider());
        assertTrue(config.enabled());
        assertTrue(config.settings().isEmpty());
    }

    @Test
    @DisplayName("WebhookPayload defaults null repository and sender safely")
    void testWebhookPayloadDefaults() {
        WebhookPayload payload = new WebhookPayload(
                "DELIVERY-101",
                IntegrationProvider.GITLAB,
                WebhookEventType.PULL_REQUEST,
                null,
                null,
                null,
                Map.of("prNumber", "42"),
                null
        );

        assertEquals("DELIVERY-101", payload.deliveryId());
        assertEquals("unknown-repo", payload.repository());
        assertEquals("main", payload.branch());
        assertEquals("system", payload.sender());
        assertEquals("42", payload.payloadData().get("prNumber"));
        assertNotNull(payload.receivedAt());
    }

    @Test
    @DisplayName("LocalRepositoryInspectRequest clamps depth and defaults exclude patterns")
    void testLocalRepositoryInspectRequestClamping() {
        LocalRepositoryInspectRequest request = new LocalRepositoryInspectRequest(
                "/workspace/oenexa-core",
                null,
                null,
                50
        );

        assertEquals("/workspace/oenexa-core", request.localPath());
        assertEquals(10, request.maxDepth());
        assertTrue(request.excludePatterns().contains(".git"));
        assertTrue(request.excludePatterns().contains("node_modules"));
    }

    @Test
    @DisplayName("LocalRepositoryInspectResponse normalizes negative counts and clamps defaults")
    void testLocalRepositoryInspectResponseDefaults() {
        LocalRepositoryInspectResponse response = new LocalRepositoryInspectResponse(
                "oenexa-ai-core",
                "main",
                -10,
                -500L,
                List.of("Java", "TypeScript"),
                List.of("domain", "application", "infrastructure", "api"),
                null
        );

        assertEquals("oenexa-ai-core", response.repositoryName());
        assertEquals(0, response.fileCount());
        assertEquals(0L, response.totalSizeByte());
        assertEquals(2, response.detectedLanguages().size());
        assertEquals(4, response.topLevelModules().size());
        assertNotNull(response.inspectedAt());
    }
}
