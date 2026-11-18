package com.oenexa.api.controller.swarm;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.swarm.SwarmDtos.ExecuteSwarmRequestDto;
import com.oenexa.api.dto.swarm.SwarmDtos.SwarmExecutionPlanResponseDto;
import com.oenexa.api.dto.swarm.SwarmDtos.SwarmTelemetryResponseDto;
import com.oenexa.application.swarm.AutonomousSwarmExecutionService;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmExecutionPlan;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmTelemetryRecord;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for Phase 12: Autonomous Multi-Agent Swarm Orchestrator.
 * Exposes endpoints for executing concurrent virtual thread DAG plans and tracking telemetry.
 */
@RestController
@RequestMapping("/api/v4/agents/swarm")
public class SwarmExecutionController {

    private final AutonomousSwarmExecutionService swarmService;

    public SwarmExecutionController(AutonomousSwarmExecutionService swarmService) {
        this.swarmService = Objects.requireNonNull(swarmService, "swarmService must not be null");
    }

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<SwarmExecutionPlanResponseDto>> executeSwarmPlan(
            @RequestBody(required = false) ExecuteSwarmRequestDto requestDto
    ) {
        ExecuteSwarmRequestDto effectiveDto = requestDto != null ? requestDto : new ExecuteSwarmRequestDto(null, null, null, null, null);
        SwarmExecutionPlan plan = swarmService.executeSwarmPlan(effectiveDto.toDomain());
        return ResponseEntity.ok(ApiResponse.ok(SwarmExecutionPlanResponseDto.fromDomain(plan)));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSwarmPlan(
            @RequestParam(required = false, defaultValue = "Autonomous Enterprise System Engineering") String goal,
            @RequestParam(required = false, defaultValue = "PARALLEL_MAX_SPEED") String strategy
    ) {
        SseEmitter emitter = new SseEmitter(60_000L);
        emitter.onTimeout(emitter::complete);
        emitter.onError(ex -> emitter.complete());

        Thread.startVirtualThread(() -> {
            try {
                ExecuteSwarmRequestDto requestDto = new ExecuteSwarmRequestDto(goal, strategy, null, null, null);
                swarmService.executeSwarmPlanWithStreaming(requestDto.toDomain(), event -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name(event.eventType())
                                .data(event));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(Map.of("message", e.getMessage() != null ? e.getMessage() : "Swarm stream error")));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping("/{planId}/status")
    public ResponseEntity<ApiResponse<SwarmExecutionPlanResponseDto>> getPlanStatus(
            @PathVariable("planId") String planId
    ) {
        Optional<SwarmExecutionPlan> planOpt = swarmService.getSwarmPlan(planId);
        return planOpt.map(plan -> ResponseEntity.ok(ApiResponse.ok(SwarmExecutionPlanResponseDto.fromDomain(plan))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.failure(404, "Swarm execution plan not found: " + planId)));
    }

    @GetMapping("/{planId}/telemetry")
    public ResponseEntity<ApiResponse<SwarmTelemetryResponseDto>> getPlanTelemetry(
            @PathVariable("planId") String planId
    ) {
        Optional<SwarmTelemetryRecord> telemetryOpt = swarmService.getSwarmTelemetry(planId);
        return telemetryOpt.map(record -> ResponseEntity.ok(ApiResponse.ok(SwarmTelemetryResponseDto.fromDomain(record))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.failure(404, "Swarm telemetry not found: " + planId)));
    }
}
