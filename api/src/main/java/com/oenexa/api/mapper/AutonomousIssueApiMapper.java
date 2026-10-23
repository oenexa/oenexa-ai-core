package com.oenexa.api.mapper;

import com.oenexa.api.dto.autonomous.AutonomousIssueDtos.*;
import com.oenexa.domain.model.cognitive.OenexaAutonomousProblemSolverDomain.*;
import org.springframework.stereotype.Component;

/**
 * Enterprise API Mapper for Autonomous Problem Solving & Self-Healing DTOs.
 */
@Component
public class AutonomousIssueApiMapper {

    public IssueResolutionResultDto toResolutionResultDto(CorrectiveAction action) {
        if (action == null) {
            return null;
        }
        return new IssueResolutionResultDto(
                action.targetIssueId(),
                action.title(),
                action.description(),
                action.validated(),
                "diff --git a/src b/src\n+// " + action.title()
        );
    }

    public SelfHealingStatusDto toHealingStatusDto(String cycleId, boolean restored) {
        return new SelfHealingStatusDto(
                cycleId,
                restored ? "RESTORED" : "FAILED",
                1,
                restored
        );
    }
}
