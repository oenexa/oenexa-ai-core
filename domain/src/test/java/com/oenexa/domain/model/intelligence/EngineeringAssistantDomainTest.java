package com.oenexa.domain.model.intelligence;

import com.oenexa.domain.model.intelligence.EngineeringAssistantDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EngineeringAssistantDomain Zero-Mock Unit Tests")
class EngineeringAssistantDomainTest {

    @Test
    @DisplayName("Should create RepositoryAnalysisReport with valid invariant bounds")
    void shouldCreateRepositoryAnalysisReport() {
        RepositoryAnalysisReport report = new RepositoryAnalysisReport(
                "rep-001",
                "oenexa-ai-core",
                "CleanHexagonalModularMonolith",
                List.of("domain", "application", "infrastructure", "api"),
                List.of(),
                0.95,
                "Clean architecture verified with zero violations.",
                null
        );

        assertEquals("rep-001", report.reportId());
        assertEquals("oenexa-ai-core", report.repositoryName());
        assertEquals(4, report.identifiedModules().size());
        assertTrue(report.architecturalViolations().isEmpty());
        assertEquals(0.95, report.maintainabilityScore());
        assertNotNull(report.generatedAt());
    }

    @Test
    @DisplayName("Should assess DevOps review report critical risk invariant")
    void shouldAssessDevOpsReviewReport() {
        DevOpsReviewReport cleanReport = new DevOpsReviewReport(
                "devops-001",
                "oenexa-api",
                "production",
                List.of(),
                List.of(),
                List.of(),
                true,
                null
        );
        assertFalse(cleanReport.hasCriticalRisks());

        DevOpsReviewReport riskyReport = new DevOpsReviewReport(
                "devops-002",
                "oenexa-api",
                "production",
                List.of("Vulnerable base image: debian-10"),
                List.of("Root container privileges allowed"),
                List.of(),
                false,
                null
        );
        assertTrue(riskyReport.hasCriticalRisks());
    }

    @Test
    @DisplayName("Should enforce compliance invariant on SecurityAuditReport")
    void shouldEnforceComplianceInvariantOnSecurityAudit() {
        SecurityAuditReport cleanAudit = new SecurityAuditReport(
                "sec-001",
                "oenexa-ai-core",
                0,
                0,
                List.of(),
                List.of(),
                true,
                null
        );
        assertTrue(cleanAudit.passCompliance());

        SecurityAuditReport failingAudit = new SecurityAuditReport(
                "sec-002",
                "oenexa-ai-core",
                1,
                2,
                List.of("SQL Injection in legacy repository"),
                List.of("ghp_fakeTokenFound"),
                false,
                null
        );
        assertFalse(failingAudit.passCompliance());
    }

    @Test
    @DisplayName("Should generate DocumentationDraft with markdown envelope")
    void shouldGenerateDocumentationDraft() {
        DocumentationDraft doc = new DocumentationDraft(
                "doc-adr-001",
                "ADR 001: Clean Hexagonal Architecture Invariant",
                DocType.ADR_RECORD,
                "# ADR 001\nDecided to enforce 11-layer architecture across all modules.",
                List.of("ARCHITECTURE.md"),
                null
        );

        assertEquals("doc-adr-001", doc.documentId());
        assertEquals(DocType.ADR_RECORD, doc.docType());
        assertTrue(doc.markdownContent().contains("ADR 001"));
        assertEquals(1, doc.referencedFiles().size());
        assertNotNull(doc.createdDate());
    }
}
