package com.oenexa.api.dto.integration;

import com.oenexa.domain.model.integration.IntegrationDomain.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Enterprise DTOs for GitHub, GitLab, Local Workspace, and OpenAPI Integrations REST API.
 */
public final class IntegrationDtos {

    private IntegrationDtos() {
    }

    public record IntegrationConfigDto(
            String integrationId,
            IntegrationProvider provider,
            String endpointUrl,
            boolean enabled,
            Map<String, String> settings
    ) {
        public static IntegrationConfigDto fromDomain(IntegrationConfig domain) {
            return new IntegrationConfigDto(
                    domain.integrationId(),
                    domain.provider(),
                    domain.endpointUrl(),
                    domain.enabled(),
                    domain.settings()
            );
        }
    }

    public record WebhookPayloadDto(
            String deliveryId,
            WebhookEventType eventType,
            String repository,
            String branch,
            String sender,
            Map<String, String> payloadData
    ) {
        public WebhookPayload toDomain(IntegrationProvider provider) {
            return new WebhookPayload(
                    deliveryId != null ? deliveryId : "DEL-" + System.currentTimeMillis(),
                    provider != null ? provider : IntegrationProvider.GITHUB,
                    eventType != null ? eventType : WebhookEventType.PUSH_COMMIT,
                    repository != null ? repository : "unknown-repo",
                    branch != null ? branch : "main",
                    sender != null ? sender : "system",
                    payloadData != null ? payloadData : Map.of(),
                    Instant.now()
            );
        }
    }

    public record WebhookProcessingResultDto(
            String deliveryId,
            boolean processedSuccessfully,
            String actionTaken,
            List<String> generatedArtifacts,
            Instant completedAt
    ) {
        public static WebhookProcessingResultDto fromDomain(WebhookProcessingResult domain) {
            return new WebhookProcessingResultDto(
                    domain.deliveryId(),
                    domain.processedSuccessfully(),
                    domain.actionTaken(),
                    domain.generatedArtifacts(),
                    domain.completedAt()
            );
        }
    }

    public record LocalRepositoryInspectRequestDto(
            String localPath,
            List<String> includePatterns,
            List<String> excludePatterns,
            int maxDepth
    ) {
        public LocalRepositoryInspectRequest toDomain() {
            return new LocalRepositoryInspectRequest(
                    localPath != null ? localPath : ".",
                    includePatterns != null ? includePatterns : List.of(),
                    excludePatterns != null ? excludePatterns : List.of(".git", ".gradle", "node_modules", "build"),
                    maxDepth > 0 ? maxDepth : 5
            );
        }
    }

    public record LocalRepositoryInspectResponseDto(
            String repositoryName,
            String currentBranch,
            int fileCount,
            long totalSizeByte,
            List<String> detectedLanguages,
            List<String> topLevelModules,
            Instant inspectedAt
    ) {
        public static LocalRepositoryInspectResponseDto fromDomain(LocalRepositoryInspectResponse domain) {
            return new LocalRepositoryInspectResponseDto(
                    domain.repositoryName(),
                    domain.currentBranch(),
                    domain.fileCount(),
                    domain.totalSizeByte(),
                    domain.detectedLanguages(),
                    domain.topLevelModules(),
                    domain.inspectedAt()
            );
        }
    }
}
