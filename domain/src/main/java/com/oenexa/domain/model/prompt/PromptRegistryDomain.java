package com.oenexa.domain.model.prompt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Enterprise Prompt Registry Domain models and specifications.
 * Enforces reusable, versioned, and standardized prompt templates
 * tailored for specialized enterprise engineering personas.
 * Pure Java 25 Domain Model (Zero framework dependencies).
 */
public final class PromptRegistryDomain {

    private PromptRegistryDomain() {}

    public enum EngineeringRole {
        SOFTWARE_ENGINEER("Software Engineer", "Code synthesis, defensive records, refactoring, and bug fixes"),
        SYSTEM_ARCHITECT("System Architect", "C4 architecture modeling, clean hexagonal boundaries, and ADRs"),
        DEVOPS_ENGINEER("DevOps Engineer", "Distroless Dockerfiles, Kubernetes manifests, Helm charts, and CI/CD"),
        CLOUD_ENGINEER("Cloud Platform Engineer", "Cloud infrastructure sizing, networking, and Terraform modules"),
        SECURITY_ENGINEER("Security Engineer", "SAST static scanning, OWASP top 10 detection, and zero-trust policies"),
        QA_ENGINEER("QA Verification Engineer", "Zero-mock unit, contract, and integration test suites"),
        SRE_ENGINEER("Site Reliability Engineer", "Incident response, runbooks, distributed telemetry, and SLOs"),
        TECHNICAL_WRITER("Technical Writer", "Architecture documentation, OpenAPI specs, and developer guides"),
        PRODUCT_ARCHITECT("Product Architect", "User stories, INVEST criteria, and feature roadmaps");

        private final String title;
        private final String focus;

        EngineeringRole(String title, String focus) {
            this.title = title;
            this.focus = focus;
        }

        public String getTitle() {
            return title;
        }

        public String getFocus() {
            return focus;
        }
    }

    public record PromptTemplate(
            String templateId,
            String name,
            EngineeringRole role,
            String semanticVersion,
            String systemPrompt,
            String userTemplate,
            List<String> requiredVariables,
            Instant createdAt
    ) {
        public PromptTemplate {
            templateId = (templateId != null) ? templateId : "prompt-" + UUID.randomUUID();
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(role, "role must not be null");
            semanticVersion = (semanticVersion != null && !semanticVersion.isBlank()) ? semanticVersion : "1.0.0";
            Objects.requireNonNull(systemPrompt, "systemPrompt must not be null");
            Objects.requireNonNull(userTemplate, "userTemplate must not be null");
            requiredVariables = (requiredVariables != null) ? List.copyOf(requiredVariables) : List.of();
            createdAt = (createdAt != null) ? createdAt : Instant.now();
        }

        public boolean validateVariables(Map<String, String> provided) {
            if (provided == null) {
                return requiredVariables.isEmpty();
            }
            return requiredVariables.stream().allMatch(provided::containsKey);
        }
    }

    public record RenderedPrompt(
            String templateId,
            String version,
            EngineeringRole role,
            String renderedSystemPrompt,
            String renderedUserPrompt,
            Map<String, String> appliedVariables,
            Instant renderedAt
    ) {
        public RenderedPrompt {
            Objects.requireNonNull(templateId, "templateId must not be null");
            version = (version != null) ? version : "1.0.0";
            Objects.requireNonNull(role, "role must not be null");
            Objects.requireNonNull(renderedSystemPrompt, "renderedSystemPrompt must not be null");
            Objects.requireNonNull(renderedUserPrompt, "renderedUserPrompt must not be null");
            appliedVariables = (appliedVariables != null) ? Map.copyOf(appliedVariables) : Map.of();
            renderedAt = (renderedAt != null) ? renderedAt : Instant.now();
        }
    }

    public record PromptVersionRecord(
            String templateId,
            String version,
            String changelog,
            Instant releaseDate
    ) {
        public PromptVersionRecord {
            Objects.requireNonNull(templateId, "templateId must not be null");
            Objects.requireNonNull(version, "version must not be null");
            changelog = (changelog != null) ? changelog : "Initial version";
            releaseDate = (releaseDate != null) ? releaseDate : Instant.now();
        }
    }
}
