package com.oenexa.domain;

import java.time.Instant;
import java.util.*;

/**
 * Enterprise Native Autonomous Problem Handling and Self-Learning Framework Domain.
 * Codifies the 10-pillar cognitive operating protocol:
 * 1. Issue Detection
 * 2. Automatic Resolution
 * 3. New Question Processing
 * 4. Self-Learning Framework
 * 5. Knowledge Expansion
 * 6. Self-Optimization
 * 7. Failure Recovery
 * 8. Adaptive Intelligence
 * 9. Continuous Improvement Loop
 * 10. Quality Standards
 */
public final class OenexaAutonomousProblemSolverDomain {

    private OenexaAutonomousProblemSolverDomain() {}

    // =========================================================================
    // 1. Issue Detection & Root Cause
    // =========================================================================

    public enum IssueSeverity { LOW, MEDIUM, HIGH, CRITICAL }

    public record DetectedIssue(
            String issueId,
            String issueType,
            IssueSeverity severity,
            String description,
            String rootCause,
            Instant detectedAt,
            Map<String, String> telemetry
    ) {
        public DetectedIssue {
            Objects.requireNonNull(issueId, "issueId must not be null");
            Objects.requireNonNull(issueType, "issueType must not be null");
            severity = (severity != null) ? severity : IssueSeverity.MEDIUM;
            description = (description != null) ? description : "";
            rootCause = (rootCause != null) ? rootCause : "Under investigation";
            detectedAt = (detectedAt != null) ? detectedAt : Instant.now();
            telemetry = (telemetry != null) ? Map.copyOf(telemetry) : Map.of();
        }
    }

    // =========================================================================
    // 2. Automatic Resolution & Corrective Action
    // =========================================================================

    public enum RiskLevel { SAFE, MODERATE, HIGH }

    public record CorrectiveAction(
            String actionId,
            String targetIssueId,
            String title,
            String description,
            RiskLevel riskLevel,
            boolean validated,
            List<String> executionSteps
    ) {
        public CorrectiveAction {
            Objects.requireNonNull(actionId, "actionId must not be null");
            Objects.requireNonNull(targetIssueId, "targetIssueId must not be null");
            title = (title != null) ? title : "Corrective Action";
            description = (description != null) ? description : "";
            riskLevel = (riskLevel != null) ? riskLevel : RiskLevel.SAFE;
            executionSteps = (executionSteps != null) ? List.copyOf(executionSteps) : List.of();
        }
    }

    // =========================================================================
    // 3. New Question Processing & Multi-Solution Ranking
    // =========================================================================

    public record RankedSolution(
            int rank,
            String title,
            String rationale,
            double effectivenessScore,
            List<String> tradeOffs,
            List<String> implementationSteps
    ) {
        public RankedSolution {
            if (rank < 1) throw new IllegalArgumentException("rank must be >= 1");
            Objects.requireNonNull(title, "title must not be null");
            rationale = (rationale != null) ? rationale : "";
            tradeOffs = (tradeOffs != null) ? List.copyOf(tradeOffs) : List.of();
            implementationSteps = (implementationSteps != null) ? List.copyOf(implementationSteps) : List.of();
        }
    }

    public record ProcessedQuestion(
            String rawQuestion,
            String inferredIntent,
            String technicalContext,
            String primaryGoal,
            String expectedOutcome,
            List<RankedSolution> rankedSolutions
    ) {
        public ProcessedQuestion {
            Objects.requireNonNull(rawQuestion, "rawQuestion must not be null");
            inferredIntent = (inferredIntent != null) ? inferredIntent : "General Inquiry";
            technicalContext = (technicalContext != null) ? technicalContext : "Standard Context";
            primaryGoal = (primaryGoal != null) ? primaryGoal : rawQuestion;
            expectedOutcome = (expectedOutcome != null) ? expectedOutcome : "Actionable Solution";
            rankedSolutions = (rankedSolutions != null) ? List.copyOf(rankedSolutions) : List.of();
        }
    }

    // =========================================================================
    // 4. Self-Learning Framework & Reusable Patterns
    // =========================================================================

    public record ReasoningPattern(
            String patternId,
            String patternName,
            String triggerCriteria,
            List<String> reasoningSteps,
            int successCount,
            int failureCount,
            double confidenceWeight
    ) {
        public ReasoningPattern {
            Objects.requireNonNull(patternId, "patternId must not be null");
            Objects.requireNonNull(patternName, "patternName must not be null");
            triggerCriteria = (triggerCriteria != null) ? triggerCriteria : "";
            reasoningSteps = (reasoningSteps != null) ? List.copyOf(reasoningSteps) : List.of();
        }

        public ReasoningPattern recordOutcome(boolean success) {
            int newSuccess = success ? successCount + 1 : successCount;
            int newFailure = success ? failureCount : failureCount + 1;
            double newWeight = (double) newSuccess / (newSuccess + newFailure);
            return new ReasoningPattern(patternId, patternName, triggerCriteria, reasoningSteps, newSuccess, newFailure, newWeight);
        }
    }

