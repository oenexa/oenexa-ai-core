package com.oenexa.application.prompt;

import com.oenexa.domain.model.prompt.PromptRegistryDomain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise Prompt Registry Application Service.
 * Central manager for reusable, versioned prompts across all engineering disciplines.
 * Enforces role specialization, semantic versioning, and variable interpolation.
 */
@Service
public class PromptRegistryService {

    private final Map<String, PromptTemplate> templateStore = new ConcurrentHashMap<>();
    private final Map<String, List<PromptVersionRecord>> versionHistory = new ConcurrentHashMap<>();

    public PromptRegistryService() {
        seedStandardEngineeringPrompts();
    }

    public List<PromptTemplate> listTemplates() {
        return List.copyOf(templateStore.values());
    }

    public Optional<PromptTemplate> getTemplate(String templateId) {
        if (templateId == null) return Optional.empty();
        return Optional.ofNullable(templateStore.get(templateId));
    }

    public List<PromptTemplate> listByRole(EngineeringRole role) {
        if (role == null) return listTemplates();
        return templateStore.values().stream()
                .filter(t -> t.role() == role)
                .toList();
    }

    public RenderedPrompt renderPrompt(String templateId, Map<String, String> variables) {
        PromptTemplate template = getTemplate(templateId)
                .orElseThrow(() -> new NoSuchElementException("Prompt template not found: " + templateId));

        Map<String, String> vars = (variables != null) ? variables : Map.of();

        String renderedUser = template.userTemplate();
        String renderedSystem = template.systemPrompt();

        for (Map.Entry<String, String> entry : vars.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            renderedUser = renderedUser.replace(placeholder, entry.getValue());
            renderedSystem = renderedSystem.replace(placeholder, entry.getValue());
        }

        return new RenderedPrompt(
                template.templateId(),
                template.semanticVersion(),
                template.role(),
                renderedSystem,
                renderedUser,
                vars,
                Instant.now()
        );
    }

    public void registerTemplate(PromptTemplate template, String changelog) {
        Objects.requireNonNull(template, "template must not be null");
        templateStore.put(template.templateId(), template);

        versionHistory.computeIfAbsent(template.templateId(), k -> new ArrayList<>())
                .add(new PromptVersionRecord(template.templateId(), template.semanticVersion(), changelog, Instant.now()));
    }

    public List<PromptVersionRecord> getVersionHistory(String templateId) {
        return List.copyOf(versionHistory.getOrDefault(templateId, List.of()));
    }

