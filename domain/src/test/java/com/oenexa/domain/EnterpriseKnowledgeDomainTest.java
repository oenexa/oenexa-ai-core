package com.oenexa.domain;

import com.oenexa.domain.EnterpriseKnowledgeDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseKnowledgeDomain Invariant Tests (Zero-Mock)")
class EnterpriseKnowledgeDomainTest {

    @Test
    @DisplayName("Should create valid KnowledgeBase and update stats")
    void shouldCreateKnowledgeBase() {
        UUID kbId = UUID.randomUUID();
        KnowledgeBase kb = new KnowledgeBase(
                kbId,
                "tenant-alpha",
                "Core Engineering Architecture",
                "Architecture specifications & microservice blueprints",
                5,
                12000L,
                Instant.now(),
                Instant.now()
        );

        assertNotNull(kb);
        assertEquals(kbId, kb.kbId());
        assertEquals("Core Engineering Architecture", kb.name());
        assertEquals(5, kb.documentCount());
        assertEquals(12000L, kb.totalTokens());

        KnowledgeBase updated = kb.withStats(6, 15000L);
        assertEquals(6, updated.documentCount());
        assertEquals(15000L, updated.totalTokens());
    }

    @Test
    @DisplayName("Should throw validation errors on invalid KnowledgeBase parameters")
    void shouldEnforceKnowledgeBaseInvariants() {
        assertThrows(NullPointerException.class, () ->
                new KnowledgeBase(null, "tenant-1", "Name", "Desc", 0, 0, null, null));
        assertThrows(IllegalArgumentException.class, () ->
                new KnowledgeBase(UUID.randomUUID(), "tenant-1", "   ", "Desc", 0, 0, null, null));
        assertThrows(IllegalArgumentException.class, () ->
                new KnowledgeBase(UUID.randomUUID(), "tenant-1", "Valid", "Desc", -1, 0, null, null));
        assertThrows(IllegalArgumentException.class, () ->
                new KnowledgeBase(UUID.randomUUID(), "tenant-1", "Valid", "Desc", 0, -5, null, null));
    }

    @Test
    @DisplayName("Should create KnowledgeDocument, SemanticChunk, and Citation records")
    void shouldCreateDocumentAndCitationRecords() {
        UUID docId = UUID.randomUUID();
        UUID kbId = UUID.randomUUID();

        KnowledgeDocument doc = new KnowledgeDocument(
                docId,
                kbId,
                "Architecture_Guide.pdf",
                KnowledgeSourceType.DOCUMENT_UPLOAD,
                "docs/Architecture_Guide.pdf",
                DocumentFormat.PDF,
                102400L,
                4,
                DocumentStatus.INDEXED,
                Instant.now()
        );

        assertEquals("Architecture_Guide.pdf", doc.title());
        assertEquals(DocumentStatus.INDEXED, doc.status());

        SemanticChunk chunk = new SemanticChunk(
                UUID.randomUUID(),
                docId,
                1,
                "Hexagonal architecture decouples core domain logic from framework adapters.",
                new float[]{0.1f, 0.2f, 0.3f},
                Map.of("page", "12"),
                "Architecture_Guide.pdf#p12"
        );

        assertEquals(1, chunk.chunkIndex());
        assertEquals("Architecture_Guide.pdf#p12", chunk.citationReference());

        Citation citation = new Citation(
                "cit-1",
                docId,
                "Architecture_Guide.pdf",
                "docs/Architecture_Guide.pdf",
                12,
                "Hexagonal architecture decouples core domain logic...",
                0.94
        );

        assertEquals(0.94, citation.relevanceScore());
        assertEquals(12, citation.sectionOrPage());
    }

    @Test
    @DisplayName("Should construct KnowledgeGraph and SemanticSearchResult")
    void shouldConstructGraphAndSearchResult() {
        KnowledgeGraphNode nodeA = new KnowledgeGraphNode("node-auth", "AuthService", "MICROSERVICE", Map.of("layer", "core"));
        KnowledgeGraphNode nodeB = new KnowledgeGraphNode("node-jwt", "JwtProvider", "SECURITY_COMPONENT", Map.of());
        KnowledgeGraphEdge edge = new KnowledgeGraphEdge("edge-1", "node-auth", "node-jwt", "USES", 1.0);

        KnowledgeGraph graph = new KnowledgeGraph(List.of(nodeA, nodeB), List.of(edge));
        assertEquals(2, graph.nodes().size());
        assertEquals(1, graph.edges().size());

        SemanticSearchResult result = new SemanticSearchResult(
                "How does authentication work?",
                List.of(),
                "Authentication uses JWT with 2FA TOTP verification.",
                graph,
                97.5,
                42L
        );

        assertEquals("How does authentication work?", result.query());
        assertEquals(97.5, result.confidenceScore());
        assertEquals(42L, result.retrievalLatencyMs());
    }
}
