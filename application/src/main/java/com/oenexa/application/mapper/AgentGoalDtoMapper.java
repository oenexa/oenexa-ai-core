package com.oenexa.application.mapper;

import com.oenexa.domain.model.chat.ChatDomain.LocalPrivacyConfig;
import com.oenexa.domain.model.chat.ChatDomain.OenexaAgentPlan;
import com.oenexa.domain.model.chat.ChatDomain.OenexaAutonomousGoalRequest;
import com.oenexa.domain.model.chat.ChatDomain.OenexaAutonomousGoalResponse;
import com.oenexa.domain.model.chat.ChatDomain.OenexaExecutionPhase;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Enterprise Mapper Layer for Autonomous Goal & Agent Orchestration Domain.
 * Maps between user goal requests, internal execution plans, and response summaries.
 */
@Component
public class AgentGoalDtoMapper {

    public OenexaAutonomousGoalResponse toCompletedResponse(
            OenexaAutonomousGoalRequest request,
            String goalId,
            OenexaAgentPlan plan,
            String summary
    ) {
        if (request == null) {
            return null;
        }
        String id = (goalId != null && !goalId.isBlank()) ? goalId : UUID.randomUUID().toString();
        OenexaAgentPlan effectivePlan = (plan != null)
                ? plan
                : new OenexaAgentPlan(UUID.randomUUID().toString(), id, request.goal(), List.of(), OenexaExecutionPhase.COMPLETED, Instant.now(), Instant.now());

        return new OenexaAutonomousGoalResponse(
                id,
                request.goal(),
                effectivePlan,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                new LocalPrivacyConfig(request.localFirstMode(), true, "in-process", "oenexa-neo-gpt-o3-max", 8192, true),
                0.99,
                summary != null ? summary : "Autonomous goal achieved successfully.",
                Instant.now()
        );
    }
}

