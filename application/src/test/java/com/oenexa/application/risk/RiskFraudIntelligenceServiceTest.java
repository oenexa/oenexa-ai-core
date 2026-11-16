package com.oenexa.application.risk;

import com.oenexa.domain.model.risk.RiskFraudDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RiskFraudIntelligenceService Zero-Mock Unit Tests")
class RiskFraudIntelligenceServiceTest {

    private RiskFraudIntelligenceService service;

    @BeforeEach
    void setUp() {
        service = new RiskFraudIntelligenceService();
    }

    @Test
    @DisplayName("Should detect destructive DDL in production migration and block deployment")
    void testDestructiveDdlBlocksDeployment() {
        EvaluateDeploymentRiskRequest request = new EvaluateDeploymentRiskRequest(
                "oenexa-wallet",
                "production",
                "DROP TABLE legacy_balances; TRUNCATE TABLE audit_log;",
                true,
                List.of("/v1/balances", "/v1/transfers"),
                Map.of("repo", "oenexa-core")
        );

        DeploymentRiskEvaluation eval = service.evaluateDeploymentRisk(request);

        assertNotNull(eval);
        assertTrue(eval.deploymentBlocked());
        assertTrue(eval.riskLevel() == RiskLevel.CRITICAL || eval.riskLevel() == RiskLevel.HIGH);
        assertTrue(eval.factors().stream().anyMatch(f -> f.category() == RiskCategory.DATABASE_MIGRATION));
        assertTrue(eval.recommendedMitigations().stream().anyMatch(m -> m.contains("BLOCK")));

        Optional<DeploymentRiskEvaluation> cached = service.getDeploymentEvaluation(eval.evaluationId());
        assertTrue(cached.isPresent());
    }

    @Test
    @DisplayName("Should allow safe low-risk deployment with non-destructive changes")
    void testSafeDeploymentAllowed() {
        EvaluateDeploymentRiskRequest request = new EvaluateDeploymentRiskRequest(
                "oenexa-frontend",
                "production",
                "feat: update landing page typography and banner text",
                false,
                List.of(),
                Map.of()
        );

        DeploymentRiskEvaluation eval = service.evaluateDeploymentRisk(request);

        assertNotNull(eval);
        assertFalse(eval.deploymentBlocked());
        assertEquals(RiskLevel.LOW, eval.riskLevel());
        assertTrue(eval.recommendedMitigations().stream().anyMatch(m -> m.contains("Standard")));
    }

    @Test
    @DisplayName("Should assess high velocity and headless bot as high-risk fraud and recommend quarantine or MFA")
    void testAssessHighRiskFraud() {
        EvaluateFraudRequest request = new EvaluateFraudRequest(
                "usr-bot-99",
                "ACCOUNT",
                0.95,
                8,
                "192.168.1.100",
                "HeadlessChrome/120.0.0",
                Map.of("country", "RO")
        );

        FraudAssessmentDossier dossier = service.assessFraud(request);

        assertNotNull(dossier);
        assertEquals("usr-bot-99", dossier.entityId());
        assertTrue(dossier.threatLevel() == RiskLevel.CRITICAL || dossier.threatLevel() == RiskLevel.HIGH);
        assertTrue(dossier.recommendedAction() == MitigationAction.QUARANTINE_SUSPEND || dossier.recommendedAction() == MitigationAction.CHALLENGE_MFA);
        assertEquals(3, dossier.signals().size());

        Optional<FraudAssessmentDossier> cached = service.getFraudDossier(dossier.dossierId());
        assertTrue(cached.isPresent());
    }

    @Test
    @DisplayName("Should allow low velocity authenticated user")
    void testAssessLowRiskUser() {
        EvaluateFraudRequest request = new EvaluateFraudRequest(
                "usr-legit-01",
                "USER",
                0.10,
                0,
                "10.0.0.5",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
                Map.of()
        );

        FraudAssessmentDossier dossier = service.assessFraud(request);

        assertNotNull(dossier);
        assertEquals(RiskLevel.LOW, dossier.threatLevel());
        assertEquals(MitigationAction.ALLOW, dossier.recommendedAction());
        assertTrue(dossier.signals().isEmpty());
    }
}
