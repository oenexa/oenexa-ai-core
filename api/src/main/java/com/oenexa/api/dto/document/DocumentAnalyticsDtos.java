package com.oenexa.api.dto.document;

import com.oenexa.domain.model.document.DocumentAnalyticsDomain.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Enterprise DTOs for Document Generation, Platform Analytics, and Developer Support REST API.
 */
public final class DocumentAnalyticsDtos {

    private DocumentAnalyticsDtos() {
    }

    public record GenerateDocumentRequestDto(
            DocumentType type,
            String title,
            String context,
            Map<String, String> parameters
    ) {
        public GenerateDocumentRequest toDomain() {
            return new GenerateDocumentRequest(
                    type != null ? type : DocumentType.TECHNICAL_SPECIFICATION,
                    title != null ? title : "Untitled Document",
                    context != null ? context : "",
                    parameters != null ? parameters : Map.of()
            );
        }
    }

    public record GeneratedDocumentDto(
            String documentId,
            DocumentType type,
            String title,
            String markdownContent,
            Map<String, String> metadata,
            Instant createdAt
    ) {
        public static GeneratedDocumentDto fromDomain(GeneratedDocument domain) {
            return new GeneratedDocumentDto(
                    domain.documentId(),
                    domain.type(),
                    domain.title(),
                    domain.markdownContent(),
                    domain.metadata(),
                    domain.createdAt()
            );
        }
    }

    public record PlatformAnalyticsDto(
            String tenantId,
            long totalTokensConsumed,
            double totalCostUsd,
            long activeQueries,
            double averageLatencyMs,
            Map<String, Long> modelDistribution,
            Instant snapshotTimestamp
    ) {
        public static PlatformAnalyticsDto fromDomain(PlatformAnalytics domain) {
            return new PlatformAnalyticsDto(
                    domain.tenantId(),
                    domain.totalTokensConsumed(),
                    domain.totalCostUsd(),
                    domain.activeQueries(),
                    domain.averageLatencyMs(),
                    domain.modelDistribution(),
                    domain.snapshotTimestamp()
            );
        }
    }

    public record DeveloperSupportQueryDto(
            String queryId,
            String developerId,
            String question,
            String repository,
            String urgency
    ) {
        public DeveloperSupportQuery toDomain() {
            return new DeveloperSupportQuery(
                    queryId != null ? queryId : "QRY-" + System.currentTimeMillis(),
                    developerId != null ? developerId : "dev-anonymous",
                    question != null ? question : "",
                    repository != null ? repository : "global",
                    urgency != null ? urgency : "NORMAL",
                    Instant.now()
            );
        }
    }

    public record DeveloperSupportResolutionDto(
            String queryId,
            String resolutionSummary,
            List<String> relevantDocs,
            List<String> suggestedCommands,
            double confidenceScore,
            Instant resolvedAt
    ) {
        public static DeveloperSupportResolutionDto fromDomain(DeveloperSupportResolution domain) {
            return new DeveloperSupportResolutionDto(
                    domain.queryId(),
                    domain.resolutionSummary(),
                    domain.relevantDocs(),
                    domain.suggestedCommands(),
                    domain.confidenceScore(),
                    domain.resolvedAt()
            );
        }
    }
}
