package com.oenexa.domain.model.knowledge;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Enterprise Context & RAG Domain models.
 * Encapsulates multi-source context expansion, hybrid retrieval scopes,
 * source attribution, and ground-truth answer verification.
 * Pure Java 25 Domain Model (Zero framework dependencies).
 */
public final class ContextRagDomain {

    private ContextRagDomain() {}

    public enum ContextScope {
        REPOSITORY,
        ARCHITECTURE,
        ADR_RECORD,
        POLICY_STANDARD,
        RUNBOOK,
        INCIDENT_LOG
    }

    public record ContextChunk(
            String chunkId,
            ContextScope scope,
            String sourcePath,
            String title,
            String content,
            double relevanceScore
    ) {
        public ContextChunk {
            chunkId = (chunkId != null) ? chunkId : "chunk-" + UUID.randomUUID();
            Objects.requireNonNull(scope, "scope must not be null");
            sourcePath = (sourcePath != null) ? sourcePath : "unspecified";
            title = (title != null) ? title : "Untitled Chunk";
            Objects.requireNonNull(content, "content must not be null");
            relevanceScore = Math.clamp(relevanceScore, 0.0, 1.0);
        }
    }

    public record ExpandedContext(
            String query,
            List<ContextScope> queriedScopes,
            List<ContextChunk> relevantChunks,
            int estimatedTokens,
            Instant assembledAt
    ) {
        public ExpandedContext {
            Objects.requireNonNull(query, "query must not be null");
            queriedScopes = (queriedScopes != null) ? List.copyOf(queriedScopes) : List.of();
            relevantChunks = (relevantChunks != null) ? List.copyOf(relevantChunks) : List.of();
            estimatedTokens = Math.max(0, estimatedTokens);
            assembledAt = (assembledAt != null) ? assembledAt : Instant.now();
        }
    }

    public record RagVerificationResult(
            boolean verified,
            double faithfulnessScore,
            List<String> verifiedGroundClaims,
            List<String> ungroundedClaims,
            String verificationSummary
    ) {
        public RagVerificationResult {
            faithfulnessScore = Math.clamp(faithfulnessScore, 0.0, 1.0);
            verifiedGroundClaims = (verifiedGroundClaims != null) ? List.copyOf(verifiedGroundClaims) : List.of();
            ungroundedClaims = (ungroundedClaims != null) ? List.copyOf(ungroundedClaims) : List.of();
            verificationSummary = (verificationSummary != null) ? verificationSummary : "Verification completed.";
            verified = (ungroundedClaims.isEmpty() && faithfulnessScore >= 0.70);
        }

        public static RagVerificationResult verified(double score, List<String> claims) {
            return new RagVerificationResult(true, score, claims, List.of(), "Answer is fully grounded in retrieved source citations.");
        }

        public static RagVerificationResult unverified(double score, List<String> validClaims, List<String> ungrounded) {
            return new RagVerificationResult(false, score, validClaims, ungrounded, "Answer contains unverified claims not grounded in citations.");
        }
    }

    public record RagQueryResponse(
            String queryId,
            String query,
            String synthesizedAnswer,
            List<ContextChunk> attributedSources,
            RagVerificationResult verification,
            long retrievalLatencyMs,
            Instant timestamp
    ) {
        public RagQueryResponse {
            queryId = (queryId != null) ? queryId : "rag-q-" + UUID.randomUUID();
            Objects.requireNonNull(query, "query must not be null");
            Objects.requireNonNull(synthesizedAnswer, "synthesizedAnswer must not be null");
            attributedSources = (attributedSources != null) ? List.copyOf(attributedSources) : List.of();
            Objects.requireNonNull(verification, "verification must not be null");
            retrievalLatencyMs = Math.max(0, retrievalLatencyMs);
            timestamp = (timestamp != null) ? timestamp : Instant.now();
        }
    }
}
