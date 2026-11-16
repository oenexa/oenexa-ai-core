package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.prompt.PromptRegistryDtos.*;
import com.oenexa.application.prompt.PromptRegistryService;
import com.oenexa.domain.model.prompt.PromptRegistryDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PromptRegistryController Zero-Mock Unit Tests")
class PromptRegistryControllerTest {

    private PromptRegistryController controller;

    @BeforeEach
    void setUp() {
        PromptRegistryService service = new PromptRegistryService();
        controller = new PromptRegistryController(service);
    }

    @Test
    @DisplayName("Should list all prompt templates via REST endpoint")
    void shouldListTemplates() {
        ResponseEntity<ApiResponse<List<PromptTemplateResponse>>> response = controller.listTemplates(null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertFalse(response.getBody().data().isEmpty());
    }

    @Test
    @DisplayName("Should filter prompt templates by role via query param")
    void shouldFilterTemplatesByRole() {
        ResponseEntity<ApiResponse<List<PromptTemplateResponse>>> response = controller.listTemplates("SOFTWARE_ENGINEER");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());

        List<PromptTemplateResponse> data = response.getBody().data();
        assertFalse(data.isEmpty());
        assertTrue(data.stream().allMatch(p -> p.role() == EngineeringRole.SOFTWARE_ENGINEER));
    }

    @Test
    @DisplayName("Should get single prompt template by ID")
    void shouldGetTemplateById() {
        ResponseEntity<ApiResponse<PromptTemplateResponse>> response = controller.getTemplate("prompt-swe-01");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("prompt-swe-01", response.getBody().data().templateId());
    }

    @Test
    @DisplayName("Should render prompt with variable interpolation via REST endpoint")
    void shouldRenderPrompt() {
        RenderPromptRequest req = new RenderPromptRequest(Map.of("componentType", "Record", "domainName", "Security"));
        ResponseEntity<ApiResponse<RenderedPrompt>> response = controller.renderPrompt("prompt-swe-01", req);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().data().renderedUserPrompt().contains("Record"));
        assertTrue(response.getBody().data().renderedUserPrompt().contains("Security"));
    }

    @Test
    @DisplayName("Should register new prompt template via REST endpoint")
    void shouldRegisterTemplate() {
        RegisterPromptRequest req = new RegisterPromptRequest(
                "prompt-cloud-01",
                "Terraform AWS Sizing",
                EngineeringRole.CLOUD_ENGINEER,
                "1.0.0",
                "You are an AWS Cloud Architect.",
                "Size EC2 instances for {{workload}}.",
                List.of("workload"),
                "Initial cloud prompt"
        );

        ResponseEntity<ApiResponse<PromptTemplateResponse>> response = controller.registerTemplate(req);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("prompt-cloud-01", response.getBody().data().templateId());
    }
}
