package com.oenexa.application.intelligence;

import com.oenexa.domain.model.intelligence.EngineeringAssistantDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EngineeringAssistantService Zero-Mock Unit Tests")
class EngineeringAssistantServiceTest {

    private EngineeringAssistantService assistantService;

    @BeforeEach
    void setUp() {
        assistantService = new EngineeringAssistantService();
    }

    @Test
    @DisplayName("Should analyze repository structure and compute maintainability score")
    void shouldAnalyzeRepositoryStructure() {
        RepositoryAnalysisReport report = assistantService.analyzeRepository(
                "oenexa-node",
                List.of("core/blockchain.go", "p2p/network.go", "rpc/server.go"),
                "public class CleanService { public void execute() { System.out.println(\"clean\"); } }"
        );

        assertNotNull(report);
        assertEquals("oenexa-node", report.repositoryName());
        assertTrue(report.maintainabilityScore() >= 0.85);
        assertTrue(report.identifiedModules().contains("core"));
        assertTrue(report.identifiedModules().contains("p2p"));
    }

    @Test
    @DisplayName("Should detect container and Kubernetes misconfigurations in DevOps review")
    void shouldDetectDevOpsMisconfigurations() {
        String riskyDocker = "FROM ubuntu:latest\nUSER root\nENTRYPOINT [\"app\"]";
        String riskyK8s = "apiVersion: apps/v1\nkind: Deployment\nspec:\n  replicas: 1";

        DevOpsReviewReport report = assistantService.reviewDevOps("payment-service", riskyDocker, riskyK8s);

        assertNotNull(report);
        assertFalse(report.productionReady());
        assertTrue(report.hasCriticalRisks());
        assertTrue(report.containerVulnerabilities().stream().anyMatch(v -> v.contains("root")));
        assertTrue(report.kubernetesMisconfigurations().stream().anyMatch(v -> v.contains("readinessProbe")));
    }

    @Test
    @DisplayName("Should conduct security audit and flag AWS keys and secrets")
    void shouldAuditSecurityAndFlagSecrets() {
        String vulnerableCode = "String key = \"AKIA1234567890ABCDEF\";";
        SecurityAuditReport report = assistantService.auditSecurity("auth-service", vulnerableCode);

        assertNotNull(report);
        assertFalse(report.passCompliance());
        assertTrue(report.criticalCount() > 0);
        assertFalse(report.detectedSecrets().isEmpty());
    }

    @Test
    @DisplayName("Should generate structured technical documentation draft")
    void shouldGenerateDocumentationDraft() {
        DocumentationDraft draft = assistantService.generateDocumentation(
                "ADR 002: Java 25 Virtual Threads Baseline",
                DocType.ADR_RECORD,
                "Need high concurrency without thread pool starvation."
        );

        assertNotNull(draft);
        assertEquals(DocType.ADR_RECORD, draft.docType());
        assertTrue(draft.markdownContent().contains("Status"));
        assertTrue(draft.markdownContent().contains("Decision"));
    }
}
