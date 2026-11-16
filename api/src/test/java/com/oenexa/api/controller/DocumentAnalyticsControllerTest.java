package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.document.DocumentAnalyticsDtos.*;
import com.oenexa.application.document.DocumentAnalyticsSupportService;
import com.oenexa.domain.model.document.DocumentAnalyticsDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DocumentAnalyticsController Zero-Mock Unit Tests")
class DocumentAnalyticsControllerTest {

    private DocumentAnalyticsSupportService service;
    private DocumentAnalyticsController controller;

    @BeforeEach
    void setUp() {
        service = new DocumentAnalyticsSupportService();
        controller = new DocumentAnalyticsController(service);
    }

    @Test
    @DisplayName("Should generate ADR document and retrieve it via endpoint")
    void testDocumentGenerationAndRetrievalFlow() {
        GenerateDocumentRequestDto requestDto = new GenerateDocumentRequestDto(
                DocumentType.ADR,
                "0005-enterprise-zero-mock",
                "Require zero-mock architecture tests across all modules.",
                Map.of("category", "testing")
        );

        ResponseEntity<ApiResponse<GeneratedDocumentDto>> response = controller.generateDocument(requestDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().success());

        GeneratedDocumentDto doc = response.getBody().data();
        assertNotNull(doc.documentId());
        assertEquals("0005-enterprise-zero-mock", doc.title());
        assertTrue(doc.markdownContent().contains("# ADR:"));

        ResponseEntity<ApiResponse<GeneratedDocumentDto>> retrieved = controller.getDocument(doc.documentId());
        assertEquals(200, retrieved.getStatusCode().value());
        assertTrue(retrieved.getBody().success());
        assertEquals(doc.documentId(), retrieved.getBody().data().documentId());
    }

    @Test
    @DisplayName("Should return 404 for missing document")
    void testGetMissingDocument() {
        ResponseEntity<ApiResponse<GeneratedDocumentDto>> response = controller.getDocument("DOC-9999");
        assertEquals(404, response.getStatusCode().value());
        assertFalse(response.getBody().success());
    }

    @Test
    @DisplayName("Should retrieve platform analytics for specified tenant")
    void testGetPlatformAnalytics() {
        ResponseEntity<ApiResponse<PlatformAnalyticsDto>> response = controller.getPlatformAnalytics("tenant-finance-1");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().success());

        PlatformAnalyticsDto analytics = response.getBody().data();
        assertEquals("tenant-finance-1", analytics.tenantId());
        assertTrue(analytics.totalTokensConsumed() > 0);
        assertFalse(analytics.modelDistribution().isEmpty());
    }

    @Test
    @DisplayName("Should resolve developer support query with high confidence")
    void testResolveSupportQuery() {
        DeveloperSupportQueryDto queryDto = new DeveloperSupportQueryDto(
                "QRY-1234",
                "dev-alice",
                "How do I run the full test suite?",
                "oenexa-ai-core",
                "HIGH"
        );

        ResponseEntity<ApiResponse<DeveloperSupportResolutionDto>> response = controller.resolveSupportQuery(queryDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().success());

        DeveloperSupportResolutionDto resolution = response.getBody().data();
        assertEquals("QRY-1234", resolution.queryId());
        assertTrue(resolution.suggestedCommands().stream().anyMatch(c -> c.contains("gradlew test")));
        assertTrue(resolution.confidenceScore() >= 0.90);
    }
}
