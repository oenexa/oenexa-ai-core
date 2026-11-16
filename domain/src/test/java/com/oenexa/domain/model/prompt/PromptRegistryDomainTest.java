package com.oenexa.domain.model.prompt;

import com.oenexa.domain.model.prompt.PromptRegistryDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PromptRegistryDomain Zero-Mock Unit Tests")
class PromptRegistryDomainTest {

    @Test
    @DisplayName("Should create PromptTemplate with valid role and required variables")
    void shouldCreatePromptTemplate() {
        PromptTemplate template = new PromptTemplate(
                "p-swe-01",
                "Clean Hexagonal Service Builder",
                EngineeringRole.SOFTWARE_ENGINEER,
                "1.0.0",
                "You are an expert Java 25 software engineer adhering to SRP and Zero-Mock testing.",
                "Implement service {{serviceName}} for domain {{domainName}}.",
                List.of("serviceName", "domainName"),
                null
        );

        assertEquals("p-swe-01", template.templateId());
        assertEquals(EngineeringRole.SOFTWARE_ENGINEER, template.role());
        assertEquals("1.0.0", template.semanticVersion());
        assertEquals(2, template.requiredVariables().size());
        assertTrue(template.validateVariables(Map.of("serviceName", "AuthService", "domainName", "Auth")));
        assertFalse(template.validateVariables(Map.of("serviceName", "AuthService"))); // missing domainName
    }

    @Test
    @DisplayName("Should instantiate RenderedPrompt immutably")
    void shouldInstantiateRenderedPrompt() {
        RenderedPrompt rendered = new RenderedPrompt(
                "p-devops-01",
                "1.1.0",
                EngineeringRole.DEVOPS_ENGINEER,
                "You are a DevOps Engineer specializing in Kubernetes.",
                "Generate deployment manifest for oenexa-service.",
                Map.of("serviceName", "oenexa-service"),
                null
        );

        assertEquals("p-devops-01", rendered.templateId());
        assertEquals(EngineeringRole.DEVOPS_ENGINEER, rendered.role());
        assertEquals("1.1.0", rendered.version());
        assertEquals("oenexa-service", rendered.appliedVariables().get("serviceName"));
        assertNotNull(rendered.renderedAt());
    }

    @Test
    @DisplayName("Should instantiate PromptVersionRecord with changelog")
    void shouldInstantiatePromptVersionRecord() {
        PromptVersionRecord versionRecord = new PromptVersionRecord(
                "p-sec-01",
                "2.0.0",
                "Added OWASP Top 10 SAST audit directives.",
                null
        );

        assertEquals("p-sec-01", versionRecord.templateId());
        assertEquals("2.0.0", versionRecord.version());
        assertTrue(versionRecord.changelog().contains("OWASP"));
        assertNotNull(versionRecord.releaseDate());
    }
}
