package com.oenexa.application.document;

import com.oenexa.domain.model.document.DocumentAnalyticsDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DocumentAnalyticsSupportService Zero-Mock Unit Tests")
class DocumentAnalyticsSupportServiceTest {

    private DocumentAnalyticsSupportService service;

    @BeforeEach
    void setUp() {
        service = new DocumentAnalyticsSupportService();
    }

    @Test
    @DisplayName("Should generate ADR document and format markdown appropriately")
    void testGenerateAdrDocument() {
        GenerateDocumentRequest request = new GenerateDocumentRequest(
                DocumentType.ADR,
                "0002-pgvector-hybrid-rag",
                "Require local-first semantic retrieval with sub-50ms latency.",
                Map.of("author", "Principal Architect")
        );

        GeneratedDocument doc = service.generateDocument(request);

        assertNotNull(doc);
        assertNotNull(doc.documentId());
        assertEquals(DocumentType.ADR, doc.type());
        assertTrue(doc.markdownContent().contains("# ADR: 0002-pgvector-hybrid-rag"));
        assertTrue(doc.markdownContent().contains("## Status"));
        assertTrue(doc.markdownContent().contains("## Context"));

        Optional<GeneratedDocument> retrieved = service.getDocument(doc.documentId());
        assertTrue(retrieved.isPresent());
        assertEquals(doc.title(), retrieved.get().title());
    }

    @Test
    @DisplayName("Should generate Technical Specification document")
    void testGenerateTechnicalSpecification() {
        GenerateDocumentRequest request = new GenerateDocumentRequest(
                DocumentType.TECHNICAL_SPECIFICATION,
                "Auth Token Service Spec",
                "Specification for RS256/ML-DSA-65 dual JWT issuance.",
                Map.of()
        );

        GeneratedDocument doc = service.generateDocument(request);

        assertNotNull(doc);
        assertEquals(DocumentType.TECHNICAL_SPECIFICATION, doc.type());
        assertTrue(doc.markdownContent().contains("# Technical Specification: Auth Token Service Spec"));
    }

    @Test
    @DisplayName("Should retrieve platform analytics with non-zero usage and model distribution")
    void testGetPlatformAnalytics() {
        PlatformAnalytics analytics = service.getPlatformAnalytics("tenant-corp-1");

        assertNotNull(analytics);
        assertEquals("tenant-corp-1", analytics.tenantId());
        assertTrue(analytics.totalTokensConsumed() > 0);
        assertTrue(analytics.totalCostUsd() > 0);
        assertFalse(analytics.modelDistribution().isEmpty());
    }

    @Test
    @DisplayName("Should resolve developer query about testing with zero-mock guidelines")
    void testResolveTestingSupportQuery() {
        DeveloperSupportQuery query = new DeveloperSupportQuery(
                "QRY-001",
                "dev-123",
                "How do I mock a repository in unit tests?",
                "oenexa-ai-core",
                "HIGH",
                null
        );

        DeveloperSupportResolution resolution = service.resolveSupportQuery(query);

        assertNotNull(resolution);
        assertEquals("QRY-001", resolution.queryId());
        assertTrue(resolution.resolutionSummary().contains("Zero-Mock"));
        assertTrue(resolution.suggestedCommands().contains("./gradlew test"));
        assertTrue(resolution.confidenceScore() >= 0.95);
    }

    @Test
    @DisplayName("Should resolve developer query about architecture with hexagonal guidelines")
    void testResolveArchitectureSupportQuery() {
        DeveloperSupportQuery query = new DeveloperSupportQuery(
                "QRY-002",
                "dev-456",
                "What are the architecture layer invariants?",
                "oenexa-ai-core",
                "NORMAL",
                null
        );

        DeveloperSupportResolution resolution = service.resolveSupportQuery(query);

        assertNotNull(resolution);
        assertTrue(resolution.resolutionSummary().contains("Hexagonal"));
        assertTrue(resolution.relevantDocs().stream().anyMatch(d -> d.contains("hexagonal")));
    }
}
