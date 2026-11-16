package com.oenexa.application.risk;

import com.oenexa.domain.model.risk.RiskFraudDomain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enterprise Risk & Fraud Intelligence Service.
 * Evaluates repository and deployment risks, database migration safety,
 * velocity anomalies, and Sybil/credential fraud patterns.
 */
@Service
public class RiskFraudIntelligenceService {

    private final Map<String, DeploymentRiskEvaluation> deploymentRiskRegistry = new ConcurrentHashMap<>();
    private final Map<String, FraudAssessmentDossier> fraudDossierRegistry = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1000);

    public DeploymentRiskEvaluation evaluateDeploymentRisk(EvaluateDeploymentRiskRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String evaluationId = "RISK-" + counter.incrementAndGet();
        List<RiskFactor> factors = new ArrayList<>();
        double totalWeightedScore = 0.0;

        String diff = request.diffSummary().toUpperCase();

        if (request.hasDatabaseMigration()) {
            boolean hasDestructiveDdl = diff.contains("DROP TABLE") || diff.contains("TRUNCATE") || diff.contains("DROP COLUMN");
            double weight = hasDestructiveDdl ? 0.95 : 0.40;
            factors.add(new RiskFactor(
                    "RF-DB-MIG",
                    RiskCategory.DATABASE_MIGRATION,
                    hasDestructiveDdl ? "Destructive DDL detected in migration (DROP/TRUNCATE)" : "Database migration present",
                    weight,
                    true
            ));
            totalWeightedScore += weight;
        }

        if (diff.contains("DOCKER.SOCK") || diff.contains("PRIVILEGED: TRUE") || diff.contains("ROOT")) {
            factors.add(new RiskFactor(
                    "RF-SEC-CONTAINER",
                    RiskCategory.DEPLOYMENT_BLAST_RADIUS,
                    "Container deployment escalates privileges or mounts docker socket",
                    0.85,
                    true
            ));
            totalWeightedScore += 0.85;
        }

        if (!request.modifiedEndpoints().isEmpty()) {
            double endpointWeight = Math.min(request.modifiedEndpoints().size() * 0.15, 0.60);
            factors.add(new RiskFactor(
                    "RF-API-BREAK",
                    RiskCategory.API_BREAKING_CHANGE,
                    "Modified " + request.modifiedEndpoints().size() + " public API endpoints",
                    endpointWeight,
                    true
            ));
            totalWeightedScore += endpointWeight;
        }

        double maxWeight = factors.stream().mapToDouble(RiskFactor::weight).max().orElse(0.05);
        double avgWeight = factors.isEmpty() ? 0.05 : totalWeightedScore / factors.size();
        double overallScore = Math.clamp(maxWeight * 0.7 + avgWeight * 0.3, 0.0, 1.0);
        RiskLevel level = resolveRiskLevel(overallScore);

        boolean hasDestructiveDdl = diff.contains("DROP TABLE") || diff.contains("TRUNCATE") || diff.contains("DROP COLUMN");
        boolean isProd = "production".equalsIgnoreCase(request.targetEnvironment());
        boolean blocked = isProd && (level == RiskLevel.CRITICAL || (request.hasDatabaseMigration() && hasDestructiveDdl) || overallScore >= 0.75);

        List<String> mitigations = resolveDeploymentMitigations(level, blocked, factors);

        DeploymentRiskEvaluation evaluation = new DeploymentRiskEvaluation(
                evaluationId,
                request.serviceName(),
                request.targetEnvironment(),
                overallScore,
                level,
                factors,
                mitigations,
                blocked
        );

        deploymentRiskRegistry.put(evaluationId, evaluation);
        return evaluation;
    }

    public FraudAssessmentDossier assessFraud(EvaluateFraudRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String dossierId = "FRAUD-" + counter.incrementAndGet();
        List<FraudSignal> signals = new ArrayList<>();
        double scoreAccumulator = 0.0;

        if (request.velocityScore() >= 0.70) {
            signals.add(new FraudSignal(
                    "SIG-VELOCITY",
                    "HIGH_TRANSACTION_VELOCITY",
                    request.velocityScore(),
                    "PAYMENT_GATEWAY",
                    "Abnormal velocity spike observed for entity"
            ));
            scoreAccumulator += request.velocityScore();
        }

        if (request.failedAttempts() >= 5) {
            double attemptScore = Math.min(request.failedAttempts() * 0.10, 0.90);
            signals.add(new FraudSignal(
                    "SIG-BRUTE-FORCE",
                    "CREDENTIAL_STUFFING_OR_AUTH_FAILURE",
                    attemptScore,
                    "AUTH_SERVICE",
                    request.failedAttempts() + " consecutive failed attempts"
            ));
            scoreAccumulator += attemptScore;
        }

        String ua = request.userAgent().toLowerCase();
        if (ua.contains("headless") || ua.contains("curl") || ua.contains("python-requests") || ua.isBlank()) {
            signals.add(new FraudSignal(
                    "SIG-AUTOMATION",
                    "HEADLESS_AUTOMATION_CLIENT",
                    0.75,
                    "INGRESS_FILTER",
                    "Automated bot user-agent signature detected: " + request.userAgent()
            ));
            scoreAccumulator += 0.75;
        }

        double fraudScore = signals.isEmpty() ? 0.05 : Math.clamp(scoreAccumulator / Math.max(1, signals.size()), 0.0, 1.0);
        RiskLevel threatLevel = resolveRiskLevel(fraudScore);
        MitigationAction action = resolveFraudMitigation(fraudScore);

        FraudAssessmentDossier dossier = new FraudAssessmentDossier(
                dossierId,
                request.entityId(),
                request.entityType(),
                fraudScore,
                threatLevel,
                action,
                signals,
                Instant.now()
        );

        fraudDossierRegistry.put(dossierId, dossier);
        return dossier;
    }

    public Optional<DeploymentRiskEvaluation> getDeploymentEvaluation(String evaluationId) {
        return Optional.ofNullable(deploymentRiskRegistry.get(evaluationId));
    }

    public Optional<FraudAssessmentDossier> getFraudDossier(String dossierId) {
        return Optional.ofNullable(fraudDossierRegistry.get(dossierId));
    }

    private RiskLevel resolveRiskLevel(double score) {
        if (score >= 0.80) return RiskLevel.CRITICAL;
        if (score >= 0.60) return RiskLevel.HIGH;
        if (score >= 0.35) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private MitigationAction resolveFraudMitigation(double fraudScore) {
        if (fraudScore >= 0.80) return MitigationAction.QUARANTINE_SUSPEND;
        if (fraudScore >= 0.60) return MitigationAction.CHALLENGE_MFA;
        if (fraudScore >= 0.35) return MitigationAction.THROTTLE_RATE_LIMIT;
        return MitigationAction.ALLOW;
    }

    private List<String> resolveDeploymentMitigations(RiskLevel level, boolean blocked, List<RiskFactor> factors) {
        List<String> mitigations = new ArrayList<>();
        if (blocked) {
            mitigations.add("BLOCK: Deployment halted. Mandates Principal Architect sign-off.");
            mitigations.add("Mandatory zero-downtime Blue/Green dry-run in staging required.");
        } else if (level == RiskLevel.HIGH) {
            mitigations.add("Deploy canary slice (5% traffic) with automated rollback guard.");
            mitigations.add("Require peer code review approval from team lead.");
        } else {
            mitigations.add("Standard automated CI/CD pipeline deployment allowed.");
        }
        return List.copyOf(mitigations);
    }
}
