package com.oenexa.application.prompt;

import com.oenexa.domain.model.prompt.PromptRegistryDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PromptRegistryService Zero-Mock Unit Tests")
class PromptRegistryServiceTest {

    private PromptRegistryService promptRegistryService;

    @BeforeEach
    void setUp() {
        promptRegistryService = new PromptRegistryService();
    }

    @Test
    @DisplayName("Should list all seeded prompt templates")
    void shouldListSeededTemplates() {
        List<PromptTemplate> templates = promptRegistryService.listTemplates();
        assertNotNull(templates);
        assertFalse(templates.isEmpty());
        assertTrue(templates.size() >= 6);
    }

    @Test
    @DisplayName("Should filter prompt templates by engineering role")
    void shouldFilterTemplatesByRole() {
        List<PromptTemplate> swePrompts = promptRegistryService.listByRole(EngineeringRole.SOFTWARE_ENGINEER);
        assertFalse(swePrompts.isEmpty());
        assertTrue(swePrompts.stream().allMatch(p -> p.role() == EngineeringRole.SOFTWARE_ENGINEER));

        List<PromptTemplate> devopsPrompts = promptRegistryService.listByRole(EngineeringRole.DEVOPS_ENGINEER);
        assertFalse(devopsPrompts.isEmpty());
        assertTrue(devopsPrompts.stream().allMatch(p -> p.role() == EngineeringRole.DEVOPS_ENGINEER));
    }

    @Test
    @DisplayName("Should render prompt template with variable interpolation")
    void shouldRenderPromptWithVariables() {
        RenderedPrompt rendered = promptRegistryService.renderPrompt(
                "prompt-swe-01",
                Map.of("componentType", "Service", "domainName", "Payment")
        );

        assertNotNull(rendered);
        assertEquals(EngineeringRole.SOFTWARE_ENGINEER, rendered.role());
        assertTrue(rendered.renderedUserPrompt().contains("Service"));
        assertTrue(rendered.renderedUserPrompt().contains("Payment"));
        assertFalse(rendered.renderedUserPrompt().contains("{{componentType}}"));
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when rendering unknown prompt")
    void shouldThrowWhenPromptNotFound() {
        assertThrows(NoSuchElementException.class, () ->
                promptRegistryService.renderPrompt("unknown-id", Map.of())
        );
    }

    @Test
    @DisplayName("Should register new custom prompt template and track version history")
    void shouldRegisterNewTemplateAndTrackVersion() {
        PromptTemplate newTemplate = new PromptTemplate(
                "prompt-writer-01",
                "Technical Writer API Doc Generator",
                EngineeringRole.TECHNICAL_WRITER,
                "1.0.0",
                "You are an API technical writer.",
                "Generate OpenAPI documentation for {{endpoint}}.",
                List.of("endpoint"),
                null
        );

        promptRegistryService.registerTemplate(newTemplate, "Added technical writer template");

        assertTrue(promptRegistryService.getTemplate("prompt-writer-01").isPresent());
        List<PromptVersionRecord> history = promptRegistryService.getVersionHistory("prompt-writer-01");
        assertEquals(1, history.size());
        assertEquals("1.0.0", history.get(0).version());
    }
}
