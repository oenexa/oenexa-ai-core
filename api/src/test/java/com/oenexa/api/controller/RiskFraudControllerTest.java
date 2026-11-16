package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.risk.RiskFraudDtos.*;
import com.oenexa.application.risk.RiskFraudIntelligenceService;
import com.oenexa.domain.model.risk.RiskFraudDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RiskFraudController Zero-Mock Unit Tests")
class RiskFraudControllerTest {

    private RiskFraudIntelligenceService service;
    private RiskFraudController controller;

    @BeforeEach
    void setUp() {
        service = new RiskFraudIntelligenceService();
        controller = new RiskFraudController(service);
    }

    @Test
    @DisplayName("Should evaluate deployment risk, block production destructive DDL, and retrieve evaluation")
    void testDeploymentEvaluationFlow() {
        EvaluateDeploymentRiskRequestDto requestDto = new EvaluateDeploymentRiskRequestDto(
                "oenexa-ledger",
                "production",
                "DROP TABLE balances; TRUNCATE ledger;",
                true,
                List.of("/v1/balances"),
                Map.of("branch", "main")
        );

        ResponseEntity<ApiResponse<DeploymentRiskEvaluationDto>> response = controller.evaluateDeploymentRisk(requestDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().success());

        DeploymentRiskEvaluationDto eval = response.getBody().data();
        assertNotNull(eval.evaluationId());
        assertTrue(eval.deploymentBlocked());
        assertEquals("oenexa-ledger", eval.serviceName());

        ResponseEntity<ApiResponse<DeploymentRiskEvaluationDto>> retrieved = controller.getDeploymentEvaluation(eval.evaluationId());
        assertEquals(200, retrieved.getStatusCode().value());
        assertTrue(retrieved.getBody().success());
        assertEquals(eval.evaluationId(), retrieved.getBody().data().evaluationId());
    }

    @Test
    @DisplayName("Should return 404 for missing deployment evaluation")
    void testGetMissingDeploymentEvaluation() {
        ResponseEntity<ApiResponse<DeploymentRiskEvaluationDto>> response = controller.getDeploymentEvaluation("RISK-9999");
        assertEquals(404, response.getStatusCode().value());
        assertFalse(response.getBody().success());
    }

    @Test
    @DisplayName("Should assess high-velocity bot fraud and retrieve dossier")
    void testFraudAssessmentFlow() {
        EvaluateFraudRequestDto requestDto = new EvaluateFraudRequestDto(
                "usr-hacker-42",
                "USER",
                0.92,
                10,
                "185.220.101.5",
                "python-requests/2.31.0",
                Map.of()
        );

        ResponseEntity<ApiResponse<FraudAssessmentDossierDto>> response = controller.assessFraud(requestDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().success());

        FraudAssessmentDossierDto dossier = response.getBody().data();
        assertNotNull(dossier.dossierId());
        assertEquals("usr-hacker-42", dossier.entityId());
        assertTrue(dossier.threatLevel() == RiskLevel.CRITICAL || dossier.threatLevel() == RiskLevel.HIGH);
        assertTrue(dossier.recommendedAction() == MitigationAction.QUARANTINE_SUSPEND || dossier.recommendedAction() == MitigationAction.CHALLENGE_MFA);

        ResponseEntity<ApiResponse<FraudAssessmentDossierDto>> retrieved = controller.getFraudDossier(dossier.dossierId());
        assertEquals(200, retrieved.getStatusCode().value());
        assertTrue(retrieved.getBody().success());
        assertEquals(dossier.dossierId(), retrieved.getBody().data().dossierId());
    }

    @Test
    @DisplayName("Should return 404 for missing fraud dossier")
    void testGetMissingFraudDossier() {
        ResponseEntity<ApiResponse<FraudAssessmentDossierDto>> response = controller.getFraudDossier("FRAUD-9999");
        assertEquals(404, response.getStatusCode().value());
        assertFalse(response.getBody().success());
    }
}
