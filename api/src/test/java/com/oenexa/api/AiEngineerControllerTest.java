package com.oenexa.api;

import com.oenexa.application.AiSoftwareEngineerService;
import com.oenexa.domain.AiEngineerDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiEngineerController Zero-Mock Unit Tests")
class AiEngineerControllerTest {

    private AiEngineerController controller;

    @BeforeEach
    void setUp() {
        controller = new AiEngineerController(new AiSoftwareEngineerService());
    }

    @Test
    @DisplayName("Should review pull request via REST endpoint")
    void shouldReviewPullRequest() {
        var req = new PrReviewRequest("PR-101", "Update auth", "main", "feat", "+ public record Token()");
        ResponseEntity<PrReviewResult> response = controller.reviewPullRequest(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ReviewVerdict.APPROVE, response.getBody().verdict());
    }

    @Test
    @DisplayName("Should generate API via REST endpoint")
    void shouldGenerateApi() {
        var req = new ApiGenerationRequest("Order", SupportedLanguage.JAVA, SupportedFramework.SPRING_BOOT, List.of("id:UUID", "status:String"), List.of("CREATE"));
        ResponseEntity<ApiGenerationResult> response = controller.generateApi(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order", response.getBody().entityName());
    }

    @Test
    @DisplayName("Should refactor code via REST endpoint")
    void shouldRefactorCode() {
        var req = new RefactoringRequest("class A { String name; }", "RECORD", SupportedLanguage.JAVA);
        ResponseEntity<RefactoringResult> response = controller.refactorCode(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().backwardsCompatible());
    }

    @Test
    @DisplayName("Should fix bug via REST endpoint")
    void shouldFixBug() {
        var req = new BugFixRequest("map.get(key)", "NullPointerException", "Safe get");
        ResponseEntity<BugFixResult> response = controller.fixBug(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().verified());
    }

    @Test
    @DisplayName("Should generate infrastructure via REST endpoint")
    void shouldGenerateInfrastructure() {
        var req = new InfrastructureAutomationRequest("oenexa-core", SupportedFramework.SPRING_BOOT, 8080, true, true);
        ResponseEntity<InfrastructureAutomationResult> response = controller.generateInfrastructure(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("oenexa-core", response.getBody().applicationName());
    }
}
