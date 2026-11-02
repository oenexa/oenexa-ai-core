package com.oenexa.api.controller;

import com.oenexa.application.service.code.AiSoftwareEngineerService;
import com.oenexa.domain.model.code.AiEngineerDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Enterprise REST API for Phase 4: AI Software Engineer.
 * Exposes endpoints for Pull Request reviews, API synthesis, code refactoring,
 * bug fixes, and DevOps infrastructure automation.
 */
@RestController
@RequestMapping("/api/v1/engineer")
@CrossOrigin(originPatterns = "*")
public class AiEngineerController {

    private final AiSoftwareEngineerService engineerService;

    public AiEngineerController(AiSoftwareEngineerService engineerService) {
        this.engineerService = Objects.requireNonNull(engineerService, "engineerService must not be null");
    }

    @PostMapping("/pr-review")
    public ResponseEntity<PrReviewResult> reviewPullRequest(@RequestBody(required = false) PrReviewRequest request) {
        PrReviewRequest safeReq = (request != null) ? request : new PrReviewRequest(null, null, null, null, null);
        return ResponseEntity.ok(engineerService.reviewPullRequest(safeReq));
    }

    @PostMapping("/api-generate")
    public ResponseEntity<ApiGenerationResult> generateApi(@RequestBody(required = false) ApiGenerationRequest request) {
        ApiGenerationRequest safeReq = (request != null) ? request : new ApiGenerationRequest(null, null, null, null, null);
        return ResponseEntity.ok(engineerService.generateApi(safeReq));
    }

    @PostMapping("/refactor")
    public ResponseEntity<RefactoringResult> refactorCode(@RequestBody(required = false) RefactoringRequest request) {
        RefactoringRequest safeReq = (request != null) ? request : new RefactoringRequest(null, null, null);
        return ResponseEntity.ok(engineerService.refactorCode(safeReq));
    }

    @PostMapping("/bug-fix")
    public ResponseEntity<BugFixResult> fixBug(@RequestBody(required = false) BugFixRequest request) {
        BugFixRequest safeReq = (request != null) ? request : new BugFixRequest(null, null, null);
        return ResponseEntity.ok(engineerService.fixBug(safeReq));
    }

    @PostMapping("/infra-generate")
    public ResponseEntity<InfrastructureAutomationResult> generateInfrastructure(
            @RequestBody(required = false) InfrastructureAutomationRequest request
    ) {
        InfrastructureAutomationRequest safeReq = (request != null)
                ? request
                : new InfrastructureAutomationRequest(null, null, 8080, true, true);
        return ResponseEntity.ok(engineerService.generateInfrastructure(safeReq));
    }
}