    private void seedStandardEngineeringPrompts() {
        registerTemplate(new PromptTemplate(
                "prompt-swe-01",
                "Clean Hexagonal Java 25 Implementation",
                EngineeringRole.SOFTWARE_ENGINEER,
                "1.0.0",
                "You are an expert Principal Java 25 Software Engineer. You write defensive records, clean hexagonal code, and maintain strict SRP (< 400 lines) with zero mocks.",
                "Implement {{componentType}} for the {{domainName}} module. Ensure 100% adherence to pure Java records in domain and clean separation.",
                List.of("componentType", "domainName"),
                Instant.now()
        ), "Initial baseline template for Java 25 clean architecture.");

        registerTemplate(new PromptTemplate(
                "prompt-arch-01",
                "C4 System Architecture & ADR Formulation",
                EngineeringRole.SYSTEM_ARCHITECT,
                "1.0.0",
                "You are a Chief Enterprise System Architect. You model systems using C4 diagrams, define hexagonal boundaries, and formulate decisive ADR records.",
                "Analyze architecture for {{systemName}} with requirements: {{requirements}}. Document context, decision, and trade-offs.",
                List.of("systemName", "requirements"),
                Instant.now()
        ), "Initial baseline template for C4 modeling.");

        registerTemplate(new PromptTemplate(
                "prompt-devops-01",
                "Distroless Kubernetes & GitOps Manifest Synthesis",
                EngineeringRole.DEVOPS_ENGINEER,
                "1.0.0",
                "You are a Senior Platform DevOps Engineer. You build multi-stage distroless Dockerfiles, hardened Kubernetes deployments, and automated CI/CD pipelines.",
                "Generate distroless Dockerfile and Kubernetes manifests for {{appName}} exposing port {{port}}.",
                List.of("appName", "port"),
                Instant.now()
        ), "Initial baseline template for distroless k8s.");

        registerTemplate(new PromptTemplate(
                "prompt-sec-01",
                "OWASP SAST Security Audit & Secrets Scan",
                EngineeringRole.SECURITY_ENGINEER,
                "1.0.0",
                "You are a Chief Information Security Officer (CISO) and AppSec Specialist. You detect OWASP top 10 vulnerabilities, credential leaks, and enforce zero-trust.",
                "Audit the following source file {{filename}} for security vulnerabilities:\n\n{{code}}",
                List.of("filename", "code"),
                Instant.now()
        ), "Initial baseline template for AppSec audits.");

        registerTemplate(new PromptTemplate(
                "prompt-qa-01",
                "Zero-Mock Comprehensive Test Synthesizer",
                EngineeringRole.QA_ENGINEER,
                "1.0.0",
                "You are a Staff QA Engineer. You enforce 100% Zero-Mock testing standards using real instantiated domain objects without Mockito.",
                "Generate JUnit 5 Zero-Mock unit test suite for {{targetClass}} covering all boundary conditions and invariants.",
                List.of("targetClass"),
                Instant.now()
        ), "Initial baseline template for zero-mock testing.");

        registerTemplate(new PromptTemplate(
                "prompt-sre-01",
                "Production Incident Runbook & RCA Generator",
                EngineeringRole.SRE_ENGINEER,
                "1.0.0",
                "You are a Principal SRE. You author actionable production runbooks, error recovery procedures, and post-incident Root Cause Analysis (RCA) reports.",
                "Generate operational runbook for incident {{incidentName}} with alert symptom: {{symptom}}.",
                List.of("incidentName", "symptom"),
                Instant.now()
        ), "Initial baseline template for SRE runbooks.");

        registerTemplate(new PromptTemplate(
                "prompt-cloud-01",
                "Terraform Cloud Infrastructure Provisioning",
                EngineeringRole.CLOUD_ENGINEER,
                "1.0.0",
                "You are a Senior Cloud Platform Engineer. You author secure Terraform modules, IAM least-privilege policies, and multi-region VPC topologies.",
                "Generate Terraform module for {{resourceName}} in region {{region}} adhering to zero-trust networking.",
                List.of("resourceName", "region"),
                Instant.now()
        ), "Initial baseline template for cloud infrastructure.");

        registerTemplate(new PromptTemplate(
                "prompt-doc-01",
                "Technical Writer API & Architecture Documentation",
                EngineeringRole.TECHNICAL_WRITER,
                "1.0.0",
                "You are a Lead Technical Writer. You write pristine Markdown developer documentation, OpenAPI 3.1 specifications, and clear onboarding tutorials.",
                "Author comprehensive developer guide for {{moduleName}} covering integration, error codes, and examples.",
                List.of("moduleName"),
                Instant.now()
        ), "Initial baseline template for technical documentation.");

        registerTemplate(new PromptTemplate(
                "prompt-prod-01",
                "Product Requirements & User Story Formulation",
                EngineeringRole.PRODUCT_ARCHITECT,
                "1.0.0",
                "You are an Executive Product Architect. You formulate clear PRDs, INVEST user stories, acceptance criteria, and edge-case definitions.",
                "Draft PRD and acceptance criteria for feature {{featureName}} targeting persona {{targetUser}}.",
                List.of("featureName", "targetUser"),
                Instant.now()
        ), "Initial baseline template for product architecture.");
    }
}
