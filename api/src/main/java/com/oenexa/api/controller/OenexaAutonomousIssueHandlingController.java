package com.oenexa.api.controller;

import com.oenexa.application.cognitive.OenexaAutonomousLifecycleOrchestrator;
import com.oenexa.application.cognitive.OenexaAutonomousProblemSolverEngine;
import com.oenexa.domain.OenexaAutonomousProblemSolverDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * Enterprise REST API Controller exposing the Oenexa-AI Autonomous Issue Handling & Self-Improvement System.
 * Operates across the 7-stage lifecycle:
 * Detect -> Analyze -> Resolve Automatically -> Learn -> Escalate Intelligence -> Self-Upgrade -> Continuous Improvement.
 */
@RestController
@RequestMapping("/api/v4/autonomous")
@CrossOrigin(origins = "*")
public class OenexaAutonomousIssueHandlingController {

    public record LifecycleExecutionRequest(String input, Double confidenceThreshold) {}
    public record FeedbackRequest(String patternId, Boolean success) {}

    private final OenexaAutonomousLifecycleOrchestrator orchestrator;
    private final OenexaAutonomousProblemSolverEngine solverEngine;

    public OenexaAutonomousIssueHandlingController() {
        this(new OenexaAutonomousLifecycleOrchestrator(), new OenexaAutonomousProblemSolverEngine());
    }

    @Autowired
    public OenexaAutonomousIssueHandlingController(
            OenexaAutonomousLifecycleOrchestrator orchestrator,
            OenexaAutonomousProblemSolverEngine solverEngine
    ) {
        this.orchestrator = Objects.requireNonNull(orchestrator, "orchestrator must not be null");
        this.solverEngine = Objects.requireNonNull(solverEngine, "solverEngine must not be null");
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
}
