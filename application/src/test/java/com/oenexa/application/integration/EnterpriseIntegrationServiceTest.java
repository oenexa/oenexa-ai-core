package com.oenexa.application.integration;

import com.oenexa.domain.model.integration.IntegrationDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseIntegrationService Zero-Mock Unit Tests")
class EnterpriseIntegrationServiceTest {

    private EnterpriseIntegrationService service;

    @BeforeEach
    void setUp() {
        service = new EnterpriseIntegrationService();
    }

    @Test
    @DisplayName("Should process GitHub pull request webhook and generate review artifacts")
    void testProcessPrWebhook() {
        WebhookPayload payload = new WebhookPayload(
                "DELIVERY-PR-99",
                IntegrationProvider.GITHUB,
                WebhookEventType.PULL_REQUEST,
                "oenexa/oenexa-ai-core",
                "feature/ai-upgrade",
                "octocat",
                Map.of("prNumber", "12", "action", "opened"),
                null
        );

        WebhookProcessingResult result = service.processWebhook(payload);

        assertNotNull(result);
        assertEquals("DELIVERY-PR-99", result.deliveryId());
        assertTrue(result.processedSuccessfully());
        assertEquals("AUTOMATED_PR_REVIEW_AND_SAST_SCAN", result.actionTaken());
        assertTrue(result.generatedArtifacts().contains("pr-review-summary.md"));
    }

    @Test
    @DisplayName("Should process GitLab commit push webhook and evaluate drift")
    void testProcessCommitPushWebhook() {
        WebhookPayload payload = new WebhookPayload(
                "DELIVERY-PUSH-88",
                IntegrationProvider.GITLAB,
                WebhookEventType.PUSH_COMMIT,
                "oenexa/oenexa-ai-core",
                "main",
                "gitlab-ci",
                Map.of("commitSha", "abc1234"),
                null
        );

        WebhookProcessingResult result = service.processWebhook(payload);

        assertNotNull(result);
        assertTrue(result.processedSuccessfully());
        assertEquals("ARCHITECTURAL_DRIFT_EVALUATION", result.actionTaken());
        assertTrue(result.generatedArtifacts().contains("commit-drift-report.md"));
    }

    @Test
    @DisplayName("Should inspect local repository and detect languages and top-level modules")
    void testInspectLocalRepository() {
        LocalRepositoryInspectRequest request = new LocalRepositoryInspectRequest(
                ".",
                List.of(),
                List.of(".git", ".gradle", "build"),
                3
        );

        LocalRepositoryInspectResponse response = service.inspectLocalRepository(request);

        assertNotNull(response);
        assertNotNull(response.repositoryName());
        assertTrue(response.detectedLanguages().contains("Java"));
        assertFalse(response.topLevelModules().isEmpty());
    }

    @Test
    @DisplayName("Should list pre-seeded integration configs including GitHub and OpenAPI")
    void testListConfigs() {
        List<IntegrationConfig> configs = service.listConfigs();

        assertFalse(configs.isEmpty());
        assertTrue(configs.stream().anyMatch(c -> c.provider() == IntegrationProvider.GITHUB));
        assertTrue(configs.stream().anyMatch(c -> c.provider() == IntegrationProvider.OPENAPI));

        Optional<IntegrationConfig> ghConfig = service.getConfig("cfg-github-01");
        assertTrue(ghConfig.isPresent());
        assertTrue(ghConfig.get().enabled());
    }
}
