package com.oenexa.domain.model.risk;

import com.oenexa.domain.model.risk.RiskFraudDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RiskFraudDomain Invariant Tests (Zero-Mock)")
class RiskFraudDomainTest {

    @Test
    @DisplayName("RiskFactor clamps weights and immutably retains category")
    void testRiskFactorDefaults() {
        RiskFactor factor = new RiskFactor(
                "RF-01",
                RiskCategory.DATABASE_MIGRATION,
                "Table lock on production accounts table",
                1.45,
                true
        );

        assertEquals("RF-01", factor.factorId());
        assertEquals(RiskCategory.DATABASE_MIGRATION, factor.category());
        assertEquals(1.0, factor.weight(), 0.001);
        assertTrue(factor.isTriggered());
    }

    @Test
    @DisplayName("DeploymentRiskEvaluation enforces non-null invariants and clamps overall score")
    void testDeploymentRiskEvaluation() {
        DeploymentRiskEvaluation eval = new DeploymentRiskEvaluation(
                "EVAL-101",
                "oenexa-auth",
                "production",
                0.88,
                RiskLevel.HIGH,
                List.of(new RiskFactor("RF-02", RiskCategory.API_BREAKING_CHANGE, "Removed /v3 endpoint", 0.8, true)),
                List.of("Deploy canary with 10% traffic first"),
                false
        );

        assertEquals("EVAL-101", eval.evaluationId());
        assertEquals("oenexa-auth", eval.serviceName());
        assertEquals(0.88, eval.overallScore(), 0.001);
        assertEquals(RiskLevel.HIGH, eval.riskLevel());
        assertEquals(1, eval.factors().size());
        assertFalse(eval.deploymentBlocked());
    }

    @Test
    @DisplayName("FraudSignal clamps anomaly confidence and defaults nulls safely")
    void testFraudSignalDefaults() {
        FraudSignal signal = new FraudSignal(
                "SIG-99",
                "CREDENTIAL_STUFFING",
                1.2,
                null,
                "50 attempts across 10 user accounts"
        );

        assertEquals("SIG-99", signal.signalId());
        assertEquals(1.0, signal.anomalyConfidence(), 0.001);
        assertEquals("GATEWAY", signal.telemetrySource());
    }

    @Test
    @DisplayName("FraudAssessmentDossier validates entity ID and defaults action")
    void testFraudAssessmentDossier() {
        FraudAssessmentDossier dossier = new FraudAssessmentDossier(
                "FD-202",
                "usr-4040",
                "USER",
                0.92,
                RiskLevel.CRITICAL,
                MitigationAction.QUARANTINE_SUSPEND,
                List.of(new FraudSignal("SIG-01", "SYBIL_ATTACK", 0.95, "IP_CLUSTER", "12 accounts on same subnet")),
                null
        );

        assertNotNull(dossier.evaluatedAt());
        assertEquals("usr-4040", dossier.entityId());
        assertEquals(RiskLevel.CRITICAL, dossier.threatLevel());
        assertEquals(MitigationAction.QUARANTINE_SUSPEND, dossier.recommendedAction());
        assertEquals(1, dossier.signals().size());
    }

    @Test
    @DisplayName("EvaluateDeploymentRiskRequest safely handles null metadata and diff summary")
    void testEvaluateDeploymentRiskRequestDefaults() {
        EvaluateDeploymentRiskRequest req = new EvaluateDeploymentRiskRequest(
                "oenexa-node",
                null,
                null,
                true,
                null,
                null
        );

        assertEquals("production", req.targetEnvironment());
        assertEquals("", req.diffSummary());
        assertTrue(req.hasDatabaseMigration());
        assertTrue(req.modifiedEndpoints().isEmpty());
        assertTrue(req.metadata().isEmpty());
    }
}
