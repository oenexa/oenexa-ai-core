package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.document.DocumentAnalyticsDtos.*;
import com.oenexa.application.document.DocumentAnalyticsSupportService;
import com.oenexa.domain.model.document.DocumentAnalyticsDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

/**
 * Enterprise Document Generation, Platform Analytics, and Developer Support REST API Controller.
 */
@RestController
@RequestMapping("/api/v4")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class DocumentAnalyticsController {

    private final DocumentAnalyticsSupportService documentAnalyticsService;

    public DocumentAnalyticsController(DocumentAnalyticsSupportService documentAnalyticsService) {
        this.documentAnalyticsService = Objects.requireNonNull(documentAnalyticsService, "documentAnalyticsService must not be null");
    }

    @PostMapping("/documents/generate")
    public ResponseEntity<ApiResponse<GeneratedDocumentDto>> generateDocument(
            @RequestBody GenerateDocumentRequestDto requestDto
    ) {
        GenerateDocumentRequest request = (requestDto != null)
                ? requestDto.toDomain()
                : new GenerateDocumentRequest(DocumentType.TECHNICAL_SPECIFICATION, "Untitled", "", null);

        GeneratedDocument doc = documentAnalyticsService.generateDocument(request);
        return ResponseEntity.ok(ApiResponse.ok(GeneratedDocumentDto.fromDomain(doc)));
    }

    @GetMapping("/documents/{documentId}")
    public ResponseEntity<ApiResponse<GeneratedDocumentDto>> getDocument(
            @PathVariable("documentId") String documentId
    ) {
        Optional<GeneratedDocument> doc = documentAnalyticsService.getDocument(documentId);
        return doc
                .map(d -> ResponseEntity.ok(ApiResponse.ok(GeneratedDocumentDto.fromDomain(d))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.failure(404, "Document not found: " + documentId)));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<PlatformAnalyticsDto>> getPlatformAnalytics(
            @RequestHeader(value = "X-Oenexa-Tenant-Id", required = false, defaultValue = "oenexa-default") String tenantId
    ) {
        PlatformAnalytics analytics = documentAnalyticsService.getPlatformAnalytics(tenantId);
        return ResponseEntity.ok(ApiResponse.ok(PlatformAnalyticsDto.fromDomain(analytics)));
    }

    @PostMapping("/support/query")
    public ResponseEntity<ApiResponse<DeveloperSupportResolutionDto>> resolveSupportQuery(
            @RequestBody DeveloperSupportQueryDto queryDto
    ) {
        DeveloperSupportQuery query = (queryDto != null)
                ? queryDto.toDomain()
                : new DeveloperSupportQuery("QRY-0", "anonymous", "", "global", "NORMAL", null);

        DeveloperSupportResolution resolution = documentAnalyticsService.resolveSupportQuery(query);
        return ResponseEntity.ok(ApiResponse.ok(DeveloperSupportResolutionDto.fromDomain(resolution)));
    }
}
