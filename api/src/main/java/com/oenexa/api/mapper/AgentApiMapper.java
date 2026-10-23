package com.oenexa.api.mapper;

import com.oenexa.api.dto.agent.AgentDtos.AgentDirectiveDto;
import com.oenexa.api.dto.agent.AgentDtos.GoalExecutionStatusDto;
import com.oenexa.api.dto.agent.AgentDtos.GoalSubmissionDto;
import com.oenexa.domain.ChatDomain.OenexaAutonomousGoalRequest;
import com.oenexa.domain.ChatDomain.OenexaAutonomousGoalResponse;
import com.oenexa.domain.OenexaCoreBrainDomain.CoreBrainDirective;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Enterprise API Mapper for Autonomous Agent & Brain DTOs.
 */
@Component
public class AgentApiMapper {

    public OenexaAutonomousGoalRequest toDomainGoalRequest(GoalSubmissionDto dto, String userId) {
        if (dto == null) {
            return null;
        }
        return new OenexaAutonomousGoalRequest(
                dto.goal(),
                dto.repositoryId(),
                userId != null ? userId : "default-user",
                dto.selfHealingEnabled(),
                true,
                List.of(),
                dto.localFirst(),
                List.of()
        );
    }

    public GoalExecutionStatusDto toGoalStatusDto(OenexaAutonomousGoalResponse response) {
        if (response == null) {
            return null;
        }
        int totalSteps = (response.plan() != null && response.plan().steps() != null) ? response.plan().steps().size() : 0;
        int completedSteps = (response.plan() != null && response.plan().steps() != null)
                ? (int) response.plan().steps().stream().filter(s -> "COMPLETED".equalsIgnoreCase(s.status())).count()
                : 0;
        String phase = (response.plan() != null && response.plan().currentPhase() != null)
                ? response.plan().currentPhase().name()
                : "COMPLETED";

        return new GoalExecutionStatusDto(
                response.goalId(),
                response.goalDescription(),
                "SUCCESS",
                phase,
                totalSteps,
                completedSteps,
                response.confidenceScore(),
                response.completedAt()
        );
    }

    public CoreBrainDirective toBrainDirective(AgentDirectiveDto dto) {
        if (dto == null) {
            return null;
        }
        return new CoreBrainDirective(
                dto.directiveId(),
                "default",
                "engineer",
                dto.objective(),
                dto.preferredModel(),
                true,
                "high",
                dto.parameters()
        );
    }
}
