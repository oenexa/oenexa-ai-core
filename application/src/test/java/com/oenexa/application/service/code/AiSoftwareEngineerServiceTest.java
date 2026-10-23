package com.oenexa.application.service.code;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.code.AiEngineerDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiSoftwareEngineerService Zero-Mock Unit Tests")
class AiSoftwareEngineerServiceTest {

    private AiSoftwareEngineerService engineerService;

    @BeforeEach
    void setUp() {
        engineerService = new AiSoftwareEngineerService();
    }

    @Test
    @DisplayName("Should detect mock framework violations in PR diff and request changes")
    void shouldDetectMockViolationInPr() {
        String diffWithMockito = """
                + import org.mockito.Mockito;
                + when(userRepo.findById(any())).thenReturn(Optional.of(user));
                """;
        var request = new PrReviewRequest("Feature: User Service", "Added mock testing", "main", "feat", diffWithMockito);
        PrReviewResult result = engineerService.reviewPullRequest(request);

        assertNotNull(result);
        assertEquals(ReviewVerdict.REQUEST_CHANGES, result.verdict());
        assertFalse(result.zeroMockCompliant());
        assertTrue(result.lineComments().stream().anyMatch(c -> c.category().equals("ZERO_MOCK_POLICY")));
    }

    @Test
    @DisplayName("Should approve clean PR diff adhering to zero-mock standard")
    void shouldApproveCleanPr() {
        String cleanDiff = """
                + public record UserProfile(UUID id, String email) {
                +     public UserProfile {
                +         Objects.requireNonNull(id, "id must not be null");
                +     }
                + }
                """;
        var request = new PrReviewRequest("Feature: Domain Profile", "Added records", "main", "feat", cleanDiff);
        PrReviewResult result = engineerService.reviewPullRequest(request);

        assertNotNull(result);
        assertEquals(ReviewVerdict.APPROVE, result.verdict());
        assertTrue(result.zeroMockCompliant());
        assertTrue(result.qualityScore() >= 90);
    }

    @Test
    @DisplayName("Should generate complete REST API with zero-mock tests and OpenAPI spec")
    void shouldGenerateRestApi() {
        var request = new ApiGenerationRequest("Cluster", SupportedLanguage.JAVA, SupportedFramework.SPRING_BOOT, List.of("id:UUID", "name:String"), List.of("CREATE", "GET"));
        ApiGenerationResult result = engineerService.generateApi(request);

        assertNotNull(result);
        assertEquals("Cluster", result.entityName());
        assertTrue(result.controllerCode().contains("ClusterController"));
        assertTrue(result.domainModelCode().contains("record Cluster"));
        assertTrue(result.openApiSpecYaml().contains("openapi: 3.1.0"));
        assertTrue(result.unitTestCode().contains("ClusterControllerTest"));
    }

    @Test
    @DisplayName("Should refactor mutable classes to defensive immutable records")
    void shouldRefactorMutableClass() {
        String pojo = """
                public class LegacyUser {
                    private String name;
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                }
                """;
        var request = new RefactoringRequest(pojo, "IMMUTABILITY", SupportedLanguage.JAVA);
        RefactoringResult result = engineerService.refactorCode(request);

        assertNotNull(result);
        assertTrue(result.refactoredCode().contains("record RefactoredEntity"));
        assertTrue(result.cyclomaticComplexityAfter() < result.cyclomaticComplexityBefore());
    }

    @Test
    @DisplayName("Should autonomously diagnose and fix NullPointerException with regression test")
    void shouldFixNpeBug() {
        String failingCode = "return map.get(key).toUpperCase();";
        String error = "java.lang.NullPointerException: Cannot invoke String.toUpperCase() on null";
        var request = new BugFixRequest(failingCode, error, "Return empty string if missing");

        BugFixResult result = engineerService.fixBug(request);
        assertNotNull(result);
        assertTrue(result.verified());
        assertTrue(result.rootCauseAnalysis().contains("Unchecked dereference"));
        assertTrue(result.fixedCode().contains("Optional.ofNullable"));
        assertTrue(result.regressionTestCode().contains("SafeProcessorRegressionTest"));
    }

    @Test
    @DisplayName("Should generate complete infrastructure: Dockerfile, K8s manifests, and CI/CD workflow")
    void shouldGenerateInfrastructure() {
        var request = new InfrastructureAutomationRequest("oenexa-auth", SupportedFramework.SPRING_BOOT, 8080, true, true);
        InfrastructureAutomationResult result = engineerService.generateInfrastructure(request);

        assertNotNull(result);
        assertEquals("oenexa-auth", result.applicationName());
        assertTrue(result.dockerfile().contains("distroless"));
        assertTrue(result.kubernetesManifestYaml().contains("kind: Deployment"));
        assertTrue(result.kubernetesManifestYaml().contains("kind: Service"));
        assertTrue(result.githubActionsWorkflowYaml().contains("Enforce Zero-Mock Test Suite"));
        assertTrue(result.dockerComposeYaml().contains("postgres:17"));
    }
}


