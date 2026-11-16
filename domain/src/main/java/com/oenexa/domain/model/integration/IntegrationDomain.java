package com.oenexa.domain.model.integration;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Enterprise Integration Domain Models and Invariants.
 * Covers GitHub, GitLab, Local Repository Access, OpenAPI, and Markdown tooling.
 */
public final class IntegrationDomain {

    private IntegrationDomain() {
    }

    public enum IntegrationProvider {
        GITHUB,
        GITLAB,
        LOCAL_FILESYSTEM,
        OPENAPI,
        MARKDOWN
    }

    public enum WebhookEventType {
        PULL_REQUEST,
        PUSH_COMMIT,
        PIPELINE_STATUS,
        SECURITY_ALERT
    }

    public record IntegrationConfig(
            String integrationId,
            IntegrationProvider provider,
            String endpointUrl,
            boolean enabled,
            Map<String, String> settings
    ) {
        public IntegrationConfig {
            Objects.requireNonNull(integrationId, "integrationId must not be null");
            Objects.requireNonNull(provider, "provider must not be null");
            endpointUrl = endpointUrl == null ? "" : endpointUrl;
            settings = settings == null ? Map.of() : Map.copyOf(settings);
        }
    }

    public record WebhookPayload(
            String deliveryId,
            IntegrationProvider provider,
            WebhookEventType eventType,
            String repository,
            String branch,
            String sender,
            Map<String, String> payloadData,
            Instant receivedAt
    ) {
        public WebhookPayload {
            Objects.requireNonNull(deliveryId, "deliveryId must not be null");
            Objects.requireNonNull(provider, "provider must not be null");
            Objects.requireNonNull(eventType, "eventType must not be null");
            repository = repository == null ? "unknown-repo" : repository;
            branch = branch == null ? "main" : branch;
            sender = sender == null ? "system" : sender;
            payloadData = payloadData == null ? Map.of() : Map.copyOf(payloadData);
            receivedAt = receivedAt == null ? Instant.now() : receivedAt;
        }
    }

    public record WebhookProcessingResult(
            String deliveryId,
            boolean processedSuccessfully,
            String actionTaken,
            List<String> generatedArtifacts,
            Instant completedAt
    ) {
        public WebhookProcessingResult {
            Objects.requireNonNull(deliveryId, "deliveryId must not be null");
            actionTaken = actionTaken == null ? "NONE" : actionTaken;
            generatedArtifacts = generatedArtifacts == null ? List.of() : List.copyOf(generatedArtifacts);
            completedAt = completedAt == null ? Instant.now() : completedAt;
        }
    }

    public record LocalRepositoryInspectRequest(
            String localPath,
            List<String> includePatterns,
            List<String> excludePatterns,
            int maxDepth
    ) {
        public LocalRepositoryInspectRequest {
            Objects.requireNonNull(localPath, "localPath must not be null");
            includePatterns = includePatterns == null ? List.of() : List.copyOf(includePatterns);
            excludePatterns = excludePatterns == null ? List.of(".git", "node_modules", "build", ".gradle") : List.copyOf(excludePatterns);
            maxDepth = Math.max(1, Math.min(maxDepth, 10));
        }
    }

    public record LocalRepositoryInspectResponse(
            String repositoryName,
            String currentBranch,
            int fileCount,
            long totalSizeByte,
            List<String> detectedLanguages,
            List<String> topLevelModules,
            Instant inspectedAt
    ) {
        public LocalRepositoryInspectResponse {
            Objects.requireNonNull(repositoryName, "repositoryName must not be null");
            currentBranch = currentBranch == null ? "main" : currentBranch;
            fileCount = Math.max(0, fileCount);
            totalSizeByte = Math.max(0L, totalSizeByte);
            detectedLanguages = detectedLanguages == null ? List.of() : List.copyOf(detectedLanguages);
            topLevelModules = topLevelModules == null ? List.of() : List.copyOf(topLevelModules);
            inspectedAt = inspectedAt == null ? Instant.now() : inspectedAt;
        }
    }
}
