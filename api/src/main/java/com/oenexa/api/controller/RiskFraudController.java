package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.risk.RiskFraudDtos.*;
import com.oenexa.application.risk.RiskFraudIntelligenceService;
import com.oenexa.domain.model.risk.RiskFraudDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

/**
 * Enterprise Risk & Fraud Intelligence REST API Controller.
 * Exposes endpoints for:
 * 1. Pre-deployment blast radius & database migration risk assessment (`/api/v4/risk/deployment/evaluate`)
 * 2. Cached deployment risk evaluation query (`/api/v4/risk/deployment/{evaluationId}`)
 * 3. Real-time velocity, sybil, and auth fraud scoring (`/api/v4/risk/fraud/assess`)
 * 4. Fraud assessment dossier retrieval (`/api/v4/risk/fraud/{dossierId}`)
 */
@RestController
@RequestMapping("/api/v4/risk")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class RiskFraudController {

    private final RiskFraudIntelligenceService riskFraudService;

    public RiskFraudController(RiskFraudIntelligenceService riskFraudService) {
        this.riskFraudService = Objects.requireNonNull(riskFraudService, "riskFraudService must not be null");
    }

    @PostMapping("/deployment/evaluate")
    public ResponseEntity<ApiResponse<DeploymentRiskEvaluationDto>> evaluateDeploymentRisk(
            @RequestBody EvaluateDeploymentRiskRequestDto requestDto
    ) {
        EvaluateDeploymentRiskRequest request = (requestDto != null)
                ? requestDto.toDomain()
                : new EvaluateDeploymentRiskRequest("unknown", "production", "", false, null, null);

        DeploymentRiskEvaluation domainEval = riskFraudService.evaluateDeploymentRisk(request);
        return ResponseEntity.ok(ApiResponse.ok(DeploymentRiskEvaluationDto.fromDomain(domainEval)));
    }

    @GetMapping("/deployment/{evaluationId}")
    public ResponseEntity<ApiResponse<DeploymentRiskEvaluationDto>> getDeploymentEvaluation(
            @PathVariable("evaluationId") String evaluationId
    ) {
        Optional<DeploymentRiskEvaluation> evaluation = riskFraudService.getDeploymentEvaluation(evaluationId);
        return evaluation
                .map(e -> ResponseEntity.ok(ApiResponse.ok(DeploymentRiskEvaluationDto.fromDomain(e))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.failure(404, "Deployment evaluation not found: " + evaluationId)));
    }

    @PostMapping("/fraud/assess")
    public ResponseEntity<ApiResponse<FraudAssessmentDossierDto>> assessFraud(
            @RequestBody EvaluateFraudRequestDto requestDto
    ) {
        EvaluateFraudRequest request = (requestDto != null)
                ? requestDto.toDomain()
                : new EvaluateFraudRequest("unknown", "ACCOUNT", 0.0, 0, "", "", null);

        FraudAssessmentDossier dossier = riskFraudService.assessFraud(request);
        return ResponseEntity.ok(ApiResponse.ok(FraudAssessmentDossierDto.fromDomain(dossier)));
    }

    @GetMapping("/fraud/{dossierId}")
    public ResponseEntity<ApiResponse<FraudAssessmentDossierDto>> getFraudDossier(
            @PathVariable("dossierId") String dossierId
    ) {
        Optional<FraudAssessmentDossier> dossier = riskFraudService.getFraudDossier(dossierId);
        return dossier
                .map(d -> ResponseEntity.ok(ApiResponse.ok(FraudAssessmentDossierDto.fromDomain(d))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.failure(404, "Fraud dossier not found: " + dossierId)));
    }
}
