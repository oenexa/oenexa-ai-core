package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.intelligence.EngineeringAssistantDtos.*;
import com.oenexa.application.intelligence.EngineeringAssistantService;
import com.oenexa.domain.model.intelligence.EngineeringAssistantDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EngineeringAssistantController Zero-Mock Unit Tests")
class EngineeringAssistantControllerTest {

    private EngineeringAssistantController controller;

    @BeforeEach
    void setUp() {
        controller = new EngineeringAssistantController(new EngineeringAssistantService());
    }

    @Test
    @DisplayName("Should analyze repository and return report via REST endpoint")
    void shouldAnalyzeRepository() {
        AnalyzeRepoRequest req = new AnalyzeRepoRequest(
                "oenexa-node",
                List.of("core/blockchain.go", "rpc/server.go"),
                "public class Valid { }"
        );

        ResponseEntity<ApiResponse<RepositoryAnalysisReport>> resp = controller.analyzeRepository(req);

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().success());
        assertEquals("oenexa-node", resp.getBody().data().repositoryName());
    }

    @Test
    @DisplayName("Should review DevOps manifests via REST endpoint")
    void shouldReviewDevOps() {
        ReviewDevOpsRequest req = new ReviewDevOpsRequest(
                "oenexa-api",
                "FROM eclipse-temurin:25-jdk\nUSER nonroot",
                "apiVersion: apps/v1\nkind: Deployment\nspec:\n  template:\n    spec:\n      containers:\n      - name: app\n        readinessProbe:\n          httpGet:\n            path: /health\n        resources:\n          limits:\n            cpu: 1\n          requests:\n            cpu: 500m"
        );

        ResponseEntity<ApiResponse<DevOpsReviewReport>> resp = controller.reviewDevOps(req);

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().success());
        assertTrue(resp.getBody().data().productionReady());
    }

    @Test
    @DisplayName("Should audit security and return vulnerability count")
    void shouldAuditSecurity() {
        SecurityAuditRequest req = new SecurityAuditRequest(
                "checkout-controller",
                "String key = \"ghp_123456789012345678901234567890123456\";"
        );

        ResponseEntity<ApiResponse<SecurityAuditReport>> resp = controller.auditSecurity(req);

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().success());
        assertFalse(resp.getBody().data().passCompliance());
    }

    @Test
    @DisplayName("Should generate documentation draft via REST endpoint")
    void shouldGenerateDocumentation() {
        GenerateDocRequest req = new GenerateDocRequest(
                "ADR 003: Redis Context Cache",
                DocType.ADR_RECORD,
                "Need ultra-fast sliding context storage."
        );

        ResponseEntity<ApiResponse<DocumentationDraft>> resp = controller.generateDocumentation(req);

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().success());
        assertEquals(DocType.ADR_RECORD, resp.getBody().data().docType());
    }
}
