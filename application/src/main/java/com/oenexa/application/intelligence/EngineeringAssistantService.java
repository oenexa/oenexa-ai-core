package com.oenexa.application.intelligence;

import com.oenexa.application.engine.AstAnalysisEngine;
import com.oenexa.application.engine.InfrastructureAutomationEngine;
import com.oenexa.application.engine.PullRequestReviewEngine;
import com.oenexa.application.engine.SecurityAuditEngine;
import com.oenexa.domain.model.code.AiEngineerDomain.PrReviewRequest;
import com.oenexa.domain.model.code.AiEngineerDomain.PrReviewResult;
import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.domain.model.code.CodeEngineeringDomain.SecurityScanRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.SecurityScanResponse;
import com.oenexa.domain.model.intelligence.EngineeringAssistantDomain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Enterprise Engineering Assistant Application Service.
 * Central coordinator for:
 * 1. Repository Architecture & AST Analysis
 * 2. DevOps & Cloud Infrastructure Assessment
 * 3. Security, OWASP & Credential Leak Audits
 * 4. Technical Documentation Synthesis (ADRs, Runbooks)
 */
@Service
public class EngineeringAssistantService {

    private final AstAnalysisEngine astEngine;
    private final SecurityAuditEngine securityEngine;
    private final InfrastructureAutomationEngine infraEngine;
    private final PullRequestReviewEngine prReviewEngine;

    public EngineeringAssistantService() {
        this(new AstAnalysisEngine(), new SecurityAuditEngine(), new InfrastructureAutomationEngine(), new PullRequestReviewEngine());
    }

    public EngineeringAssistantService(
            AstAnalysisEngine astEngine,
            SecurityAuditEngine securityEngine,
            InfrastructureAutomationEngine infraEngine,
            PullRequestReviewEngine prReviewEngine
    ) {
        this.astEngine = Objects.requireNonNull(astEngine, "astEngine must not be null");
        this.securityEngine = Objects.requireNonNull(securityEngine, "securityEngine must not be null");
        this.infraEngine = Objects.requireNonNull(infraEngine, "infraEngine must not be null");
        this.prReviewEngine = Objects.requireNonNull(prReviewEngine, "prReviewEngine must not be null");
    }

    public RepositoryAnalysisReport analyzeRepository(String repositoryName, List<String> fileList, String sampleCode) {
        String repoName = (repositoryName != null && !repositoryName.isBlank()) ? repositoryName : "oenexa-core";
        List<String> files = (fileList != null) ? fileList : List.of();
        String code = (sampleCode != null) ? sampleCode : "";

        List<String> violations = new ArrayList<>();
        double maintainability = 0.92;

        if (!code.isBlank()) {
            CodeAnalysisResponse ast = astEngine.analyze(new CodeAnalysisRequest(code, "java"));
            if (ast.cyclomaticComplexity() > 15) {
                violations.add("High cyclomatic complexity (" + ast.cyclomaticComplexity() + ") detected in primary class.");
                maintainability -= 0.10;
            }
        }

        List<String> detectedModules = files.stream()
                .filter(f -> f.contains("/"))
                .map(f -> f.split("/")[0])
                .distinct()
                .toList();

        return new RepositoryAnalysisReport(
                UUID.randomUUID().toString(),
                repoName,
                "CleanHexagonalModularMonolith",
                detectedModules.isEmpty() ? List.of("domain", "application", "infrastructure", "api") : detectedModules,
                violations,
                maintainability,
                "Repository conforms to OENEXA enterprise governance standards.",
                Instant.now()
        );
    }

    public DevOpsReviewReport reviewDevOps(String applicationName, String dockerfileContent, String k8sYamlContent) {
        String app = (applicationName != null && !applicationName.isBlank()) ? applicationName : "oenexa-app";
        List<String> containerIssues = new ArrayList<>();
        List<String> k8sRisks = new ArrayList<>();

        if (dockerfileContent != null) {
            if (dockerfileContent.contains("USER root") || !dockerfileContent.contains("USER ")) {
                containerIssues.add("Dockerfile executes as root user; nonroot distroless user required.");
            }
            if (dockerfileContent.contains(":latest")) {
                containerIssues.add("Base image uses mutable ':latest' tag instead of pinned immutable digest.");
            }
        }

        if (k8sYamlContent != null) {
            if (!k8sYamlContent.contains("readinessProbe")) {
                k8sRisks.add("Missing readinessProbe in Kubernetes deployment spec.");
            }
            if (!k8sYamlContent.contains("limits:") || !k8sYamlContent.contains("requests:")) {
                k8sRisks.add("Missing CPU and Memory resource limits and requests.");
            }
        }

        boolean productionReady = containerIssues.isEmpty() && k8sRisks.isEmpty();

        return new DevOpsReviewReport(
                UUID.randomUUID().toString(),
                app,
                "production",
                containerIssues,
                k8sRisks,
                List.of(),
                productionReady,
                Instant.now()
        );
    }

    public SecurityAuditReport auditSecurity(String targetScope, String codeContent) {
        String scope = (targetScope != null && !targetScope.isBlank()) ? targetScope : "global";
        String code = (codeContent != null) ? codeContent : "";

        SecurityScanResponse scan = securityEngine.scan(new SecurityScanRequest(code, scope + ".java"));

        return new SecurityAuditReport(
                UUID.randomUUID().toString(),
                scope,
                scan.criticalCount(),
                scan.highCount(),
                scan.vulnerabilities(),
                scan.secrets(),
                scan.criticalCount() == 0 && scan.highCount() == 0 && scan.secrets().isEmpty(),
                Instant.now()
        );
    }

    public DocumentationDraft generateDocumentation(String title, DocType docType, String technicalContext) {
        String docTitle = (title != null && !title.isBlank()) ? title : "Architecture Specification";
        DocType type = (docType != null) ? docType : DocType.ARCHITECTURE_SPEC;

        String markdown = switch (type) {
            case ADR_RECORD -> String.format("""
                    # %s
                    
                    ## Status
                    Accepted
                    
                    ## Context
                    %s
                    
                    ## Decision
                    We adhere strictly to the 11-Layer Clean Hexagonal Architecture and Zero-Mock testing standards.
                    
                    ## Consequences
                    - High test reliability and maintainability.
                    - Zero carrier thread pinning using Java 25 Virtual Threads.
                    """, docTitle, technicalContext);
            case RUNBOOK -> String.format("""
                    # Operational Runbook: %s
                    
                    ## Severity Level: P1 / P2
                    
                    ## Trigger & Diagnosis
                    %s
                    
                    ## Recovery Procedure
                    1. Inspect logs via distributed tracing.
                    2. Check heap usage and database connection pool health.
                    3. Restart pod gracefully: `kubectl rollout restart deployment/%s`.
                    """, docTitle, technicalContext, docTitle.toLowerCase().replace(" ", "-"));
            default -> String.format("""
                    # %s
                    
                    ## Executive Summary
                    %s
                    
                    ## Architecture Blueprint
                    Follows Clean Hexagonal Modular Monolith (`domain`, `application`, `infrastructure`, `api`).
                    """, docTitle, technicalContext);
        };

        return new DocumentationDraft(
                UUID.randomUUID().toString(),
                docTitle,
                type,
                markdown,
                List.of("ARCHITECTURE.md", "DEVELOPMENT_NOTES.md"),
                Instant.now()
        );
    }

    public PrReviewResult reviewPullRequest(PrReviewRequest request) {
        return prReviewEngine.reviewPr(request);
    }
}
