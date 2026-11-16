package com.oenexa.api.dto.knowledge;

import com.oenexa.domain.model.knowledge.ContextRagDomain.ContextScope;

import java.util.List;

/**
 * Enterprise Context & RAG API Data Transfer Objects (DTOs).
 * Strictly immutable boundary schemas for request/response serialization.
 */
public final class ContextRagDtos {

    private ContextRagDtos() {}

    public record ExecuteRagQueryRequest(
            String query,
            List<ContextScope> scopes
    ) {}

    public record ExpandContextRequest(
            String query,
            List<ContextScope> targetScopes,
            int maxTokens
    ) {}

    public record IndexContextChunkRequest(
            String chunkId,
            ContextScope scope,
            String sourcePath,
            String title,
            String content,
            double relevanceScore
    ) {}
}
