package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.intelligence.EngineeringAssistantDtos.*;
import com.oenexa.application.intelligence.EngineeringAssistantService;
import com.oenexa.domain.model.intelligence.EngineeringAssistantDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Enterprise Engineering Assistant REST API Controller.
 * Provides specialized internal assistance for all OENEXA engineers:
 * 1. Repository Architecture & Quality Analysis
 * 2. DevOps & Infrastructure Risk Reviews
 * 3. Security, OWASP & Credential Leak Audits
 * 4. Technical Documentation Synthesis (ADRs, Runbooks)
 */
@RestController
@RequestMapping("/api/v4/assistant")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class EngineeringAssistantController {

    private final EngineeringAssistantService assistantService;

    public EngineeringAssistantController(EngineeringAssistantService assistantService) {
        this.assistantService = Objects.requireNonNull(assistantService, "assistantService must not be null");
    }

    @PostMapping("/repository/analyze")
    public ResponseEntity<ApiResponse<RepositoryAnalysisReport>> analyzeRepository(
            @RequestBody(required = false) AnalyzeRepoRequest request
    ) {
        String repoName = (request != null) ? request.repositoryName() : "oenexa-default";
        var files = (request != null) ? request.files() : null;
        String code = (request != null) ? request.sampleCode() : "";

        RepositoryAnalysisReport report = assistantService.analyzeRepository(repoName, files, code);
        return ResponseEntity.ok(ApiResponse.ok(report));
    }

    @PostMapping("/devops/review")
    public ResponseEntity<ApiResponse<DevOpsReviewReport>> reviewDevOps(
            @RequestBody(required = false) ReviewDevOpsRequest request
    ) {
        String appName = (request != null) ? request.applicationName() : "oenexa-service";
        String dockerfile = (request != null) ? request.dockerfileContent() : "";
        String k8s = (request != null) ? request.kubernetesYamlContent() : "";

        DevOpsReviewReport report = assistantService.reviewDevOps(appName, dockerfile, k8s);
        return ResponseEntity.ok(ApiResponse.ok(report));
    }

    @PostMapping("/security/audit")
    public ResponseEntity<ApiResponse<SecurityAuditReport>> auditSecurity(
            @RequestBody(required = false) SecurityAuditRequest request
    ) {
        String scope = (request != null) ? request.targetScope() : "default-scope";
        String code = (request != null) ? request.codeContent() : "";

        SecurityAuditReport report = assistantService.auditSecurity(scope, code);
        return ResponseEntity.ok(ApiResponse.ok(report));
    }

    @PostMapping("/docs/generate")
    public ResponseEntity<ApiResponse<DocumentationDraft>> generateDocumentation(
            @RequestBody(required = false) GenerateDocRequest request
    ) {
        String title = (request != null) ? request.title() : "Architecture Specification";
        DocType type = (request != null && request.docType() != null) ? request.docType() : DocType.ARCHITECTURE_SPEC;
        String context = (request != null) ? request.context() : "Standard enterprise system.";

        DocumentationDraft draft = assistantService.generateDocumentation(title, type, context);
        return ResponseEntity.ok(ApiResponse.ok(draft));
    }
}
