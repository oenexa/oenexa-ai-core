package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.knowledge.ContextRagDtos.*;
import com.oenexa.application.knowledge.ContextRagPlatformService;
import com.oenexa.domain.model.knowledge.ContextRagDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise Context & RAG Platform Controller.
 * Exposes endpoints for:
 * 1. Multi-source semantic search & grounded RAG answers (`/api/v4/rag/query`)
 * 2. Dynamic context expansion across ADRs, Architecture, and Runbooks (`/api/v4/context/expand`)
 * 3. Hot indexing of operational knowledge chunks (`/api/v4/context/index`)
 */
@RestController
@RequestMapping("/api/v4/rag")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class ContextRagController {

    private final ContextRagPlatformService ragPlatformService;

    public ContextRagController(ContextRagPlatformService ragPlatformService) {
        this.ragPlatformService = Objects.requireNonNull(ragPlatformService, "ragPlatformService must not be null");
    }

    @PostMapping("/query")
    public ResponseEntity<ApiResponse<RagQueryResponse>> executeRagQuery(
            @RequestBody ExecuteRagQueryRequest request
    ) {
        String query = (request != null && request.query() != null) ? request.query() : "";
        List<ContextScope> scopes = (request != null) ? request.scopes() : null;

        RagQueryResponse response = ragPlatformService.executeRagQuery(query, scopes);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/context/expand")
    public ResponseEntity<ApiResponse<ExpandedContext>> expandContext(
            @RequestBody ExpandContextRequest request
    ) {
        String query = (request != null && request.query() != null) ? request.query() : "";
        List<ContextScope> scopes = (request != null) ? request.targetScopes() : null;
        int maxTokens = (request != null) ? request.maxTokens() : 4_000;

        ExpandedContext context = ragPlatformService.expandContext(query, scopes, maxTokens);
        return ResponseEntity.ok(ApiResponse.ok(context));
    }

    @PostMapping("/context/index")
    public ResponseEntity<ApiResponse<String>> indexChunk(
            @RequestBody IndexContextChunkRequest request
    ) {
        if (request == null || request.content() == null || request.scope() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(400, "Scope and content must not be null"));
        }

        ContextChunk chunk = new ContextChunk(
                request.chunkId(),
                request.scope(),
                request.sourcePath(),
                request.title(),
                request.content(),
                request.relevanceScore() > 0 ? request.relevanceScore() : 1.0
        );

        ragPlatformService.indexContextChunk(chunk);
        return ResponseEntity.ok(ApiResponse.ok("Chunk indexed successfully: " + chunk.chunkId()));
    }
}
