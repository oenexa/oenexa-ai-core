package com.oenexa.api.dto.risk;

import com.oenexa.domain.model.risk.RiskFraudDomain.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Enterprise DTOs for Risk & Fraud Intelligence REST API.
 */
public final class RiskFraudDtos {

    private RiskFraudDtos() {
    }

    public record RiskFactorDto(
            String factorId,
            RiskCategory category,
            String description,
            double weight,
            boolean isTriggered
    ) {
        public static RiskFactorDto fromDomain(RiskFactor domain) {
            return new RiskFactorDto(
                    domain.factorId(),
                    domain.category(),
                    domain.description(),
                    domain.weight(),
                    domain.isTriggered()
            );
        }
    }

    public record DeploymentRiskEvaluationDto(
            String evaluationId,
            String serviceName,
            String targetEnvironment,
            double overallScore,
            RiskLevel riskLevel,
            List<RiskFactorDto> factors,
            List<String> recommendedMitigations,
            boolean deploymentBlocked
    ) {
        public static DeploymentRiskEvaluationDto fromDomain(DeploymentRiskEvaluation domain) {
            return new DeploymentRiskEvaluationDto(
                    domain.evaluationId(),
                    domain.serviceName(),
                    domain.targetEnvironment(),
                    domain.overallScore(),
                    domain.riskLevel(),
                    domain.factors().stream().map(RiskFactorDto::fromDomain).toList(),
                    domain.recommendedMitigations(),
                    domain.deploymentBlocked()
            );
        }
    }

    public record EvaluateDeploymentRiskRequestDto(
            String serviceName,
            String targetEnvironment,
            String diffSummary,
            boolean hasDatabaseMigration,
            List<String> modifiedEndpoints,
            Map<String, String> metadata
    ) {
        public EvaluateDeploymentRiskRequest toDomain() {
            return new EvaluateDeploymentRiskRequest(
                    serviceName != null ? serviceName : "unknown-service",
                    targetEnvironment != null ? targetEnvironment : "production",
                    diffSummary != null ? diffSummary : "",
                    hasDatabaseMigration,
                    modifiedEndpoints != null ? modifiedEndpoints : List.of(),
                    metadata != null ? metadata : Map.of()
            );
        }
    }

    public record FraudSignalDto(
            String signalId,
            String signalType,
            double anomalyConfidence,
            String telemetrySource,
            String details
    ) {
        public static FraudSignalDto fromDomain(FraudSignal domain) {
            return new FraudSignalDto(
                    domain.signalId(),
                    domain.signalType(),
                    domain.anomalyConfidence(),
                    domain.telemetrySource(),
                    domain.details()
            );
        }
    }

    public record FraudAssessmentDossierDto(
            String dossierId,
            String entityId,
            String entityType,
            double fraudScore,
            RiskLevel threatLevel,
            MitigationAction recommendedAction,
            List<FraudSignalDto> signals,
            Instant evaluatedAt
    ) {
        public static FraudAssessmentDossierDto fromDomain(FraudAssessmentDossier domain) {
            return new FraudAssessmentDossierDto(
                    domain.dossierId(),
                    domain.entityId(),
                    domain.entityType(),
                    domain.fraudScore(),
                    domain.threatLevel(),
                    domain.recommendedAction(),
                    domain.signals().stream().map(FraudSignalDto::fromDomain).toList(),
                    domain.evaluatedAt()
            );
        }
    }

    public record EvaluateFraudRequestDto(
            String entityId,
            String entityType,
            double velocityScore,
            int failedAttempts,
            String ipSubnet,
            String userAgent,
            Map<String, String> telemetry
    ) {
        public EvaluateFraudRequest toDomain() {
            return new EvaluateFraudRequest(
                    entityId != null ? entityId : "unknown-entity",
                    entityType != null ? entityType : "ACCOUNT",
                    velocityScore,
                    failedAttempts,
                    ipSubnet != null ? ipSubnet : "",
                    userAgent != null ? userAgent : "",
                    telemetry != null ? telemetry : Map.of()
            );
        }
    }
}
