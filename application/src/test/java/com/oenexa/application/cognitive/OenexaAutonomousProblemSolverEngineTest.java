package com.oenexa.application.cognitive;

import com.oenexa.domain.OenexaAutonomousProblemSolverDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAutonomousProblemSolverEngine Zero-Mock Tests")
class OenexaAutonomousProblemSolverEngineTest {

    private OenexaAutonomousProblemSolverEngine engine;

    @BeforeEach
    void setUp() {
        engine = new OenexaAutonomousProblemSolverEngine();
    }

    @Test
    @DisplayName("Pillar 1: Issue Detection classifies errors and root causes accurately")
    void testIssueDetection() {
        assertThrows(NullPointerException.class, () -> engine.detectIssue(null, "some error"));

        DetectedIssue npeIssue = engine.detectIssue("User service call", "NullPointerException at line 42");
        assertEquals("NULL_POINTER_DEREFERENCE", npeIssue.issueType());
        assertEquals(IssueSeverity.HIGH, npeIssue.severity());
        assertTrue(npeIssue.rootCause().contains("null verification"));

        DetectedIssue oomIssue = engine.detectIssue("Large tensor compute", "OutOfMemoryError: Java heap space");
        assertEquals("RESOURCE_EXHAUSTION", oomIssue.issueType());
        assertEquals(IssueSeverity.CRITICAL, oomIssue.severity());

        DetectedIssue indexIssue = engine.detectIssue("Array access", "IndexOutOfBoundsException: Index 5 out of bounds for length 3");
        assertEquals("BOUNDARY_VIOLATION", indexIssue.issueType());
        assertEquals(IssueSeverity.MEDIUM, indexIssue.severity());

        DetectedIssue genericIssue = engine.detectIssue("Network connection", "SocketTimeoutException");
        assertEquals("GENERIC_RUNTIME_EXCEPTION", genericIssue.issueType());
        assertEquals(IssueSeverity.MEDIUM, genericIssue.severity());

        DetectedIssue cleanIssue = engine.detectIssue("Normal flow", "");
        assertEquals("INFORMATIONAL", cleanIssue.issueType());
        assertEquals(IssueSeverity.LOW, cleanIssue.severity());
    }

    @Test
    @DisplayName("Pillar 2: Automatic Resolution generates targeted corrective actions")
    void testAutomaticResolution() {
        assertThrows(NullPointerException.class, () -> engine.resolveAutomatically(null));

        DetectedIssue npe = engine.detectIssue("Service", "NullPointerException");
        CorrectiveAction actionNpe = engine.resolveAutomatically(npe);
        assertTrue(actionNpe.title().contains("Null Checks"));
        assertEquals(RiskLevel.SAFE, actionNpe.riskLevel());
        assertTrue(actionNpe.validated());
        assertFalse(actionNpe.executionSteps().isEmpty());

        DetectedIssue oom = engine.detectIssue("Service", "OutOfMemory");
        CorrectiveAction actionOom = engine.resolveAutomatically(oom);
        assertEquals(RiskLevel.HIGH, actionOom.riskLevel());
        assertTrue(actionOom.title().contains("Virtual Thread Unmount"));

        DetectedIssue bounds = engine.detectIssue("Service", "IndexOutOfBounds");
        CorrectiveAction actionBounds = engine.resolveAutomatically(bounds);
        assertTrue(actionBounds.title().contains("Collection Size"));

        DetectedIssue info = engine.detectIssue("Service", "");
        CorrectiveAction actionInfo = engine.resolveAutomatically(info);
        assertTrue(actionInfo.title().contains("Diagnostics"));
    }

    @Test
    @DisplayName("Pillar 3: New Question Processing returns ranked solutions")
    void testQuestionProcessing() {
        assertThrows(NullPointerException.class, () -> engine.processQuestion(null, "context", ExpertiseLevel.EXPERT));

        ProcessedQuestion pq = engine.processQuestion("How do I architect high-throughput services?", "Spring ecosystem", ExpertiseLevel.EXPERT);
        assertEquals("How do I architect high-throughput services?", pq.rawQuestion());
        assertEquals(2, pq.rankedSolutions().size());
        assertEquals(1, pq.rankedSolutions().get(0).rank());
        assertTrue(pq.rankedSolutions().get(0).effectivenessScore() > pq.rankedSolutions().get(1).effectivenessScore());
    }

    @Test
    @DisplayName("Pillar 4: Self-Learning Framework stores and updates pattern weights")
    void testSelfLearningFramework() {
        int initialCount = engine.getPatternCount();
        assertTrue(initialCount >= 2);

        ReasoningPattern customPattern = new ReasoningPattern(
                "PAT-CUSTOM", "Custom Pattern", "Criteria", List.of("Step 1", "Step 2"), 5, 5, 0.5
        );
        engine.registerPattern(customPattern);
        assertEquals(initialCount + 1, engine.getPatternCount());

        ReasoningPattern updated = engine.recordPatternOutcome("PAT-CUSTOM", true);
        assertEquals(6, updated.successCount());
        assertEquals(5, updated.failureCount());
        assertTrue(updated.confidenceWeight() > 0.5);

        assertThrows(IllegalArgumentException.class, () -> engine.recordPatternOutcome("UNKNOWN_PAT", true));
        assertTrue(engine.findPattern("PAT-CUSTOM").isPresent());
        assertTrue(engine.findPattern("NON_EXISTENT").isEmpty());
    }

