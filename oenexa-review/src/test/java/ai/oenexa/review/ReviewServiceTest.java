package ai.oenexa.review;

import ai.oenexa.review.domain.PullRequest;
import ai.oenexa.review.domain.QualityScore;
import ai.oenexa.review.domain.ReviewComment;
import ai.oenexa.review.domain.ReviewResult;
import ai.oenexa.review.domain.RiskScore;
import ai.oenexa.review.service.DefaultReviewService;
import ai.oenexa.review.web.ReviewController;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OENEXA Review Service & Controller Tests (Zero-Mock Policy)")
class ReviewServiceTest {

    private DefaultReviewService reviewService;
    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        reviewService = new DefaultReviewService();
        reviewController = new ReviewController(reviewService);
    }

    @Test
    @DisplayName("QualityScore clamping and acceptability logic")
    void testQualityScore() {
        QualityScore qs = new QualityScore(120, -10, 85, 90);
        assertEquals(100, qs.maintainability());
        assertEquals(0, qs.reliability());
        assertEquals(85, qs.security());
        assertTrue(new QualityScore(80, 80, 80, 80).isAcceptable());
        assertFalse(new QualityScore(60, 60, 60, 60).isAcceptable());
    }

    @Test
    @DisplayName("RiskScore level thresholds and human approval requirement")
    void testRiskScore() {
        RiskScore low = new RiskScore(RiskScore.RiskLevel.LOW, 5, List.of());
        assertFalse(low.requiresHumanApproval());

        RiskScore crit = new RiskScore(RiskScore.RiskLevel.CRITICAL, 60, List.of("Auth changed"));
        assertTrue(crit.requiresHumanApproval());
    }

    @Test
    @DisplayName("Review clean pull request without violations")
    void testCleanPullRequest() {
        String cleanDiff = """
                diff --git a/Service.java b/Service.java
                --- a/Service.java
                +++ b/Service.java
                @@ -1,3 +1,4 @@
                +public record UserData(String id) {}
                """;
        PullRequest pr = new PullRequest("pr-101", "oenexa-repo", "feature/record", "Add UserData", cleanDiff);
        ReviewResult result = reviewService.reviewPullRequest(pr);

        assertEquals("pr-101", result.prId());
        assertTrue(result.approved());
        assertEquals(0, result.comments().size());
        assertEquals(RiskScore.RiskLevel.LOW, result.riskScore().level());
    }

    @Test
    @DisplayName("Review diff with stdout prints and hardcoded secret")
    void testDiffWithViolations() {
        String riskyDiff = """
                diff --git a/App.java b/App.java
                --- a/App.java
                +++ b/App.java
                @@ -10,3 +10,6 @@
                +System.out.println("debugging");
                +// TODO: resolve this before prod
                +String apiKey = "sk-live-1234567890abcdef";
                """;
        ReviewResult result = reviewService.reviewDiff(riskyDiff);

        assertFalse(result.approved());
        assertTrue(result.comments().size() >= 3);
        assertTrue(result.errorCount() >= 1);
        assertTrue(result.warningCount() >= 1);
    }

    @Test
    @DisplayName("Review diff touching database migrations triggers CRITICAL or HIGH risk")
    void testDatabaseMigrationRisk() {
        String migrationDiff = """
                diff --git a/V2__schema.sql b/V2__schema.sql
                --- a/V2__schema.sql
                +++ b/V2__schema.sql
                @@ -1,2 +1,3 @@
                +ALTER TABLE users ADD COLUMN phone VARCHAR(20);
                """;
        PullRequest pr = new PullRequest("pr-migration", "backend", "db-upgrade", "Update DB", migrationDiff);
        ReviewResult result = reviewService.reviewPullRequest(pr);

        assertTrue(result.riskScore().blastRadiusScore() >= 30);
        assertTrue(result.riskScore().riskFactors().stream().anyMatch(f -> f.contains("migration")));
    }

    @Test
    @DisplayName("ReviewController endpoints return valid HTTP responses")
    void testReviewController() {
        PullRequest pr = new PullRequest("pr-controller", "repo", "main", "Test", "");
        ResponseEntity<ReviewResult> prResp = reviewController.reviewPullRequest(pr);
        assertEquals(200, prResp.getStatusCode().value());
        assertNotNull(prResp.getBody());

        ResponseEntity<ReviewResult> diffResp = reviewController.reviewDiff(Map.of("diff", ""));
        assertEquals(200, diffResp.getStatusCode().value());
        assertNotNull(diffResp.getBody());
    }

    @Test
    @DisplayName("ReviewModule metadata and initialization")
    void testReviewModuleMetadata() {
        ReviewModule module = new ReviewModule();
        assertEquals("review", module.id());
        assertEquals("PR Review Engine", module.displayName());
        assertTrue(module.capabilities().contains("risk-score"));
    }

    @Test
    @DisplayName("Domain records nullability and fallback invariants")
    void testDomainRecordsEdgeCases() {
        ReviewComment comment = new ReviewComment("File.java", 10, null, "message", null);
        assertEquals(ReviewComment.Severity.INFO, comment.severity());
        assertEquals("", comment.suggestedFix());

        RiskScore score = new RiskScore(RiskScore.RiskLevel.HIGH, 35, null);
        assertNotNull(score.riskFactors());
        assertTrue(score.requiresHumanApproval());

        ReviewResult result = new ReviewResult("pr-1", true, "summary", new QualityScore(90, 90, 90, 90), score, null);
        assertEquals(0, result.comments().size());
        assertEquals(0, result.warningCount());
        assertEquals(0, result.errorCount());
    }

    @Test
    @DisplayName("Review diff with build files and critical risk combinations")
    void testBuildFilesAndCriticalRisk() {
        String buildDiff = """
                diff --git a/build.gradle.kts b/build.gradle.kts
                --- a/build.gradle.kts
                +++ b/build.gradle.kts
                @@ -1,1 +1,2 @@
                +// update dependencies
                """;
        ReviewResult buildResult = reviewService.reviewDiff(buildDiff);
        assertEquals(RiskScore.RiskLevel.MEDIUM, buildResult.riskScore().level());
        assertTrue(buildResult.riskScore().riskFactors().stream().anyMatch(f -> f.contains("Build or deployment")));

        String criticalDiff = """
                diff --git a/V1__schema.sql b/V1__schema.sql
                --- a/V1__schema.sql
                +++ b/V1__schema.sql
                @@ -1,1 +1,2 @@
                +CREATE TABLE test ();
                diff --git a/SecurityConfig.java b/SecurityConfig.java
                --- a/SecurityConfig.java
                +++ b/SecurityConfig.java
                @@ -1,1 +1,2 @@
                +// security update
                """;
        ReviewResult critResult = reviewService.reviewDiff(criticalDiff);
        assertEquals(RiskScore.RiskLevel.CRITICAL, critResult.riskScore().level());
        assertFalse(critResult.approved());
    }
}
