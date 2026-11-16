package com.oenexa.domain.model.document;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Enterprise Domain Records and Invariants for Document Generation,
 * Platform Analytics, and Developer Support Intelligence.
 */
public final class DocumentAnalyticsDomain {

    private DocumentAnalyticsDomain() {
    }

    public enum DocumentType {
        ADR,
        TECHNICAL_SPECIFICATION,
        POST_MORTEM_RUNBOOK,
        DEVELOPER_GUIDE,
        API_CONTRACT
    }

    public record GeneratedDocument(
            String documentId,
            DocumentType type,
            String title,
            String markdownContent,
            Map<String, String> metadata,
            Instant createdAt
    ) {
        public GeneratedDocument {
            Objects.requireNonNull(documentId, "documentId must not be null");
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(title, "title must not be null");
            markdownContent = markdownContent == null ? "" : markdownContent;
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
            createdAt = createdAt == null ? Instant.now() : createdAt;
        }
    }

    public record GenerateDocumentRequest(
            DocumentType type,
            String title,
            String context,
            Map<String, String> parameters
    ) {
        public GenerateDocumentRequest {
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(title, "title must not be null");
            context = context == null ? "" : context;
            parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        }
    }

    public record PlatformAnalytics(
            String tenantId,
            long totalTokensConsumed,
            double totalCostUsd,
            long activeQueries,
            double averageLatencyMs,
            Map<String, Long> modelDistribution,
            Instant snapshotTimestamp
    ) {
        public PlatformAnalytics {
            Objects.requireNonNull(tenantId, "tenantId must not be null");
            totalTokensConsumed = Math.max(0, totalTokensConsumed);
            totalCostUsd = Math.max(0.0, totalCostUsd);
            activeQueries = Math.max(0, activeQueries);
            averageLatencyMs = Math.max(0.0, averageLatencyMs);
            modelDistribution = modelDistribution == null ? Map.of() : Map.copyOf(modelDistribution);
            snapshotTimestamp = snapshotTimestamp == null ? Instant.now() : snapshotTimestamp;
        }
    }

    public record DeveloperSupportQuery(
            String queryId,
            String developerId,
            String question,
            String repository,
            String urgency,
            Instant submittedAt
    ) {
        public DeveloperSupportQuery {
            Objects.requireNonNull(queryId, "queryId must not be null");
            Objects.requireNonNull(question, "question must not be null");
            developerId = developerId == null ? "dev-anonymous" : developerId;
            repository = repository == null ? "global" : repository;
            urgency = urgency == null ? "NORMAL" : urgency;
            submittedAt = submittedAt == null ? Instant.now() : submittedAt;
        }
    }

    public record DeveloperSupportResolution(
            String queryId,
            String resolutionSummary,
            List<String> relevantDocs,
            List<String> suggestedCommands,
            double confidenceScore,
            Instant resolvedAt
    ) {
        public DeveloperSupportResolution {
            Objects.requireNonNull(queryId, "queryId must not be null");
            resolutionSummary = resolutionSummary == null ? "" : resolutionSummary;
            relevantDocs = relevantDocs == null ? List.of() : List.copyOf(relevantDocs);
            suggestedCommands = suggestedCommands == null ? List.of() : List.copyOf(suggestedCommands);
            confidenceScore = Math.clamp(confidenceScore, 0.0, 1.0);
            resolvedAt = resolvedAt == null ? Instant.now() : resolvedAt;
        }
    }
}
