package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.knowledge.KnowledgeDtos.DocumentChunkDto;
import com.oenexa.api.dto.knowledge.KnowledgeDtos.DocumentIngestRequestDto;
import com.oenexa.api.dto.knowledge.KnowledgeDtos.KnowledgeSearchResultDto;
import com.oenexa.api.mapper.KnowledgeApiMapper;
import com.oenexa.application.service.knowledge.RagService;
import com.oenexa.application.security.KnowledgeSecurityPolicy;
import com.oenexa.application.validation.KnowledgeValidator;
import com.oenexa.domain.model.knowledge.RagDocument.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * REST API for Document Ingestion and Semantic Retrieval.
 */
@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    private final RagService ragService;
    private final KnowledgeValidator knowledgeValidator;
    private final KnowledgeSecurityPolicy knowledgeSecurityPolicy;
    private final KnowledgeApiMapper knowledgeApiMapper;

    public RagController(RagService ragService) {
        this(ragService, new KnowledgeValidator(), new KnowledgeSecurityPolicy(), new KnowledgeApiMapper());
    }

    @Autowired
    public RagController(
            RagService ragService,
            KnowledgeValidator knowledgeValidator,
            KnowledgeSecurityPolicy knowledgeSecurityPolicy,
            KnowledgeApiMapper knowledgeApiMapper
    ) {
        this.ragService = Objects.requireNonNull(ragService, "ragService must not be null");
        this.knowledgeValidator = knowledgeValidator != null ? knowledgeValidator : new KnowledgeValidator();
        this.knowledgeSecurityPolicy = knowledgeSecurityPolicy != null ? knowledgeSecurityPolicy : new KnowledgeSecurityPolicy();
        this.knowledgeApiMapper = knowledgeApiMapper != null ? knowledgeApiMapper : new KnowledgeApiMapper();
    }

    public KnowledgeValidator getKnowledgeValidator() {
        return knowledgeValidator;
    }

    public KnowledgeSecurityPolicy getKnowledgeSecurityPolicy() {
        return knowledgeSecurityPolicy;
    }

    public KnowledgeApiMapper getKnowledgeApiMapper() {
        return knowledgeApiMapper;
    }

    @PostMapping("/v2/documents")
    public ResponseEntity<ApiResponse<DocumentChunkDto>> ingestV2(@RequestBody DocumentIngestRequestDto dto) {
        if (dto == null || dto.title() == null || dto.content() == null) {
            return ResponseEntity.badRequest().build();
        }
        DocumentInput input = knowledgeApiMapper.toDomainInput(dto);
        knowledgeValidator.validateDocumentInput(input);
        IngestResult result = ragService.ingest(input);
        String chunkId = (result.chunkIds() != null && !result.chunkIds().isEmpty()) ? result.chunkIds().get(0) : "chunk-0";
        Chunk firstChunk = new Chunk(chunkId, input.title(), input.content(), 0);
        return ResponseEntity.ok(ApiResponse.ok("Document ingested successfully", knowledgeApiMapper.toChunkDto(firstChunk)));
    }

    @GetMapping("/v2/query")
    public ResponseEntity<ApiResponse<List<KnowledgeSearchResultDto>>> queryV2(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<SearchResult> results = ragService.search(q, limit);
        List<KnowledgeSearchResultDto> dtos = results.stream().map(knowledgeApiMapper::toSearchDto).toList();
        return ResponseEntity.ok(ApiResponse.ok("Knowledge search results", dtos));
    }

    @PostMapping("/documents")
    public ResponseEntity<IngestResult> ingest(@RequestBody DocumentInput input) {
        if (input == null || input.title() == null || input.content() == null) {
            return ResponseEntity.badRequest().build();
        }
        knowledgeValidator.validateDocumentInput(input);
        return ResponseEntity.ok(ragService.ingest(input));
    }

    @GetMapping("/query")
    public ResponseEntity<List<SearchResult>> query(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(ragService.search(q, limit));
    }
}


