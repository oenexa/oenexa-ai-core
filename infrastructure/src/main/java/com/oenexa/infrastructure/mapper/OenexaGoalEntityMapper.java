package com.oenexa.infrastructure.mapper;

import com.oenexa.domain.ChatDomain.LocalPrivacyConfig;
import com.oenexa.domain.ChatDomain.OenexaAgentPlan;
import com.oenexa.domain.ChatDomain.OenexaAutonomousGoalResponse;
import com.oenexa.domain.ChatDomain.OenexaExecutionPhase;
import com.oenexa.infrastructure.persistence.entity.OenexaGoalEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Enterprise Entity Mapper Layer for Autonomous Goals.
 * Translates between JPA OenexaGoalEntity persistence models and Autonomous Goal responses.
 */
@Component
public class OenexaGoalEntityMapper {

    public OenexaGoalEntity toEntity(String goalDescription, String userId, String repositoryId) {
        String id = UUID.randomUUID().toString();
        String user = (userId != null && !userId.isBlank()) ? userId : "default-user";
        String repo = (repositoryId != null && !repositoryId.isBlank()) ? repositoryId : "default-repo";
        return new OenexaGoalEntity(id, user, repo, goalDescription != null ? goalDescription : "");
    }

    public OenexaAutonomousGoalResponse toDomain(OenexaGoalEntity entity) {
        if (entity == null) {
            return null;
        }
        OenexaExecutionPhase phase;
        try {
            phase = OenexaExecutionPhase.valueOf(entity.getCurrentPhase().toUpperCase());
        } catch (Exception e) {
            phase = OenexaExecutionPhase.PLANNING;
        }

        OenexaAgentPlan plan = new OenexaAgentPlan(
                UUID.randomUUID().toString(),
                entity.getId(),
                entity.getGoalDescription(),
                List.of(),
                phase,
                entity.getCreatedAt(),
                Instant.now()
        );

        return new OenexaAutonomousGoalResponse(
                entity.getId(),
                entity.getGoalDescription(),
                plan,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                new LocalPrivacyConfig(),
                entity.getConfidenceScore(),
                entity.getFinalSummary() != null ? entity.getFinalSummary() : "Goal completed.",
                entity.getCompletedAt() != null ? entity.getCompletedAt() : Instant.now()
        );
    }
}
