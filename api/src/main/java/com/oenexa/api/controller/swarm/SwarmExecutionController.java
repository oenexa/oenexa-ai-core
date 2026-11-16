package com.oenexa.api.controller.swarm;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.swarm.SwarmDtos.ExecuteSwarmRequestDto;
import com.oenexa.api.dto.swarm.SwarmDtos.SwarmExecutionPlanResponseDto;
import com.oenexa.api.dto.swarm.SwarmDtos.SwarmTelemetryResponseDto;
import com.oenexa.application.swarm.AutonomousSwarmExecutionService;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmExecutionPlan;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmTelemetryRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
