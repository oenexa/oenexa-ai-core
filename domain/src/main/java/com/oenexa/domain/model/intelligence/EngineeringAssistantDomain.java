package com.oenexa.domain.model.intelligence;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Enterprise Engineering Assistant Domain records and specifications.
 * Encompasses:
 * 1. Repository Architecture & Code Quality Analysis
 * 2. DevOps & Infrastructure Risk Reviews (K8s, Docker, Terraform)
 * 3. Security & OWASP Compliance Audits
 * 4. Technical Documentation Synthesis (ADRs, Runbooks, Architecture Specs)
 * Pure Java 25 Domain Model (Zero framework dependencies).
 */
public final class EngineeringAssistantDomain {

    private EngineeringAssistantDomain() {}

    public enum AnalysisType {
        ARCHITECTURE_REVIEW,
        CODE_EXPLANATION,
        BUG_DETECTION,
        TEST_GENERATION,
        PR_SUMMARY
    }

    public enum DocType {
        ARCHITECTURE_SPEC,
        API_SPECIFICATION,
        RUNBOOK,
        ADR_RECORD,
        DEVELOPER_GUIDE
    }

    public record RepositoryAnalysisReport(
            String reportId,
            String repositoryName,
            String architectureClassification,
            List<String> identifiedModules,
            List<String> architecturalViolations,
            double maintainabilityScore,
            String executiveSummary,
            Instant generatedAt
    ) {
        public RepositoryAnalysisReport {
            reportId = (reportId != null) ? reportId : "repo-rep-" + UUID.randomUUID();
            Objects.requireNonNull(repositoryName, "repositoryName must not be null");
            architectureClassification = (architectureClassification != null) ? architectureClassification : "CleanHexagonal";
            identifiedModules = (identifiedModules != null) ? List.copyOf(identifiedModules) : List.of();
            architecturalViolations = (architecturalViolations != null) ? List.copyOf(architecturalViolations) : List.of();
            maintainabilityScore = Math.clamp(maintainabilityScore, 0.0, 1.0);
            executiveSummary = (executiveSummary != null) ? executiveSummary : "Repository complies with OENEXA standards.";
            generatedAt = (generatedAt != null) ? generatedAt : Instant.now();
        }
    }

    public record DevOpsReviewReport(
            String reviewId,
            String applicationName,
            String environment,
            List<String> containerVulnerabilities,
            List<String> kubernetesMisconfigurations,
            List<String> ciCdDeploymentRisks,
            boolean productionReady,
            Instant reviewedAt
    ) {
        public DevOpsReviewReport {
            reviewId = (reviewId != null) ? reviewId : "devops-rev-" + UUID.randomUUID();
            Objects.requireNonNull(applicationName, "applicationName must not be null");
            environment = (environment != null) ? environment : "production";
            containerVulnerabilities = (containerVulnerabilities != null) ? List.copyOf(containerVulnerabilities) : List.of();
            kubernetesMisconfigurations = (kubernetesMisconfigurations != null) ? List.copyOf(kubernetesMisconfigurations) : List.of();
            ciCdDeploymentRisks = (ciCdDeploymentRisks != null) ? List.copyOf(ciCdDeploymentRisks) : List.of();
            reviewedAt = (reviewedAt != null) ? reviewedAt : Instant.now();
        }

        public boolean hasCriticalRisks() {
            return !containerVulnerabilities.isEmpty() || !kubernetesMisconfigurations.isEmpty();
        }
    }

    public record SecurityAuditReport(
            String auditId,
            String targetScope,
            int criticalCount,
            int highCount,
            List<String> owaspViolations,
            List<String> detectedSecrets,
            boolean passCompliance,
            Instant auditedAt
    ) {
        public SecurityAuditReport {
            auditId = (auditId != null) ? auditId : "sec-aud-" + UUID.randomUUID();
            Objects.requireNonNull(targetScope, "targetScope must not be null");
            criticalCount = Math.max(0, criticalCount);
            highCount = Math.max(0, highCount);
            owaspViolations = (owaspViolations != null) ? List.copyOf(owaspViolations) : List.of();
            detectedSecrets = (detectedSecrets != null) ? List.copyOf(detectedSecrets) : List.of();
            passCompliance = (criticalCount == 0 && highCount == 0 && detectedSecrets.isEmpty());
            auditedAt = (auditedAt != null) ? auditedAt : Instant.now();
        }
    }

    public record DocumentationDraft(
            String documentId,
            String title,
            DocType docType,
            String markdownContent,
            List<String> referencedFiles,
            Instant createdDate
    ) {
        public DocumentationDraft {
            documentId = (documentId != null) ? documentId : "doc-" + UUID.randomUUID();
            Objects.requireNonNull(title, "title must not be null");
            Objects.requireNonNull(docType, "docType must not be null");
            markdownContent = (markdownContent != null) ? markdownContent : "# " + title;
            referencedFiles = (referencedFiles != null) ? List.copyOf(referencedFiles) : List.of();
            createdDate = (createdDate != null) ? createdDate : Instant.now();
        }
    }
}
