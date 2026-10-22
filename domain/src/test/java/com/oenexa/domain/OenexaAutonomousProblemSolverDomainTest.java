package com.oenexa.domain;

import com.oenexa.domain.OenexaAutonomousProblemSolverDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAutonomousProblemSolverDomain Zero-Mock Tests")
class OenexaAutonomousProblemSolverDomainTest {

    @Test
    @DisplayName("DetectedIssue should enforce non-null invariants and defaults")
    void testDetectedIssueInvariants() {
        assertThrows(NullPointerException.class, () -> new DetectedIssue(
                null, "TEST_TYPE", IssueSeverity.HIGH, "desc", "cause", Instant.now(), Map.of()
        ));
        assertThrows(NullPointerException.class, () -> new DetectedIssue(
                "ISS-1", null, IssueSeverity.HIGH, "desc", "cause", Instant.now(), Map.of()
        ));

        DetectedIssue issue = new DetectedIssue(
                "ISS-1", "NPE", null, null, null, null, null
        );
        assertEquals("ISS-1", issue.issueId());
        assertEquals("NPE", issue.issueType());
        assertEquals(IssueSeverity.MEDIUM, issue.severity());
        assertEquals("", issue.description());
        assertEquals("Under investigation", issue.rootCause());
        assertNotNull(issue.detectedAt());
        assertTrue(issue.telemetry().isEmpty());
    }

    @Test
    @DisplayName("CorrectiveAction should validate invariants")
    void testCorrectiveActionInvariants() {
        assertThrows(NullPointerException.class, () -> new CorrectiveAction(
                null, "ISS-1", "Title", "Desc", RiskLevel.SAFE, true, List.of()
        ));
        assertThrows(NullPointerException.class, () -> new CorrectiveAction(
                "ACT-1", null, "Title", "Desc", RiskLevel.SAFE, true, List.of()
        ));

        CorrectiveAction action = new CorrectiveAction(
                "ACT-1", "ISS-1", null, null, null, true, null
        );
        assertEquals("ACT-1", action.actionId());
        assertEquals("ISS-1", action.targetIssueId());
        assertEquals("Corrective Action", action.title());
        assertEquals("", action.description());
        assertEquals(RiskLevel.SAFE, action.riskLevel());
        assertTrue(action.validated());
        assertTrue(action.executionSteps().isEmpty());
    }

    @Test
    @DisplayName("RankedSolution should enforce rank >= 1 and non-null title")
    void testRankedSolutionInvariants() {
        assertThrows(IllegalArgumentException.class, () -> new RankedSolution(
                0, "Title", "Rationale", 0.9, List.of(), List.of()
        ));
        assertThrows(NullPointerException.class, () -> new RankedSolution(
                1, null, "Rationale", 0.9, List.of(), List.of()
        ));

        RankedSolution sol = new RankedSolution(
                1, "Hexagonal Architecture", null, 0.95, null, null
        );
        assertEquals(1, sol.rank());
        assertEquals("Hexagonal Architecture", sol.title());
        assertEquals("", sol.rationale());
        assertEquals(0.95, sol.effectivenessScore());
        assertTrue(sol.tradeOffs().isEmpty());
        assertTrue(sol.implementationSteps().isEmpty());
    }

    @Test
    @DisplayName("ProcessedQuestion should enforce non-null rawQuestion and safe defaults")
    void testProcessedQuestionInvariants() {
        assertThrows(NullPointerException.class, () -> new ProcessedQuestion(
                null, "intent", "context", "goal", "outcome", List.of()
        ));

        ProcessedQuestion pq = new ProcessedQuestion(
                "How to scale microservices?", null, null, null, null, null
        );
        assertEquals("How to scale microservices?", pq.rawQuestion());
        assertEquals("General Inquiry", pq.inferredIntent());
        assertEquals("Standard Context", pq.technicalContext());
        assertEquals("How to scale microservices?", pq.primaryGoal());
        assertEquals("Actionable Solution", pq.expectedOutcome());
        assertTrue(pq.rankedSolutions().isEmpty());
    }

    @Test
    @DisplayName("ReasoningPattern should record outcomes and compute weight dynamically")
    void testReasoningPatternOutcome() {
        assertThrows(NullPointerException.class, () -> new ReasoningPattern(
                null, "Pattern", "Criteria", List.of(), 0, 0, 0.0
        ));
        assertThrows(NullPointerException.class, () -> new ReasoningPattern(
                "PAT-1", null, "Criteria", List.of(), 0, 0, 0.0
        ));

        ReasoningPattern pat = new ReasoningPattern(
                "PAT-1", "Test Pattern", null, null, 1, 1, 0.5
        );
        assertEquals("PAT-1", pat.patternId());
        assertEquals("", pat.triggerCriteria());
        assertTrue(pat.reasoningSteps().isEmpty());

        ReasoningPattern updatedSuccess = pat.recordOutcome(true);
        assertEquals(2, updatedSuccess.successCount());
        assertEquals(1, updatedSuccess.failureCount());
        assertEquals(2.0 / 3.0, updatedSuccess.confidenceWeight(), 0.001);

        ReasoningPattern updatedFailure = pat.recordOutcome(false);
        assertEquals(1, updatedFailure.successCount());
        assertEquals(2, updatedFailure.failureCount());
        assertEquals(1.0 / 3.0, updatedFailure.confidenceWeight(), 0.001);
    }

