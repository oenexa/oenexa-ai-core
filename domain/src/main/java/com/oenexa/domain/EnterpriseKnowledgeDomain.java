package com.oenexa.domain;

import java.time.Instant;
import java.util.*;

/**
 * Enterprise Knowledge & RAG Domain Model (Phase 6).
 * Encapsulates knowledge bases, documents, semantic chunks, citations, and knowledge graphs.
 */
public final class EnterpriseKnowledgeDomain {

    private EnterpriseKnowledgeDomain() {}

    public enum KnowledgeSourceType {
        DOCUMENT_UPLOAD,
        WEBSITE_CRAWL,
        CODEBASE_REPO,
        API_SPEC,
        CUSTOM_TEXT
    }

    public enum DocumentFormat {
        PDF,
        MARKDOWN,
        HTML,
        DOCX,
        TXT,
        JSON,
        CSV,
        XML
    }

    public enum DocumentStatus {
        QUEUED,
        CRAWLING,
        PARSING,
        EMBEDDING,
        INDEXED,
        FAILED
    }

    public record KnowledgeBase(
            UUID kbId,
            String tenantId,
            String name,
            String description,
            int documentCount,
            long totalTokens,
            Instant createdAt,
            Instant updatedAt
    ) {
        public KnowledgeBase {
            Objects.requireNonNull(kbId, "kbId must not be null");
            Objects.requireNonNull(tenantId, "tenantId must not be null");
            Objects.requireNonNull(name, "name must not be null");
            if (name.isBlank()) {
                throw new IllegalArgumentException("name cannot be blank");
            }
            if (documentCount < 0) {
                throw new IllegalArgumentException("documentCount cannot be negative");
            }
            if (totalTokens < 0) {
                throw new IllegalArgumentException("totalTokens cannot be negative");
            }
            createdAt = (createdAt != null) ? createdAt : Instant.now();
            updatedAt = (updatedAt != null) ? updatedAt : createdAt;
        }

        public KnowledgeBase withStats(int newCount, long newTokens) {
            return new KnowledgeBase(kbId, tenantId, name, description, newCount, newTokens, createdAt, Instant.now());
        }
    }

    public record KnowledgeDocument(
            UUID documentId,
            UUID kbId,
            String title,
            KnowledgeSourceType sourceType,
            String sourceReference,
            DocumentFormat format,
            long sizeBytes,
            int chunkCount,
            DocumentStatus status,
            Instant ingestedAt
    ) {
        public KnowledgeDocument {
            Objects.requireNonNull(documentId, "documentId must not be null");
            Objects.requireNonNull(kbId, "kbId must not be null");
            Objects.requireNonNull(title, "title must not be null");
            Objects.requireNonNull(sourceType, "sourceType must not be null");
            Objects.requireNonNull(format, "format must not be null");
            Objects.requireNonNull(status, "status must not be null");
            ingestedAt = (ingestedAt != null) ? ingestedAt : Instant.now();
        }
    }

    public record SemanticChunk(
            UUID chunkId,
            UUID documentId,
            int chunkIndex,
            String content,
            float[] embedding,
            Map<String, String> metadata,
            String citationReference
    ) {
        public SemanticChunk {
            Objects.requireNonNull(chunkId, "chunkId must not be null");
            Objects.requireNonNull(documentId, "documentId must not be null");
            Objects.requireNonNull(content, "content must not be null");
            metadata = (metadata != null) ? Map.copyOf(metadata) : Map.of();
            citationReference = (citationReference != null) ? citationReference : "chunk-" + chunkIndex;
        }
    }

    public record Citation(
            String citationId,
            UUID documentId,
            String documentTitle,
            String sourceReference,
            int sectionOrPage,
            String snippet,
            double relevanceScore
    ) {
        public Citation {
            Objects.requireNonNull(citationId, "citationId must not be null");
            Objects.requireNonNull(documentId, "documentId must not be null");
            Objects.requireNonNull(documentTitle, "documentTitle must not be null");
            Objects.requireNonNull(snippet, "snippet must not be null");
        }
    }

    public record KnowledgeGraphNode(
            String nodeId,
            String label,
            String entityType,
            Map<String, String> properties
    ) {
        public KnowledgeGraphNode {
            Objects.requireNonNull(nodeId, "nodeId must not be null");
            Objects.requireNonNull(label, "label must not be null");
            Objects.requireNonNull(entityType, "entityType must not be null");
            properties = (properties != null) ? Map.copyOf(properties) : Map.of();
        }
    }

    public record KnowledgeGraphEdge(
            String edgeId,
            String sourceNodeId,
            String targetNodeId,
            String relationshipType,
            double weight
    ) {
        public KnowledgeGraphEdge {
            Objects.requireNonNull(edgeId, "edgeId must not be null");
            Objects.requireNonNull(sourceNodeId, "sourceNodeId must not be null");
            Objects.requireNonNull(targetNodeId, "targetNodeId must not be null");
            Objects.requireNonNull(relationshipType, "relationshipType must not be null");
        }
    }

    public record KnowledgeGraph(
            List<KnowledgeGraphNode> nodes,
            List<KnowledgeGraphEdge> edges
    ) {
        public KnowledgeGraph {
            nodes = (nodes != null) ? List.copyOf(nodes) : List.of();
            edges = (edges != null) ? List.copyOf(edges) : List.of();
        }
    }

    public record SemanticSearchResult(
            String query,
            List<Citation> citations,
            String synthesizedAnswer,
            KnowledgeGraph subgraph,
            double confidenceScore,
            long retrievalLatencyMs
    ) {
        public SemanticSearchResult {
            Objects.requireNonNull(query, "query must not be null");
            Objects.requireNonNull(synthesizedAnswer, "synthesizedAnswer must not be null");
            citations = (citations != null) ? List.copyOf(citations) : List.of();
            subgraph = (subgraph != null) ? subgraph : new KnowledgeGraph(List.of(), List.of());
        }
    }
}
