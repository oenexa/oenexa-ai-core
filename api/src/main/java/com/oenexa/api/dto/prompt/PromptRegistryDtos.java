package com.oenexa.api.dto.prompt;

import com.oenexa.domain.model.prompt.PromptRegistryDomain.EngineeringRole;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Enterprise Prompt Registry API Data Transfer Objects (DTOs).
 * Strictly immutable boundary schemas for request/response serialization.
 */
public final class PromptRegistryDtos {

    private PromptRegistryDtos() {}

    public record RenderPromptRequest(
            Map<String, String> variables
    ) {}

    public record RegisterPromptRequest(
            String templateId,
            String name,
            EngineeringRole role,
            String semanticVersion,
            String systemPrompt,
            String userTemplate,
            List<String> requiredVariables,
            String changelog
    ) {}

    public record PromptTemplateResponse(
            String templateId,
            String name,
            EngineeringRole role,
            String semanticVersion,
            String systemPrompt,
            String userTemplate,
            List<String> requiredVariables,
            Instant createdAt
    ) {}
}
