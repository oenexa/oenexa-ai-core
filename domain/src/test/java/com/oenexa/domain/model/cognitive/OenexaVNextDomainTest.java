package com.oenexa.domain.model.cognitive;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaVNextDomain Zero-Mock Unit Tests")
class OenexaVNextDomainTest {

    @Test
    @DisplayName("ClassifiedIssue should enforce non-null invariants and defaults")
    void testClassifiedIssueInvariants() {
        assertThrows(NullPointerException.class, () -> new ClassifiedIssue(null, VNextDetectionType.BUG, "title", "desc", "HIGH", Instant.now(), Map.of()));
        assertThrows(NullPointerException.class, () -> new ClassifiedIssue("ISS-1", null, "title", "desc", "HIGH", Instant.now(), Map.of()));

        ClassifiedIssue issue = new ClassifiedIssue("ISS-1", VNextDetectionType.RUNTIME_FAILURE, null, null, null, null, null);
        assertEquals("ISS-1", issue.issueId());
        assertEquals(VNextDetectionType.RUNTIME_FAILURE, issue.type());
        assertEquals("Detected RUNTIME_FAILURE", issue.title());
        assertEquals("", issue.description());
        assertEquals("MEDIUM", issue.severity());
        assertNotNull(issue.detectedAt());
        assertTrue(issue.contextData().isEmpty());
    }

    @Test
    @DisplayName("RootCauseReport and RiskAssessment should enforce contracts")
    void testRootCauseAndRiskInvariants() {
        assertThrows(NullPointerException.class, () -> new RootCauseReport(null, "cause", "CWE-1", 0.9, List.of()));

        RootCauseReport report = new RootCauseReport("ISS-1", null, null, 0.9, null);
        assertEquals("Root cause under analysis", report.probableRootCause());
        assertEquals("CWE-General", report.cweClassification());
        assertTrue(report.evidenceChain().isEmpty());

        RiskAssessment assessment = new RiskAssessment("ISS-1", 0.8, null, true, null);
        assertEquals("HIGH", assessment.riskLevel());
        assertTrue(assessment.requiresImmediateMitigation());
        assertTrue(assessment.impactAreas().isEmpty());
    }

    @Test
    @DisplayName("ResolutionFix and AutonomousResolutionResult should handle safe defaults")
    void testResolutionRecords() {
        assertThrows(NullPointerException.class, () -> new ResolutionFix(1, null, "title", "diff", 0.9, 0.9, List.of()));

        ResolutionFix fix = new ResolutionFix(1, "FIX-1", null, null, 0.9, 0.9, null);
        assertEquals("Automated Fix", fix.title());
        assertEquals("", fix.codeDiff());
        assertTrue(fix.validationChecks().isEmpty());

        AutonomousResolutionResult result = new AutonomousResolutionResult("RES-1", "ISS-1", fix, true, true, null, null);
        assertEquals("Resolution completed", result.outcomeSummary());
        assertNotNull(result.resolvedAt());
        assertTrue(result.safetyVerified());
        assertTrue(result.validationPassed());
    }

    @Test
    @DisplayName("VNextPattern should update weights dynamically on success and failure")
    void testPatternEvolution() {
        VNextPattern pattern = new VNextPattern("PAT-1", VNextPatternCategory.REASONING, null, null, null, 2, 2, 0.5);
        assertEquals("Pattern-PAT-1", pattern.name());

        VNextPattern success = pattern.updateOutcome(true);
        assertEquals(3, success.successTally());
        assertEquals(2, success.failureTally());
        assertEquals(0.6, success.dynamicWeight(), 0.001);

        VNextPattern failure = pattern.updateOutcome(false);
        assertEquals(2, failure.successTally());
        assertEquals(3, failure.failureTally());
        assertEquals(0.4, failure.dynamicWeight(), 0.001);
    }

    @Test
    @DisplayName("DeepReasoningPlan and Hypothesis should enforce invariants")
    void testDeepReasoningRecords() {
        Hypothesis h = new Hypothesis("H-1", null, 0.9, null, null);
        assertEquals("", h.thesis());
        assertTrue(h.supportingArguments().isEmpty());
        assertTrue(h.counterArguments().isEmpty());

        DeepReasoningPlan plan = new DeepReasoningPlan("Statement", null, null, null, 0.95);
        assertTrue(plan.subProblems().isEmpty());
        assertTrue(plan.evaluatedHypotheses().isEmpty());
        assertEquals("", plan.synthesizedAnswer());
    }

    @Test
    @DisplayName("CapabilityGenerationArtifact should hold generated code structures")
    void testCapabilityRecords() {
        CapabilityGenerationArtifact artifact = new CapabilityGenerationArtifact("BlockchainAnalyzer", "interface code", "impl code", "test code", true, true);
        assertEquals("BlockchainAnalyzer", artifact.capabilityName());
        assertTrue(artifact.designVerified());
        assertTrue(artifact.registered());
    }

    @Test
    @DisplayName("Memory and Architecture evolution records should handle defaults")
    void testMemoryAndArchitectureRecords() {
        MemoryConsolidationReport report = new MemoryConsolidationReport(10, 2, 0.8, 1024L, null);
        assertEquals(10, report.entriesConsolidated());
        assertEquals(2, report.duplicatesEliminated());
        assertNotNull(report.consolidatedAt());

        ArchitectureEvolutionPlan plan = new ArchitectureEvolutionPlan("DOMAIN", null, null, true, null);
        assertEquals("Legacy", plan.currentPattern());
        assertEquals("Hexagonal DDD", plan.proposedEvolution());
        assertTrue(plan.migrationSteps().isEmpty());
    }

    @Test
    @DisplayName("QualityGateV2Report should evaluate aggregate checks")
    void testQualityGateRecords() {
        QualityGateCheck check = new QualityGateCheck("Security", true, 1.0, null);
        assertEquals("OK", check.diagnosticMessage());

        QualityGateV2Report report = new QualityGateV2Report(true, 1.0, List.of(check), true);
        assertTrue(report.allPassed());
        assertTrue(report.acceptedForDelivery());
    }
}


