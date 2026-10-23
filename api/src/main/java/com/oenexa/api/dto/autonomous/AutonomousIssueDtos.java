package com.oenexa.api.dto.autonomous;

/**
 * Standard Data Transfer Objects for Autonomous Problem Solving and Self-Healing.
 */
public final class AutonomousIssueDtos {

    private AutonomousIssueDtos() {}

    public record IssueDetectionRequestDto(
            String errorTrace,
            String component,
            String environment,
            String severity
    ) {}

    public record IssueResolutionResultDto(
            String issueId,
            String rootCause,
            String resolutionPlan,
            boolean autoHealed,
            String patchDiff
    ) {}

    public record SelfHealingTriggerRequestDto(
            String cycleId,
            String failureType,
            String affectedResource
    ) {}

    public record SelfHealingStatusDto(
            String cycleId,
            String status,
            int attemptCount,
            boolean restored
    ) {}
}
