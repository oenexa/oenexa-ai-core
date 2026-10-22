package com.oenexa.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Domain contracts for Phase 5: Autonomous AI Agents Ecosystem.
 * Models the 8 autonomous agent types, goal planning, multi-step reasoning,
 * tool calling, self reflection, memory management, and team collaboration.
 */
public class AutonomousAgentEcosystemDomain {

    public enum EnterpriseAgentType {
        SOFTWARE_ENGINEER,
        ARCHITECT,
        DEVOPS,
        SECURITY,
        RESEARCH,
        PRODUCT_MANAGER,
        DATA_ENGINEER,
        QA,
        FINANCE,
        CUSTOMER_SUPPORT,
        MARKETING,
        LEGAL_ASSISTANT
    }

    public enum ExecutionStatus {
        PENDING, IN_PROGRESS, SUCCESS, NEEDS_CORRECTION, FAILED
    }

    public record AgentProfile(
            EnterpriseAgentType type,
            String displayName,
            String description,
            List<String> coreSkills,
            List<String> toolsAvailable,
            String primaryModel
    ) {
        public AgentProfile {
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(displayName, "displayName must not be null");
            coreSkills = (coreSkills != null) ? List.copyOf(coreSkills) : List.of();
            toolsAvailable = (toolsAvailable != null) ? List.copyOf(toolsAvailable) : List.of();
        }
    }

    public record AgentToolInvocation(
            String toolName,
            String inputParameters,
            String executionOutput,
            boolean isSuccessful
    ) {
        public AgentToolInvocation {
            Objects.requireNonNull(toolName, "toolName must not be null");
        }
    }

    public record AgentReflectionResult(
            String stepId,
            boolean passed,
            int confidenceScore,
            String critique,
            String correctiveActionTaken
    ) {
        public AgentReflectionResult {
            Objects.requireNonNull(stepId, "stepId must not be null");
        }
    }

    public record AgentPlanStep(
            String stepId,
            EnterpriseAgentType agentType,
            String stepDescription,
            String toolToInvoke,
            ExecutionStatus status,
            String outputResult,
            AgentReflectionResult reflection,
            int retryCount
    ) {
        public AgentPlanStep {
            Objects.requireNonNull(stepId, "stepId must not be null");
            Objects.requireNonNull(agentType, "agentType must not be null");
            Objects.requireNonNull(stepDescription, "stepDescription must not be null");
            status = (status != null) ? status : ExecutionStatus.PENDING;
            outputResult = (outputResult != null) ? outputResult : "";
        }
    }

    public record AgentCollaborationEvent(
            String eventId,
            EnterpriseAgentType fromAgent,
            EnterpriseAgentType toAgent,
            String messageOrHandoff,
            Instant timestamp
    ) {
        public AgentCollaborationEvent {
            Objects.requireNonNull(eventId, "eventId must not be null");
            Objects.requireNonNull(fromAgent, "fromAgent must not be null");
            Objects.requireNonNull(toAgent, "toAgent must not be null");
            timestamp = (timestamp != null) ? timestamp : Instant.now();
        }
    }

    public record AgentGoalExecutionPlan(
            String planId,
            String goalTitle,
            String goalObjective,
            String currentPhase,
            List<AgentPlanStep> steps,
            List<AgentCollaborationEvent> collaborationLog,
            List<String> learnedMemories,
            boolean completed,
            Instant createdAt,
            Instant updatedAt
    ) {
        public AgentGoalExecutionPlan {
            Objects.requireNonNull(planId, "planId must not be null");
            Objects.requireNonNull(goalTitle, "goalTitle must not be null");
            steps = (steps != null) ? List.copyOf(steps) : List.of();
            collaborationLog = (collaborationLog != null) ? List.copyOf(collaborationLog) : List.of();
            learnedMemories = (learnedMemories != null) ? List.copyOf(learnedMemories) : List.of();
            createdAt = (createdAt != null) ? createdAt : Instant.now();
            updatedAt = (updatedAt != null) ? updatedAt : Instant.now();
        }
    }

    public enum AgentThreadState {
        IDLE,
        RUNNING,
        WAITING,
        COMPLETED,
        FAILED
    }

    public record AgentThreadExecutionRecord(
            String threadId,
            String threadName,
            EnterpriseAgentType agentType,
            String taskDescription,
            AgentThreadState state,
            long startEpochMs,
            long executionDurationMs,
            String outputResult,
            int memoryAllocatedKb
    ) {
        public AgentThreadExecutionRecord {
            Objects.requireNonNull(threadId, "threadId must not be null");
            Objects.requireNonNull(threadName, "threadName must not be null");
            Objects.requireNonNull(agentType, "agentType must not be null");
            Objects.requireNonNull(taskDescription, "taskDescription must not be null");
            state = (state != null) ? state : AgentThreadState.IDLE;
            outputResult = (outputResult != null) ? outputResult : "";
        }
    }

    public record ConcurrentAgentSwarmResult(
            String swarmId,
            String goalTitle,
            String concurrencyMode,
            int activeThreadCount,
            long wallClockDurationMs,
            long sequentialDurationMs,
            double speedupFactor,
            List<AgentThreadExecutionRecord> threadRecords,
            List<String> consolidatedArtifacts,
            boolean allPassed
    ) {
        public ConcurrentAgentSwarmResult {
            Objects.requireNonNull(swarmId, "swarmId must not be null");
            Objects.requireNonNull(goalTitle, "goalTitle must not be null");
            concurrencyMode = (concurrencyMode != null) ? concurrencyMode : "JAVA_25_STRUCTURED_VIRTUAL_THREADS";
            threadRecords = (threadRecords != null) ? List.copyOf(threadRecords) : List.of();
            consolidatedArtifacts = (consolidatedArtifacts != null) ? List.copyOf(consolidatedArtifacts) : List.of();
        }
    }
}
