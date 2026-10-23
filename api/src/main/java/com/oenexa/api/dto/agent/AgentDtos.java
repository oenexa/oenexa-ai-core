package com.oenexa.api.dto.agent;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized DTO Layer for Autonomous Agent & Brain Domain.
 */
public class AgentDtos {

    public record GoalSubmissionDto(
            String goal,
            String repositoryId,
            boolean selfHealingEnabled,
            boolean localFirst
    ) {
        public GoalSubmissionDto {
            goal = (goal != null) ? goal : "";
            repositoryId = (repositoryId != null && !repositoryId.isBlank()) ? repositoryId : "default-repo";
        }
    }

    public record GoalExecutionStatusDto(
            String goalId,
            String goalDescription,
            String status,
            String currentPhase,
            int totalSteps,
            int completedSteps,
            double confidenceScore,
            Instant startedAt
    ) {}

    public record AgentDirectiveDto(
            String directiveId,
            String objective,
            String preferredModel,
            String strategy,
            Map<String, Object> parameters
    ) {
        public AgentDirectiveDto {
            objective = (objective != null) ? objective : "";
            preferredModel = (preferredModel != null && !preferredModel.isBlank()) ? preferredModel : "oenexa-neo-gpt-o3-max";
            strategy = (strategy != null && !strategy.isBlank()) ? strategy : "AUTONOMOUS";
            parameters = (parameters != null) ? Map.copyOf(parameters) : Map.of();
        }
    }
}