    @Test
    @DisplayName("ConceptualModel should enforce non-null modelId and domainArea")
    void testConceptualModelInvariants() {
        assertThrows(NullPointerException.class, () -> new ConceptualModel(null, "Domain", "Hypothesis", List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new ConceptualModel("MOD-1", null, "Hypothesis", List.of(), List.of()));

        ConceptualModel cm = new ConceptualModel("MOD-1", "Distributed Systems", null, null, null);
        assertEquals("MOD-1", cm.modelId());
        assertEquals("Distributed Systems", cm.domainArea());
        assertEquals("", cm.hypothesis());
        assertTrue(cm.coreInvariants().isEmpty());
        assertTrue(cm.alternativePaths().isEmpty());
    }

    @Test
    @DisplayName("SelfOptimizationMetric should handle defaults")
    void testSelfOptimizationMetric() {
        assertThrows(NullPointerException.class, () -> new SelfOptimizationMetric(null, 0.5, 0.8, "none", "none"));

        SelfOptimizationMetric metric = new SelfOptimizationMetric("Latency", 100.0, 85.0, null, null);
        assertEquals("Latency", metric.metricName());
        assertEquals(100.0, metric.baselineScore());
        assertEquals(85.0, metric.evaluatedScore());
        assertEquals("None", metric.weaknessIdentified());
        assertEquals("Maintain current strategy", metric.upgradeStrategy());
    }

    @Test
    @DisplayName("FailureRecoveryPlan should enforce non-null failedObjective")
    void testFailureRecoveryPlan() {
        assertThrows(NullPointerException.class, () -> new FailureRecoveryPlan(null, List.of(), List.of(), List.of(), "fb", "strat"));

        FailureRecoveryPlan plan = new FailureRecoveryPlan("Deploy to k8s", null, null, null, null, null);
        assertEquals("Deploy to k8s", plan.failedObjective());
        assertTrue(plan.missingInformation().isEmpty());
        assertTrue(plan.alternativeMethods().isEmpty());
        assertTrue(plan.decomposedParts().isEmpty());
        assertEquals("Fallback solution formulated", plan.fallbackSolution());
        assertEquals("Strategy updated", plan.futureImprovementStrategy());
    }

    @Test
    @DisplayName("AdaptiveCommunication should apply safe defaults")
    void testAdaptiveCommunication() {
        AdaptiveCommunication comm = new AdaptiveCommunication(null, null, null, null);
        assertEquals(ExpertiseLevel.EXPERT, comm.targetExpertise());
        assertEquals("Authoritative, Structured, Technical", comm.tone());
        assertEquals("Markdown tables, code blocks, bullet points", comm.formattingStrategy());
        assertEquals("", comm.adaptedPayload());
    }

    @Test
    @DisplayName("ContinuousImprovementCycle should handle defaults")
    void testContinuousImprovementCycle() {
        assertThrows(NullPointerException.class, () -> new ContinuousImprovementCycle(null, "req", List.of(), "sol", true, "out", "upg", Instant.now()));

        ContinuousImprovementCycle cycle = new ContinuousImprovementCycle(
                "CYC-1", null, null, null, true, null, null, null
        );
        assertEquals("CYC-1", cycle.cycleId());
        assertEquals("", cycle.requestAnalysis());
        assertTrue(cycle.detectedProblems().isEmpty());
        assertEquals("", cycle.generatedSolution());
        assertTrue(cycle.accuracyVerified());
        assertNotNull(cycle.executedAt());
    }

    @Test
    @DisplayName("QualityStandardsReport should evaluate acceptability correctly")
    void testQualityStandardsReport() {
        QualityStandardsReport passing = new QualityStandardsReport(true, true, true, true, true, 0.95, null);
        assertTrue(passing.isAcceptable());
        assertEquals("Fully compliant with enterprise quality standards", passing.notes());

        QualityStandardsReport failingInaccuracy = new QualityStandardsReport(false, true, true, true, true, 0.95, "Inaccurate");
        assertFalse(failingInaccuracy.isAcceptable());

        QualityStandardsReport failingLowScore = new QualityStandardsReport(true, true, true, true, true, 0.80, "Low score");
        assertFalse(failingLowScore.isAcceptable());

        QualityStandardsReport failingSecurity = new QualityStandardsReport(true, true, true, true, false, 0.90, "Security issue");
        assertFalse(failingSecurity.isAcceptable());
    }
}
