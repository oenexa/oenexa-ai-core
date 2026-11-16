package com.oenexa.domain.model.swarm;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Domain records for Phase 12: Autonomous Multi-Agent Swarm Orchestrator.
 * Pure Java 25 records with zero framework dependencies and defensive invariants.
 */
public final class AutonomousSwarmDomain {

    private AutonomousSwarmDomain() {}

    public enum SwarmStrategy {
        PARALLEL_MAX_SPEED,
        PHASED_PIPELINE,
        FAILSAFE_CONSENSUS
    }

    public enum SwarmPersona {
        SYSTEM_ARCHITECT("system-architect", "C4 Modeling, Domain Boundaries & Architectural Governance"),
        SOFTWARE_ENGINEER("software-engineer", "Clean Code Synthesis, Refactoring & Defensive Programming"),
        SECURITY_AUDITOR("security-auditor", "SAST, OWASP Top 10, Secrets Leakage & Zero-Trust Checks"),
        DEVOPS_ENGINEER("devops-engineer", "Distroless Docker, K8s Manifests & CI/CD Pipelines"),
        QA_VERIFICATION("qa-verification", "100% Zero-Mock Test Generation & Mutation Coverage"),
        DATA_ENGINEER("data-engineer", "Schema DDL, Migrations & Vector Storage Optimization"),
        SRE_ENGINEER("sre-engineer", "Incident Triage, Root Cause Analysis & Runbook Resolution");

        private final String roleId;
        private final String capability;

        SwarmPersona(String roleId, String capability) {
            this.roleId = roleId;
            this.capability = capability;
        }

        public String roleId() {
            return roleId;
        }

        public String capability() {
            return capability;
        }
    }

    public enum SwarmTaskStatus {
        QUEUED,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    public record SwarmTaskNode(
            String taskId,
            SwarmPersona persona,
            String title,
            String directive,
            List<String> dependencies,
            int stage,
            SwarmTaskStatus status,
            String outputArtifact,
            long durationMs
    ) {
        public SwarmTaskNode {
            Objects.requireNonNull(taskId, "taskId must not be null");
            Objects.requireNonNull(persona, "persona must not be null");
            Objects.requireNonNull(title, "title must not be null");
            Objects.requireNonNull(directive, "directive must not be null");
            dependencies = dependencies == null ? List.of() : List.copyOf(dependencies);
            status = status == null ? SwarmTaskStatus.QUEUED : status;
            outputArtifact = outputArtifact == null ? "" : outputArtifact;
        }

        public SwarmTaskNode withStatus(SwarmTaskStatus newStatus, String artifact, long duration) {
            return new SwarmTaskNode(taskId, persona, title, directive, dependencies, stage, newStatus, artifact, duration);
        }
    }

    public record SwarmExecutionRequest(
            String goal,
            SwarmStrategy strategy,
            List<SwarmPersona> requiredPersonas,
            String contextRepository,
            Map<String, String> parameters
    ) {
        public SwarmExecutionRequest {
            Objects.requireNonNull(goal, "goal must not be null");
            strategy = strategy == null ? SwarmStrategy.PHASED_PIPELINE : strategy;
            requiredPersonas = (requiredPersonas == null || requiredPersonas.isEmpty())
                    ? List.of(SwarmPersona.SYSTEM_ARCHITECT, SwarmPersona.SOFTWARE_ENGINEER, SwarmPersona.QA_VERIFICATION)
                    : List.copyOf(requiredPersonas);
            contextRepository = contextRepository == null ? "oenexa-ai-core" : contextRepository;
            parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        }
    }

    public record SwarmExecutionPlan(
            String planId,
            String goal,
            SwarmStrategy strategy,
            List<SwarmTaskNode> tasks,
            SwarmTaskStatus overallStatus,
            Instant startedAt,
            Instant completedAt,
            double speedupFactor,
            int activeVirtualThreads,
            long totalWallClockMs,
            long totalSequentialMs,
            Map<String, String> synthesizedArtifacts
    ) {
        public SwarmExecutionPlan {
            Objects.requireNonNull(planId, "planId must not be null");
            Objects.requireNonNull(goal, "goal must not be null");
            Objects.requireNonNull(strategy, "strategy must not be null");
            tasks = tasks == null ? List.of() : List.copyOf(tasks);
            overallStatus = overallStatus == null ? SwarmTaskStatus.QUEUED : overallStatus;
            startedAt = startedAt == null ? Instant.now() : startedAt;
            completedAt = completedAt == null ? startedAt : completedAt;
            synthesizedArtifacts = synthesizedArtifacts == null ? Map.of() : Map.copyOf(synthesizedArtifacts);
        }
    }

    public record SwarmTelemetryRecord(
            String planId,
            int activeThreads,
            double heapMemoryKb,
            double concurrencySpeedup,
            int completedTasks,
            int totalTasks
    ) {
        public SwarmTelemetryRecord {
            Objects.requireNonNull(planId, "planId must not be null");
        }
    }
}
