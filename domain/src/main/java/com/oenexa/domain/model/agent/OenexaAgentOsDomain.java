package com.oenexa.domain.model.agent;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise Native Autonomous Agent Operating System (OENEXA Agent OS) Domain.
 * Models the 10 specialized software engineering personas, task step scheduling,
 * deliberative reasoning traces, and air-gapped zero-mock verification metrics.
 */
public final class OenexaAgentOsDomain {

    private OenexaAgentOsDomain() {}

    public enum OenexaAgentPersona {
        ARCHITECT_AGENT("Software Architect", "Hexagonal boundaries, sealed records, C4 contracts"),
        BACKEND_AGENT("Backend Engineer", "Reactive WebFlux, Java 27 virtual threads, REST APIs"),
        FRONTEND_AGENT("Frontend Engineer", "React 18, Vite, Material UI, Monaco editor integration"),
        SECURITY_AGENT("AppSec Specialist", "OWASP Top 10, secret detection, SAST vulnerability scanner"),
        QA_AGENT("Verification Engineer", "Strictly zero-mock JUnit 5 test synthesis, 100% JaCoCo"),
        DEVOPS_AGENT("DevOps & SRE", "Multi-stage Docker packaging, Kubernetes manifests, Helm charts"),
        DATABASE_AGENT("Database Engineer", "Flyway schema migrations, PostgreSQL, pgvector indexing"),
        DOCUMENTATION_AGENT("Technical Writer", "OpenAPI 3.0 specs, Mermaid architecture diagrams"),
        REFACTORING_AGENT("Staff Refactoring Engineer", "AST complexity reduction, code smell eradication"),
        PERFORMANCE_AGENT("Performance Engineer", "Generational ZGC tuning, memory leak prevention, async I/O");

        private final String roleTitle;
        private final String coreMission;

        OenexaAgentPersona(String roleTitle, String coreMission) {
            this.roleTitle = roleTitle;
            this.coreMission = coreMission;
        }

        public String getRoleTitle() {
            return roleTitle;
        }

        public String getCoreMission() {
            return coreMission;
        }
    }

    public record OenexaAgentOsGoalRequest(
            String goal,
            String repositoryId,
            String userId,
            boolean sovereignAirGapped,
            String reasoningEffort,
            List<OenexaAgentPersona> selectedPersonas
    ) {
        public OenexaAgentOsGoalRequest {
            Objects.requireNonNull(goal, "goal must not be null");
            repositoryId = (repositoryId != null && !repositoryId.isBlank()) ? repositoryId : "repo-default";
            userId = (userId != null && !userId.isBlank()) ? userId : "engineer";
            reasoningEffort = (reasoningEffort != null && !reasoningEffort.isBlank()) ? reasoningEffort : "high";
            selectedPersonas = (selectedPersonas != null && !selectedPersonas.isEmpty())
                    ? List.copyOf(selectedPersonas)
                    : List.of(OenexaAgentPersona.values());
        }
    }

    public record OenexaAgentOsTaskStep(
            String stepId,
            OenexaAgentPersona persona,
            String action,
            String outputSnippet,
            String status,
            long durationMs
    ) {
        public OenexaAgentOsTaskStep {
            Objects.requireNonNull(stepId, "stepId must not be null");
            Objects.requireNonNull(persona, "persona must not be null");
            Objects.requireNonNull(action, "action must not be null");
            outputSnippet = (outputSnippet != null) ? outputSnippet : "";
            status = (status != null) ? status : "COMPLETED";
        }
    }

    public record OenexaAgentOsMetrics(
            long totalDurationMs,
            int tokensGenerated,
            double tokensPerSecond,
            boolean airGappedVerified,
            boolean zeroMockTestVerified,
            double confidenceScore
    ) {}

    public record OenexaAgentOsGoalResponse(
            String executionId,
            String goal,
            String status,
            OenexaCognitiveDomain.OenexaDeliberativeTrace deliberativeTrace,
            List<OenexaAgentOsTaskStep> executedSteps,
            List<ChatDomain.CodeArtifact> synthesizedArtifacts,
            OenexaAgentOsMetrics metrics
    ) {
        public OenexaAgentOsGoalResponse {
            Objects.requireNonNull(executionId, "executionId must not be null");
            Objects.requireNonNull(goal, "goal must not be null");
            status = (status != null) ? status : "COMPLETED";
            Objects.requireNonNull(deliberativeTrace, "deliberativeTrace must not be null");
            executedSteps = (executedSteps != null) ? List.copyOf(executedSteps) : List.of();
            synthesizedArtifacts = (synthesizedArtifacts != null) ? List.copyOf(synthesizedArtifacts) : List.of();
            Objects.requireNonNull(metrics, "metrics must not be null");
        }
    }
}

