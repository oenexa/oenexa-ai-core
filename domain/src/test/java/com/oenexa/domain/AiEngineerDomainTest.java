package com.oenexa.domain;

import com.oenexa.domain.AiEngineerDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiEngineerDomain Zero-Mock Unit Tests")
class AiEngineerDomainTest {

    @Test
    @DisplayName("Should validate PrReviewResult contract invariants")
    void shouldValidatePrReviewResult() {
        var comment = new PrReviewFileComment("src/App.java", 10, "HIGH", "SECURITY", "Fix SQL injection", "");
        var result = new PrReviewResult(
                "rev-1",
                ReviewVerdict.APPROVE,
                95,
                98,
                "Clean PR review",
                List.of(comment),
                List.of("Zero-mock compliant"),
                List.of(),
                true,
                null
        );

        assertEquals("rev-1", result.reviewId());
        assertEquals(ReviewVerdict.APPROVE, result.verdict());
        assertEquals(95, result.qualityScore());
        assertTrue(result.zeroMockCompliant());
        assertEquals(1, result.lineComments().size());
    }

    @Test
    @DisplayName("Should validate ApiGenerationRequest and Result invariants")
    void shouldValidateApiGenerationContracts() {
        var req = new ApiGenerationRequest("Deployment", SupportedLanguage.JAVA, SupportedFramework.SPRING_BOOT, List.of("id:UUID", "name:String"), List.of("CREATE", "GET"));
        assertEquals("Deployment", req.entityName());
        assertEquals(SupportedFramework.SPRING_BOOT, req.framework());

        var res = new ApiGenerationResult(
                "Deployment",
                SupportedLanguage.JAVA,
                SupportedFramework.SPRING_BOOT,
                "public class DeploymentController {}",
                "public record Deployment() {}",
                "openapi: 3.1.0",
                "class DeploymentTest {}",
                null
        );
        assertEquals("Deployment", res.entityName());
        assertNotNull(res.controllerCode());
        assertNotNull(res.openApiSpecYaml());
    }

    @Test
    @DisplayName("Should validate BugFixResult and RefactoringResult contracts")
    void shouldValidateBugFixAndRefactoring() {
        var bugResult = new BugFixResult("fixed", "Root cause", "diff", "test", true, null);
        assertTrue(bugResult.verified());
        assertEquals("Root cause", bugResult.rootCauseAnalysis());

        var refactResult = new RefactoringResult("refactored", "diff", List.of("Immutable record"), 10, 2, true, null);
        assertEquals(10, refactResult.cyclomaticComplexityBefore());
        assertEquals(2, refactResult.cyclomaticComplexityAfter());
        assertTrue(refactResult.backwardsCompatible());
    }

    @Test
    @DisplayName("Should validate InfrastructureAutomationResult contracts")
    void shouldValidateInfrastructureAutomation() {
        var result = new InfrastructureAutomationResult("oenexa-auth", "FROM alpine", "apiVersion: v1", "name: CI", "services:", null);
        assertEquals("oenexa-auth", result.applicationName());
        assertTrue(result.dockerfile().contains("FROM"));
    }
}