    @Test
    @DisplayName("Pillar 5: Knowledge Expansion synthesizes conceptual models")
    void testKnowledgeExpansion() {
        assertThrows(NullPointerException.class, () -> engine.expandKnowledge(null, "hypothesis"));

        ConceptualModel model = engine.expandKnowledge("Distributed Consensus", "Raft consensus under network partitions");
        assertEquals("Distributed Consensus", model.domainArea());
        assertEquals("Raft consensus under network partitions", model.hypothesis());
        assertFalse(model.coreInvariants().isEmpty());
        assertFalse(model.alternativePaths().isEmpty());
    }

    @Test
    @DisplayName("Pillar 6: Self-Optimization evaluates weaknesses and strategies")
    void testSelfOptimization() {
        assertThrows(NullPointerException.class, () -> engine.evaluateSelfOptimization(null, 0.9));

        SelfOptimizationMetric passing = engine.evaluateSelfOptimization("Accuracy", 0.95);
        assertEquals("None", passing.weaknessIdentified());

        SelfOptimizationMetric failing = engine.evaluateSelfOptimization("Completeness", 0.70);
        assertTrue(failing.weaknessIdentified().contains("below industrial baseline"));
        assertTrue(failing.upgradeStrategy().contains("Enhance internal reasoning"));
    }

    @Test
    @DisplayName("Pillar 7: Failure Recovery decomposes failed objectives")
    void testFailureRecovery() {
        assertThrows(NullPointerException.class, () -> engine.recoverFromFailure(null, List.of()));

        FailureRecoveryPlan plan = engine.recoverFromFailure("Deploy cluster", List.of("Missing kubeconfig", "Missing secrets"));
        assertEquals("Deploy cluster", plan.failedObjective());
        assertEquals(2, plan.missingInformation().size());
        assertEquals(3, plan.alternativeMethods().size());
        assertEquals(3, plan.decomposedParts().size());
        assertNotNull(plan.fallbackSolution());
        assertNotNull(plan.futureImprovementStrategy());
    }

    @Test
    @DisplayName("Pillar 8: Adaptive Intelligence formats responses by expertise level")
    void testAdaptiveIntelligence() {
        assertThrows(NullPointerException.class, () -> engine.adaptCommunication(null, ExpertiseLevel.EXPERT));

        AdaptiveCommunication beg = engine.adaptCommunication("Core content", ExpertiseLevel.BEGINNER);
        assertEquals(ExpertiseLevel.BEGINNER, beg.targetExpertise());
        assertTrue(beg.adaptedPayload().contains("Simple Overview"));

        AdaptiveCommunication inter = engine.adaptCommunication("Core content", ExpertiseLevel.INTERMEDIATE);
        assertEquals(ExpertiseLevel.INTERMEDIATE, inter.targetExpertise());
        assertTrue(inter.adaptedPayload().contains("Practical Implementation Guide"));

        AdaptiveCommunication exp = engine.adaptCommunication("Core content", ExpertiseLevel.EXPERT);
        assertEquals(ExpertiseLevel.EXPERT, exp.targetExpertise());
        assertTrue(exp.adaptedPayload().contains("Enterprise Autonomous Specification"));
    }

    @Test
    @DisplayName("Pillar 9: Continuous Improvement Loop runs all 7 stages")
    void testContinuousImprovementLoop() {
        assertThrows(NullPointerException.class, () -> engine.executeImprovementLoop(null));

        ContinuousImprovementCycle cycle = engine.executeImprovementLoop("Optimize SQL joins");
        assertNotNull(cycle.cycleId());
        assertTrue(cycle.requestAnalysis().contains("Optimize SQL joins"));
        assertFalse(cycle.detectedProblems().isEmpty());
        assertTrue(cycle.accuracyVerified());
        assertFalse(cycle.outcomeLearned().isEmpty());
        assertFalse(cycle.upgradedFramework().isEmpty());
        assertNotNull(cycle.executedAt());
    }

    @Test
    @DisplayName("Pillar 10: Quality Standards validates compliance thresholds")
    void testQualityStandards() {
        assertThrows(NullPointerException.class, () -> engine.assessQuality(null, false));

        QualityStandardsReport good = engine.assessQuality("### Complete Solution\nThis is a transparent, consistent and accurate solution for the problem.", false);
        assertTrue(good.isAcceptable());
        assertEquals(1.0, good.overallQualityScore(), 0.001);

        QualityStandardsReport withSecurity = engine.assessQuality("### Complete Solution\nThis solution has a vulnerability.", true);
        assertFalse(withSecurity.isAcceptable());
        assertFalse(withSecurity.securityPrivacyCompliant());
    }
}
