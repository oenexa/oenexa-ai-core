package com.oenexa.api.controller;

import com.oenexa.application.EnterpriseKnowledgeService;
import com.oenexa.domain.EnterpriseKnowledgeDomain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Enterprise Knowledge & RAG REST Controller (Phase 6).
 * Exposes knowledge base management, document ingestion, web crawling, semantic search, and knowledge graph.
 */
@RestController
@RequestMapping("/api/v1/knowledge")
@CrossOrigin(origins = "*")
public class EnterpriseKnowledgeController {

    private final EnterpriseKnowledgeService knowledgeService;

    public EnterpriseKnowledgeController(EnterpriseKnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping("/bases")
    public ResponseEntity<List<KnowledgeBase>> listBases(@RequestParam(value = "tenantId", required = false) String tenantId) {
        return ResponseEntity.ok(knowledgeService.listKnowledgeBases(tenantId));
    }

    @PostMapping("/bases")
    public ResponseEntity<KnowledgeBase> createBase(@RequestBody CreateBaseRequest req) {
        KnowledgeBase kb = knowledgeService.createKnowledgeBase(req.tenantId(), req.name(), req.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(kb);
    }

    @PostMapping("/bases/{kbId}/documents/upload")
    public ResponseEntity<KnowledgeDocument> uploadDocument(
            @PathVariable UUID kbId,
            @RequestBody UploadDocumentRequest req
    ) {
        byte[] bytes = req.content() != null ? req.content().getBytes(StandardCharsets.UTF_8) : new byte[0];
        DocumentFormat format = req.format() != null ? req.format() : DocumentFormat.TXT;
        KnowledgeDocument doc = knowledgeService.uploadDocument(kbId, req.filename(), bytes, format);
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }

    @PostMapping("/bases/{kbId}/crawl")
    public ResponseEntity<KnowledgeDocument> crawlWebsite(
            @PathVariable UUID kbId,
            @RequestBody CrawlWebsiteRequest req
    ) {
        KnowledgeDocument doc = knowledgeService.crawlWebsite(kbId, req.url(), req.maxDepth(), req.crawlSubdomains());
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }

    @PostMapping("/bases/{kbId}/search")
    public ResponseEntity<SemanticSearchResult> searchKnowledge(
            @PathVariable UUID kbId,
            @RequestBody SearchKnowledgeRequest req
    ) {
        int topK = req.topK() != null ? req.topK() : 5;
        SemanticSearchResult result = knowledgeService.searchKnowledge(kbId, req.query(), topK);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/bases/{kbId}/graph")
    public ResponseEntity<KnowledgeGraph> getGraph(@PathVariable UUID kbId) {
        return ResponseEntity.ok(knowledgeService.getKnowledgeGraph(kbId));
    }

    public record CreateBaseRequest(String tenantId, String name, String description) {}
    public record UploadDocumentRequest(String filename, String content, DocumentFormat format) {}
    public record CrawlWebsiteRequest(String url, int maxDepth, boolean crawlSubdomains) {}
    public record SearchKnowledgeRequest(String query, Integer topK) {}
}
