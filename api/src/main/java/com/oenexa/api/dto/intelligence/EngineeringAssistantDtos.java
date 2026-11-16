package com.oenexa.api.dto.intelligence;

import com.oenexa.domain.model.intelligence.EngineeringAssistantDomain.DocType;

import java.time.Instant;
import java.util.List;

/**
 * Enterprise Engineering Assistant API Data Transfer Objects (DTOs).
 * Strictly immutable boundary schemas for request/response serialization.
 */
public final class EngineeringAssistantDtos {

    private EngineeringAssistantDtos() {}

    public record AnalyzeRepoRequest(
            String repositoryName,
            List<String> files,
            String sampleCode
    ) {}

    public record ReviewDevOpsRequest(
            String applicationName,
            String dockerfileContent,
            String kubernetesYamlContent
    ) {}

    public record SecurityAuditRequest(
            String targetScope,
            String codeContent
    ) {}

    public record GenerateDocRequest(
            String title,
            DocType docType,
            String context
    ) {}
}