    // =========================================================================
    // 5. Knowledge Expansion & Conceptual Models
    // =========================================================================

    public record ConceptualModel(
            String modelId,
            String domainArea,
            String hypothesis,
            List<String> coreInvariants,
            List<String> alternativePaths
    ) {
        public ConceptualModel {
            Objects.requireNonNull(modelId, "modelId must not be null");
            Objects.requireNonNull(domainArea, "domainArea must not be null");
            hypothesis = (hypothesis != null) ? hypothesis : "";
            coreInvariants = (coreInvariants != null) ? List.copyOf(coreInvariants) : List.of();
            alternativePaths = (alternativePaths != null) ? List.copyOf(alternativePaths) : List.of();
        }
    }

    // =========================================================================
    // 6. Self-Optimization & Weakness Remediation
    // =========================================================================

    public record SelfOptimizationMetric(
            String metricName,
            double baselineScore,
            double evaluatedScore,
            String weaknessIdentified,
            String upgradeStrategy
    ) {
        public SelfOptimizationMetric {
            Objects.requireNonNull(metricName, "metricName must not be null");
            weaknessIdentified = (weaknessIdentified != null) ? weaknessIdentified : "None";
            upgradeStrategy = (upgradeStrategy != null) ? upgradeStrategy : "Maintain current strategy";
        }
    }

    // =========================================================================
    // 7. Failure Recovery & Problem Decomposition
    // =========================================================================

    public record FailureRecoveryPlan(
            String failedObjective,
            List<String> missingInformation,
            List<String> alternativeMethods,
            List<String> decomposedParts,
            String fallbackSolution,
            String futureImprovementStrategy
    ) {
        public FailureRecoveryPlan {
            Objects.requireNonNull(failedObjective, "failedObjective must not be null");
            missingInformation = (missingInformation != null) ? List.copyOf(missingInformation) : List.of();
            alternativeMethods = (alternativeMethods != null) ? List.copyOf(alternativeMethods) : List.of();
            decomposedParts = (decomposedParts != null) ? List.copyOf(decomposedParts) : List.of();
            fallbackSolution = (fallbackSolution != null) ? fallbackSolution : "Fallback solution formulated";
            futureImprovementStrategy = (futureImprovementStrategy != null) ? futureImprovementStrategy : "Strategy updated";
        }
    }

    // =========================================================================
    // 8. Adaptive Intelligence & Communication Level
    // =========================================================================

    public enum ExpertiseLevel { BEGINNER, INTERMEDIATE, EXPERT }

    public record AdaptiveCommunication(
            ExpertiseLevel targetExpertise,
            String tone,
            String formattingStrategy,
            String adaptedPayload
    ) {
        public AdaptiveCommunication {
            targetExpertise = (targetExpertise != null) ? targetExpertise : ExpertiseLevel.EXPERT;
            tone = (tone != null) ? tone : "Authoritative, Structured, Technical";
            formattingStrategy = (formattingStrategy != null) ? formattingStrategy : "Markdown tables, code blocks, bullet points";
            adaptedPayload = (adaptedPayload != null) ? adaptedPayload : "";
        }
    }

    // =========================================================================
    // 9. Continuous Improvement Loop (7 Stages)
    // =========================================================================

    public record ContinuousImprovementCycle(
            String cycleId,
            String requestAnalysis,
            List<String> detectedProblems,
            String generatedSolution,
            boolean accuracyVerified,
            String outcomeLearned,
            String upgradedFramework,
            Instant executedAt
    ) {
        public ContinuousImprovementCycle {
            Objects.requireNonNull(cycleId, "cycleId must not be null");
            requestAnalysis = (requestAnalysis != null) ? requestAnalysis : "";
            detectedProblems = (detectedProblems != null) ? List.copyOf(detectedProblems) : List.of();
            generatedSolution = (generatedSolution != null) ? generatedSolution : "";
            outcomeLearned = (outcomeLearned != null) ? outcomeLearned : "";
            upgradedFramework = (upgradedFramework != null) ? upgradedFramework : "";
            executedAt = (executedAt != null) ? executedAt : Instant.now();
        }
    }

    // =========================================================================
    // 10. Quality Standards Compliance
    // =========================================================================

    public record QualityStandardsReport(
            boolean accuracyPassed,
            boolean logicalConsistencyPassed,
            boolean reasoningTransparent,
            boolean completeSolution,
            boolean securityPrivacyCompliant,
            double overallQualityScore,
            String notes
    ) {
        public QualityStandardsReport {
            notes = (notes != null) ? notes : "Fully compliant with enterprise quality standards";
        }

        public boolean isAcceptable() {
            return accuracyPassed && logicalConsistencyPassed && securityPrivacyCompliant && overallQualityScore >= 0.85;
        }
    }
}
