package com.oenexa.api;

import com.oenexa.application.RagService;
import com.oenexa.domain.RagDocument.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Document Ingestion and Semantic Retrieval.
 */
@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/documents")
    public ResponseEntity<IngestResult> ingest(@RequestBody DocumentInput input) {
        if (input.title() == null || input.content() == null) {
            return ResponseEntity.badRequest().build();
        }
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
