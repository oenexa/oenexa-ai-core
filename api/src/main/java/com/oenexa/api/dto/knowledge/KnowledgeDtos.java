package com.oenexa.api.dto.knowledge;

import java.time.Instant;

/**
 * Standardized DTO Layer for Knowledge & RAG Domain.
 */
public class KnowledgeDtos {

    public record DocumentIngestRequestDto(
            String title,
            String content,
            String repositoryId,
            String language
    ) {
        public DocumentIngestRequestDto {
            title = (title != null) ? title : "";
            content = (content != null) ? content : "";
            repositoryId = (repositoryId != null && !repositoryId.isBlank()) ? repositoryId : "default-repo";
            language = (language != null && !language.isBlank()) ? language : "markdown";
        }
    }

    public record DocumentChunkDto(
            String chunkId,
            String title,
            String contentSnippet,
            int index
    ) {}

    public record KnowledgeSearchResultDto(
            String chunkId,
            String title,
            String snippet,
            double relevanceScore
    ) {}

    public record KnowledgeBaseSummaryDto(
            String knowledgeBaseId,
            String name,
            int totalDocuments,
            int totalChunks,
            Instant updatedAt
    ) {}
}
