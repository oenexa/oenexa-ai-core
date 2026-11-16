package com.oenexa.domain.model.document;

import com.oenexa.domain.model.document.DocumentAnalyticsDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DocumentAnalyticsDomain Invariant Tests (Zero-Mock)")
class DocumentAnalyticsDomainTest {

    @Test
    @DisplayName("GeneratedDocument handles null content and metadata safely")
    void testGeneratedDocumentDefaults() {
        GeneratedDocument doc = new GeneratedDocument(
                "DOC-001",
                DocumentType.ADR,
                "ADR 001: Clean Hexagonal Monolith",
                null,
                null,
                null
        );

        assertEquals("DOC-001", doc.documentId());
        assertEquals(DocumentType.ADR, doc.type());
        assertEquals("", doc.markdownContent());
        assertTrue(doc.metadata().isEmpty());
        assertNotNull(doc.createdAt());
    }

    @Test
    @DisplayName("GenerateDocumentRequest throws NullPointerException on missing title or type")
    void testGenerateDocumentRequestValidation() {
        assertThrows(NullPointerException.class, () ->
                new GenerateDocumentRequest(null, "Title", "Context", Map.of())
        );

        assertThrows(NullPointerException.class, () ->
                new GenerateDocumentRequest(DocumentType.TECHNICAL_SPECIFICATION, null, "Context", Map.of())
        );
    }

    @Test
    @DisplayName("PlatformAnalytics normalizes negative values and retains distribution map")
    void testPlatformAnalyticsNormalization() {
        PlatformAnalytics analytics = new PlatformAnalytics(
                "tenant-oenexa-prod",
                -500,
                -12.5,
                -2,
                -15.0,
                Map.of("oenexa-gpt-o3", 1200L, "claude-3-5-sonnet", 450L),
                null
        );

        assertEquals("tenant-oenexa-prod", analytics.tenantId());
        assertEquals(0L, analytics.totalTokensConsumed());
        assertEquals(0.0, analytics.totalCostUsd());
        assertEquals(0L, analytics.activeQueries());
        assertEquals(0.0, analytics.averageLatencyMs());
        assertEquals(2, analytics.modelDistribution().size());
        assertNotNull(analytics.snapshotTimestamp());
    }

    @Test
    @DisplayName("DeveloperSupportResolution clamps confidence and initializes defaults")
    void testDeveloperSupportResolutionClamping() {
        DeveloperSupportResolution resolution = new DeveloperSupportResolution(
                "QRY-100",
                "Execute clean gradle build with zero mocks",
                List.of("docs/architecture.md"),
                List.of("./gradlew test"),
                1.35,
                null
        );

        assertEquals("QRY-100", resolution.queryId());
        assertEquals(1.0, resolution.confidenceScore(), 0.001);
        assertEquals(1, resolution.relevantDocs().size());
        assertEquals(1, resolution.suggestedCommands().size());
        assertNotNull(resolution.resolvedAt());
    }
}
