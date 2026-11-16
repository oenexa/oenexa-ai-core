package com.oenexa.domain.model.risk;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Enterprise Risk & Fraud Intelligence Domain Records and Invariants.
 */
public final class RiskFraudDomain {

    private RiskFraudDomain() {
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum RiskCategory {
        DEPLOYMENT_BLAST_RADIUS,
        DATABASE_MIGRATION,
        API_BREAKING_CHANGE,
        AUTHENTICATION_ABUSE,
        FINANCIAL_VELOCITY,
        SYBIL_CLUSTERING,
        COMPLIANCE_DRIFT
    }

    public enum MitigationAction {
        ALLOW,
        REQUIRE_PEER_APPROVAL,
        CHALLENGE_MFA,
        THROTTLE_RATE_LIMIT,
        QUARANTINE_SUSPEND,
        REVERT_DEPLOYMENT
    }

    public record RiskFactor(
            String factorId,
            RiskCategory category,
            String description,
            double weight,
            boolean isTriggered
    ) {
        public RiskFactor {
            Objects.requireNonNull(factorId, "factorId must not be null");
            Objects.requireNonNull(category, "category must not be null");
            description = description == null ? "" : description;
            weight = Math.clamp(weight, 0.0, 1.0);
        }
    }

    public record DeploymentRiskEvaluation(
            String evaluationId,
            String serviceName,
            String targetEnvironment,
            double overallScore,
            RiskLevel riskLevel,
            List<RiskFactor> factors,
            List<String> recommendedMitigations,
            boolean deploymentBlocked
    ) {
        public DeploymentRiskEvaluation {
            Objects.requireNonNull(evaluationId, "evaluationId must not be null");
            Objects.requireNonNull(serviceName, "serviceName must not be null");
            targetEnvironment = targetEnvironment == null ? "production" : targetEnvironment;
            overallScore = Math.clamp(overallScore, 0.0, 1.0);
            riskLevel = riskLevel == null ? RiskLevel.LOW : riskLevel;
            factors = factors == null ? List.of() : List.copyOf(factors);
            recommendedMitigations = recommendedMitigations == null ? List.of() : List.copyOf(recommendedMitigations);
        }
    }

    public record FraudSignal(
            String signalId,
            String signalType,
            double anomalyConfidence,
            String telemetrySource,
            String details
    ) {
        public FraudSignal {
            Objects.requireNonNull(signalId, "signalId must not be null");
            Objects.requireNonNull(signalType, "signalType must not be null");
            anomalyConfidence = Math.clamp(anomalyConfidence, 0.0, 1.0);
            telemetrySource = telemetrySource == null ? "GATEWAY" : telemetrySource;
            details = details == null ? "" : details;
        }
    }

    public record FraudAssessmentDossier(
            String dossierId,
            String entityId,
            String entityType,
            double fraudScore,
            RiskLevel threatLevel,
            MitigationAction recommendedAction,
            List<FraudSignal> signals,
            Instant evaluatedAt
    ) {
        public FraudAssessmentDossier {
            Objects.requireNonNull(dossierId, "dossierId must not be null");
            Objects.requireNonNull(entityId, "entityId must not be null");
            entityType = entityType == null ? "ACCOUNT" : entityType;
            fraudScore = Math.clamp(fraudScore, 0.0, 1.0);
            threatLevel = threatLevel == null ? RiskLevel.LOW : threatLevel;
            recommendedAction = recommendedAction == null ? MitigationAction.ALLOW : recommendedAction;
            signals = signals == null ? List.of() : List.copyOf(signals);
            evaluatedAt = evaluatedAt == null ? Instant.now() : evaluatedAt;
        }
    }

    public record EvaluateDeploymentRiskRequest(
            String serviceName,
            String targetEnvironment,
            String diffSummary,
            boolean hasDatabaseMigration,
            List<String> modifiedEndpoints,
            Map<String, String> metadata
    ) {
        public EvaluateDeploymentRiskRequest {
            Objects.requireNonNull(serviceName, "serviceName must not be null");
            targetEnvironment = targetEnvironment == null ? "production" : targetEnvironment;
            diffSummary = diffSummary == null ? "" : diffSummary;
            modifiedEndpoints = modifiedEndpoints == null ? List.of() : List.copyOf(modifiedEndpoints);
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    public record EvaluateFraudRequest(
            String entityId,
            String entityType,
            double velocityScore,
            int failedAttempts,
            String ipSubnet,
            String userAgent,
            Map<String, String> telemetry
    ) {
        public EvaluateFraudRequest {
            Objects.requireNonNull(entityId, "entityId must not be null");
            entityType = entityType == null ? "ACCOUNT" : entityType;
            velocityScore = Math.clamp(velocityScore, 0.0, 1.0);
            ipSubnet = ipSubnet == null ? "" : ipSubnet;
            userAgent = userAgent == null ? "" : userAgent;
            telemetry = telemetry == null ? Map.of() : Map.copyOf(telemetry);
        }
    }
}
