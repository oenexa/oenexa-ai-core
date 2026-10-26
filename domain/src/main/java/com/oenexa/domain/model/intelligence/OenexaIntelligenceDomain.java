package com.oenexa.domain.model.intelligence;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Sovereign Domain Model for OENEXA Intelligence System (OIS).
 * Enforces the original 7-step cognitive cycle:
 * Understand -> Analyze -> Reason -> Validate -> Respond -> Learn -> Improve
 */
public final class OenexaIntelligenceDomain {

    private OenexaIntelligenceDomain() {}

    public record OenexaPrompt(
            UUID promptId,
            String userId,
            String workspaceId,
            String query,
            List<String> contextFiles,
            String sovereignEnginePreference,
            Instant timestamp
    ) {
        public OenexaPrompt {
            Objects.requireNonNull(promptId, "promptId must not be null");
            Objects.requireNonNull(userId, "userId must not be null");
            Objects.requireNonNull(workspaceId, "workspaceId must not be null");
            Objects.requireNonNull(query, "query must not be null");
            contextFiles = contextFiles == null ? List.of() : List.copyOf(contextFiles);
            sovereignEnginePreference = sovereignEnginePreference == null ? "OENEXA-CNE-32B" : sovereignEnginePreference;
            timestamp = timestamp == null ? Instant.now() : timestamp;
        }
    }

    public record OenexaContext(
            String workspaceId,
            String workspaceRoot,
            int indexedSymbolsCount,
            List<String> relevantFiles,
            String architectureClassification
    ) {
        public OenexaContext {
            Objects.requireNonNull(workspaceId, "workspaceId must not be null");
            Objects.requireNonNull(workspaceRoot, "workspaceRoot must not be null");
            relevantFiles = relevantFiles == null ? List.of() : List.copyOf(relevantFiles);
            architectureClassification = architectureClassification == null ? "OenexaCleanModularMonolith" : architectureClassification;
        }
    }

    public record OenexaReasoningTrace(
            List<String> analyticalSteps,
            String derivedHypothesis,
            boolean invariantValidated,
            long executionLatencyMs
    ) {
        public OenexaReasoningTrace {
            analyticalSteps = analyticalSteps == null ? List.of() : List.copyOf(analyticalSteps);
            derivedHypothesis = derivedHypothesis == null ? "Sovereign Invariants Confirmed" : derivedHypothesis;
        }
    }

    public record OenexaValidationReport(
            boolean compliant,
            int invariantScore,
            List<String> verifiedRules
    ) {
        public OenexaValidationReport {
            verifiedRules = verifiedRules == null ? List.of() : List.copyOf(verifiedRules);
        }
    }

    public record OenexaArtifact(
            String id,
            String title,
            String targetPath,
            String syntaxLanguage,
            String operation, // CREATE, MODIFY, DELETE
            String sourceCode,
            String unifiedDiff
    ) {
        public OenexaArtifact {
            Objects.requireNonNull(id, "id must not be null");
            Objects.requireNonNull(title, "title must not be null");
            Objects.requireNonNull(targetPath, "targetPath must not be null");
            Objects.requireNonNull(syntaxLanguage, "syntaxLanguage must not be null");
            operation = operation == null ? "CREATE" : operation;
            sourceCode = sourceCode == null ? "" : sourceCode;
            unifiedDiff = unifiedDiff == null ? "" : unifiedDiff;
        }
    }

    public record OenexaIntelligenceResponse(
            UUID responseId,
            UUID promptId,
            String directAnswer,
            OenexaReasoningTrace reasoningTrace,
            OenexaValidationReport validationReport,
            List<String> actionableRecommendations,
            String structuredActionPlan,
            List<OenexaArtifact> artifacts,
            String activeNeuralEngine,
            Instant generatedAt
    ) {
        public OenexaIntelligenceResponse {
            Objects.requireNonNull(responseId, "responseId must not be null");
            Objects.requireNonNull(promptId, "promptId must not be null");
            Objects.requireNonNull(directAnswer, "directAnswer must not be null");
            Objects.requireNonNull(reasoningTrace, "reasoningTrace must not be null");
            validationReport = validationReport == null ? new OenexaValidationReport(true, 100, List.of()) : validationReport;
            actionableRecommendations = actionableRecommendations == null ? List.of() : List.copyOf(actionableRecommendations);
            structuredActionPlan = structuredActionPlan == null ? "" : structuredActionPlan;
            artifacts = artifacts == null ? List.of() : List.copyOf(artifacts);
            activeNeuralEngine = activeNeuralEngine == null ? "OENEXA-CNE-32B" : activeNeuralEngine;
            generatedAt = generatedAt == null ? Instant.now() : generatedAt;
        }
    }

    public record OenexaTaskExecution(
            UUID executionId,
            String engineIdentifier,
            String targetCommand,
            String consoleOutput,
            int statusExitCode,
            boolean isSuccessful,
            long durationMs
    ) {
        public OenexaTaskExecution {
            Objects.requireNonNull(executionId, "executionId must not be null");
            Objects.requireNonNull(engineIdentifier, "engineIdentifier must not be null");
            consoleOutput = consoleOutput == null ? "" : consoleOutput;
        }
    }

    public record OenexaLearnedInsight(
            String insightId,
            String feedbackVector,
            boolean knowledgeConsolidated,
            Instant recordedAt
    ) {
        public OenexaLearnedInsight {
            Objects.requireNonNull(insightId, "insightId must not be null");
            recordedAt = recordedAt == null ? Instant.now() : recordedAt;
        }
    }

    public record OenexaImprovementMetric(
            String metricName,
            double baselineMeasure,
            double currentMeasure,
            Instant evaluatedAt
    ) {
        public OenexaImprovementMetric {
            Objects.requireNonNull(metricName, "metricName must not be null");
            evaluatedAt = evaluatedAt == null ? Instant.now() : evaluatedAt;
        }
    }
}
