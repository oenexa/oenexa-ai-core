package com.oenexa.api.dto.swarm;

import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmExecutionPlan;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmExecutionRequest;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmPersona;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmStrategy;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmTaskNode;
import com.oenexa.domain.model.swarm.AutonomousSwarmDomain.SwarmTelemetryRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Request and Response DTOs for Phase 12: Autonomous Multi-Agent Swarm Orchestration.
 */
public final class SwarmDtos {

    private SwarmDtos() {}

    public record ExecuteSwarmRequestDto(
            String goal,
            String strategy,
            List<String> personas,
            String contextRepository,
            Map<String, String> parameters
    ) {
        public SwarmExecutionRequest toDomain() {
            SwarmStrategy strat = SwarmStrategy.PHASED_PIPELINE;
            if (strategy != null) {
                try {
                    strat = SwarmStrategy.valueOf(strategy.toUpperCase());
                } catch (IllegalArgumentException ignored) {}
            }

            List<SwarmPersona> requiredPersonas = new ArrayList<>();
            if (personas != null && !personas.isEmpty()) {
                for (String p : personas) {
                    try {
                        requiredPersonas.add(SwarmPersona.valueOf(p.toUpperCase().replace('-', '_')));
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            return new SwarmExecutionRequest(
                    goal == null ? "Default Enterprise Goal" : goal,
                    strat,
                    requiredPersonas,
                    contextRepository,
                    parameters
            );
        }
    }

    public record SwarmTaskNodeDto(
            String taskId,
            String persona,
            String title,
            String directive,
            List<String> dependencies,
            int stage,
            String status,
            String outputArtifact,
            long durationMs
    ) {
        public static SwarmTaskNodeDto fromDomain(SwarmTaskNode node) {
            return new SwarmTaskNodeDto(
                    node.taskId(),
                    node.persona().name(),
                    node.title(),
                    node.directive(),
                    node.dependencies(),
                    node.stage(),
                    node.status().name(),
                    node.outputArtifact(),
                    node.durationMs()
            );
        }
    }

    public record SwarmExecutionPlanResponseDto(
            String planId,
            String goal,
            String strategy,
            List<SwarmTaskNodeDto> tasks,
            String overallStatus,
            String startedAt,
            String completedAt,
            double speedupFactor,
            int activeVirtualThreads,
            long totalWallClockMs,
            long totalSequentialMs,
            Map<String, String> synthesizedArtifacts
    ) {
        public static SwarmExecutionPlanResponseDto fromDomain(SwarmExecutionPlan plan) {
            List<SwarmTaskNodeDto> taskDtos = plan.tasks().stream()
                    .map(SwarmTaskNodeDto::fromDomain)
                    .toList();

            return new SwarmExecutionPlanResponseDto(
                    plan.planId(),
                    plan.goal(),
                    plan.strategy().name(),
                    taskDtos,
                    plan.overallStatus().name(),
                    plan.startedAt().toString(),
                    plan.completedAt().toString(),
                    plan.speedupFactor(),
                    plan.activeVirtualThreads(),
                    plan.totalWallClockMs(),
                    plan.totalSequentialMs(),
                    plan.synthesizedArtifacts()
            );
        }
    }

    public record SwarmTelemetryResponseDto(
            String planId,
            int activeThreads,
            double heapMemoryKb,
            double concurrencySpeedup,
            int completedTasks,
            int totalTasks
    ) {
        public static SwarmTelemetryResponseDto fromDomain(SwarmTelemetryRecord record) {
            return new SwarmTelemetryResponseDto(
                    record.planId(),
                    record.activeThreads(),
                    record.heapMemoryKb(),
                    record.concurrencySpeedup(),
                    record.completedTasks(),
                    record.totalTasks()
            );
        }
    }
}
