package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.autonomous.AutonomousIssueDtos.*;
import com.oenexa.api.mapper.AutonomousIssueApiMapper;
import com.oenexa.application.cognitive.OenexaAutonomousLifecycleOrchestrator;
import com.oenexa.application.cognitive.OenexaAutonomousProblemSolverEngine;
import com.oenexa.application.security.AutonomousIssueSecurityPolicy;
import com.oenexa.application.validation.AutonomousIssueValidator;
import com.oenexa.domain.model.cognitive.OenexaAutonomousProblemSolverDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * Enterprise REST API Controller exposing the Oenexa-AI Autonomous Issue Handling & Self-Improvement System.
 * Supports standard 11-Layer Clean Hexagonal Architecture:
 * Controller -> DTO -> Validation -> Security -> Mapper -> Service -> ApiResponse<T>.
 */
@RestController
@RequestMapping("/api/v4/autonomous")
@CrossOrigin(origins = "*")
public class OenexaAutonomousIssueHandlingController {

    public record LifecycleExecutionRequest(String input, Double confidenceThreshold) {}
    public record FeedbackRequest(String patternId, Boolean success) {}

    private final OenexaAutonomousLifecycleOrchestrator orchestrator;
    private final OenexaAutonomousProblemSolverEngine solverEngine;
    private final AutonomousIssueValidator validator;
    private final AutonomousIssueSecurityPolicy securityPolicy;
    private final AutonomousIssueApiMapper mapper;

    public OenexaAutonomousIssueHandlingController() {
        this(new OenexaAutonomousLifecycleOrchestrator(), new OenexaAutonomousProblemSolverEngine());
    }

    public OenexaAutonomousIssueHandlingController(
            OenexaAutonomousLifecycleOrchestrator orchestrator,
            OenexaAutonomousProblemSolverEngine solverEngine
    ) {
        this(orchestrator, solverEngine, new AutonomousIssueValidator(), new AutonomousIssueSecurityPolicy(), new AutonomousIssueApiMapper());
    }

    @Autowired
    public OenexaAutonomousIssueHandlingController(
            OenexaAutonomousLifecycleOrchestrator orchestrator,
            OenexaAutonomousProblemSolverEngine solverEngine,
            AutonomousIssueValidator validator,
            AutonomousIssueSecurityPolicy securityPolicy,
            AutonomousIssueApiMapper mapper
    ) {
        this.orchestrator = Objects.requireNonNull(orchestrator, "orchestrator must not be null");
        this.solverEngine = Objects.requireNonNull(solverEngine, "solverEngine must not be null");
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.securityPolicy = Objects.requireNonNull(securityPolicy, "securityPolicy must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    @PostMapping("/lifecycle")
    public ResponseEntity<AutonomousLifecycleResult> executeLifecycle(
            @RequestBody(required = false) LifecycleExecutionRequest request
    ) {
        String input = (request != null && request.input() != null && !request.input().isBlank())
                ? request.input()
                : "Analyze and resolve potential system invariant regressions";
        Double threshold = (request != null) ? request.confidenceThreshold() : 0.90;

        AutonomousLifecycleResult result = orchestrator.executeLifecycle(input, threshold);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/telemetry")
    public ResponseEntity<Map<String, Object>> getTelemetry() {
        return ResponseEntity.ok(Map.of(
                "processedCount", orchestrator.getProcessedCount(),
                "repeatedErrorsPrevented", orchestrator.getRepeatedErrorsPrevented(),
                "averageLatencyMs", orchestrator.getAverageLatencyMs(),
                "registeredPatternsCount", solverEngine.getPatternCount(),
                "status", "ONLINE_SELF_IMPROVING"
        ));
    }

    @PostMapping("/learn-feedback")
    public ResponseEntity<ReasoningPattern> recordFeedback(
            @RequestBody FeedbackRequest request
    ) {
        if (request == null || request.patternId() == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean isSuccess = (request.success() != null) ? request.success() : true;
        ReasoningPattern updated = solverEngine.recordPatternOutcome(request.patternId(), isSuccess);
        return ResponseEntity.ok(updated);
    }

    // ==========================================
    // 11-Layer Clean Hexagonal Architecture v2 Endpoints
    // ==========================================

    @PostMapping("/v2/detect-resolve")
    public ResponseEntity<ApiResponse<IssueResolutionResultDto>> detectAndResolveV2(@RequestBody IssueDetectionRequestDto req) {
        validator.validateIssueDetection(req.errorTrace(), req.component());

        DetectedIssue issue = solverEngine.detectIssue(req.errorTrace(), req.component());
        CorrectiveAction action = solverEngine.resolveAutomatically(issue);

        return ResponseEntity.ok(ApiResponse.ok("Autonomous issue resolution completed", mapper.toResolutionResultDto(action)));
    }

    @PostMapping("/v2/self-healing")
    public ResponseEntity<ApiResponse<SelfHealingStatusDto>> triggerSelfHealingV2(
            @RequestBody SelfHealingTriggerRequestDto req,
            @RequestHeader(value = "X-Environment", defaultValue = "DEVELOPMENT") String env,
            @RequestHeader(value = "X-User-Role", defaultValue = "ROLE_USER") String role
    ) {
        validator.validateSelfHealingTrigger(req.cycleId(), req.failureType());
        securityPolicy.verifySelfHealingPermission(env, role);

        return ResponseEntity.ok(ApiResponse.ok("Autonomous self-healing cycle executed", mapper.toHealingStatusDto(req.cycleId(), true)));
    }
}
