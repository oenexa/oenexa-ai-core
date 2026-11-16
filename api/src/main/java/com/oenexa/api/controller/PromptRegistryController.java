package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.prompt.PromptRegistryDtos.*;
import com.oenexa.application.prompt.PromptRegistryService;
import com.oenexa.domain.model.prompt.PromptRegistryDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Enterprise Prompt Registry REST API Controller.
 * Manages reusable, versioned prompts across all engineering disciplines.
 */
@RestController
@RequestMapping("/api/v4/prompts")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class PromptRegistryController {

    private final PromptRegistryService promptRegistryService;

    public PromptRegistryController(PromptRegistryService promptRegistryService) {
        this.promptRegistryService = Objects.requireNonNull(promptRegistryService, "promptRegistryService must not be null");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PromptTemplateResponse>>> listTemplates(
            @RequestParam(value = "role", required = false) String roleStr
    ) {
        EngineeringRole role = null;
        if (roleStr != null && !roleStr.isBlank()) {
            try {
                role = EngineeringRole.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // fall back to listing all
            }
        }

        List<PromptTemplate> templates = (role != null)
                ? promptRegistryService.listByRole(role)
                : promptRegistryService.listTemplates();

        List<PromptTemplateResponse> response = templates.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<ApiResponse<PromptTemplateResponse>> getTemplate(@PathVariable String templateId) {
        return promptRegistryService.getTemplate(templateId)
                .map(t -> ResponseEntity.ok(ApiResponse.ok(toResponse(t))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{templateId}/render")
    public ResponseEntity<ApiResponse<RenderedPrompt>> renderPrompt(
            @PathVariable String templateId,
            @RequestBody(required = false) RenderPromptRequest request
    ) {
        try {
            var vars = (request != null && request.variables() != null) ? request.variables() : null;
            RenderedPrompt rendered = promptRegistryService.renderPrompt(templateId, vars);
            return ResponseEntity.ok(ApiResponse.ok(rendered));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PromptTemplateResponse>> registerTemplate(
            @RequestBody RegisterPromptRequest request
    ) {
        if (request == null || request.name() == null || request.role() == null || request.userTemplate() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(400, "Missing required template parameters"));
        }

        PromptTemplate template = new PromptTemplate(
                request.templateId(),
                request.name(),
                request.role(),
                request.semanticVersion(),
                request.systemPrompt() != null ? request.systemPrompt() : "You are an AI engineering assistant.",
                request.userTemplate(),
                request.requiredVariables(),
                null
        );

        String changelog = (request.changelog() != null) ? request.changelog() : "Registered via REST API";
        promptRegistryService.registerTemplate(template, changelog);

        return ResponseEntity.ok(ApiResponse.created(toResponse(template)));
    }

    private PromptTemplateResponse toResponse(PromptTemplate t) {
        return new PromptTemplateResponse(
                t.templateId(),
                t.name(),
                t.role(),
                t.semanticVersion(),
                t.systemPrompt(),
                t.userTemplate(),
                t.requiredVariables(),
                t.createdAt()
        );
    }
}
