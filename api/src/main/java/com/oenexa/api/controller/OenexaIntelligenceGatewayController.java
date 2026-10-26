package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.application.intelligence.OenexaIntelligenceCoreEngines.*;
import com.oenexa.domain.model.intelligence.OenexaIntelligenceDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sovereign REST Gateway Controller for OENEXA Intelligence System.
 * Adheres strictly to OENEXA original architecture and standards.
 */
@RestController
@RequestMapping("/api/v1/oenexa")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OenexaIntelligenceGatewayController {

    private final IntelligenceEngine intelligenceEngine;
    private final ProjectEngine projectEngine;
    private final MemoryEngine memoryEngine;
    private final MonitoringEngine monitoringEngine;

    public OenexaIntelligenceGatewayController(
            IntelligenceEngine intelligenceEngine,
            ProjectEngine projectEngine,
            MemoryEngine memoryEngine,
            MonitoringEngine monitoringEngine
    ) {
        this.intelligenceEngine = intelligenceEngine;
        this.projectEngine = projectEngine;
        this.memoryEngine = memoryEngine;
        this.monitoringEngine = monitoringEngine;
    }

    public record ProcessDirectiveRequest(
            String query,
            String workspaceId,
            String sovereignEnginePreference,
            List<String> contextFiles
    ) {}

    public record ExecuteTaskRequest(
            String engineIdentifier,
            String command
    ) {}

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<OenexaIntelligenceResponse>> processDirective(
            @RequestHeader(value = "X-Oenexa-User-Id", defaultValue = "sovereign-engineer") String userId,
            @RequestBody ProcessDirectiveRequest request
    ) {
        OenexaPrompt prompt = new OenexaPrompt(
                UUID.randomUUID(),
                userId,
                request.workspaceId() != null ? request.workspaceId() : "oenexarepo",
                request.query() != null ? request.query() : "Analyze OENEXA Invariants",
                request.contextFiles(),
                request.sovereignEnginePreference(),
                Instant.now()
        );

        OenexaIntelligenceResponse response = intelligenceEngine.process(prompt);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/task")
    public ResponseEntity<ApiResponse<OenexaTaskExecution>> executeTask(
            @RequestBody ExecuteTaskRequest request
    ) {
        OenexaTaskExecution execution = intelligenceEngine.executeTask(
                request.engineIdentifier() != null ? request.engineIdentifier() : "OENEXA-TaskEngine-v1",
                request.command() != null ? request.command() : "echo 'OENEXA Active'"
        );
        return ResponseEntity.ok(ApiResponse.ok(execution));
    }

    @GetMapping("/workspaces")
    public ResponseEntity<ApiResponse<List<String>>> listWorkspaces() {
        return ResponseEntity.ok(ApiResponse.ok(projectEngine.listRegisteredWorkspaces()));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<OenexaIntelligenceResponse>>> getInteractionHistory(
            @RequestHeader(value = "X-Oenexa-User-Id", defaultValue = "sovereign-engineer") String userId,
            @RequestParam(value = "workspaceId", defaultValue = "oenexarepo") String workspaceId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(memoryEngine.retrieveHistory(userId, workspaceId)));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        return ResponseEntity.ok(ApiResponse.ok(monitoringEngine.getSystemHealth()));
    }
}
