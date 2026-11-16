package com.oenexa.application.document;

import com.oenexa.domain.model.document.DocumentAnalyticsDomain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enterprise Document Generation, Platform Analytics, and Developer Support Intelligence Service.
 */
@Service
public class DocumentAnalyticsSupportService {

    private final Map<String, GeneratedDocument> documentRegistry = new ConcurrentHashMap<>();
    private final AtomicLong documentCounter = new AtomicLong(100);
    private final AtomicLong supportCounter = new AtomicLong(500);

    public GeneratedDocument generateDocument(GenerateDocumentRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String documentId = "DOC-" + documentCounter.incrementAndGet();
        String markdown = buildDocumentMarkdown(request);

        GeneratedDocument doc = new GeneratedDocument(
                documentId,
                request.type(),
                request.title(),
                markdown,
                request.parameters(),
                Instant.now()
        );

        documentRegistry.put(documentId, doc);
        return doc;
    }

    public Optional<GeneratedDocument> getDocument(String documentId) {
        return Optional.ofNullable(documentRegistry.get(documentId));
    }

    public PlatformAnalytics getPlatformAnalytics(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        Map<String, Long> distribution = Map.of(
                "oenexa-gpt-o3", 145000L,
                "claude-3-5-sonnet", 82000L,
                "gemini-1-5-pro", 64000L,
                "oenexa-local-qwen", 32000L
        );

        return new PlatformAnalytics(
                tenantId,
                323000L,
                4.845,
                18L,
                24.5,
                distribution,
                Instant.now()
        );
    }

    public DeveloperSupportResolution resolveSupportQuery(DeveloperSupportQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        String q = query.question().toLowerCase();
        List<String> docs = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        String summary;
        double confidence;

        if (q.contains("test") || q.contains("mock")) {
            summary = "OENEXA enforces a strict 100% Zero-Mock testing policy. Instantiate real records and services directly without Mockito.";
            docs.add("docs/testing/zero-mock-guidelines.md");
            commands.add("./gradlew test");
            commands.add("npm test -- --run");
            confidence = 0.98;
        } else if (q.contains("deploy") || q.contains("k8s") || q.contains("docker")) {
            summary = "Production deployments require pre-flight blast radius evaluation and zero destructive DDL without architect sign-off.";
            docs.add("docs/devops/deployment-guardrails.md");
            commands.add("kubectl get pods -n oenexa-prod");
            commands.add("helm status oenexa-ai-core");
            confidence = 0.95;
        } else if (q.contains("architecture") || q.contains("hexagonal") || q.contains("layer")) {
            summary = "OENEXA AI Core is a Clean Hexagonal Monolith (domain, application, infrastructure, api). Domain classes must be pure Java 25 records.";
            docs.add("docs/adr/0001-hexagonal-architecture.md");
            commands.add("./gradlew :domain:test");
            confidence = 0.96;
        } else {
            summary = "General developer support query matched against OENEXA Engineering Knowledge Base.";
            docs.add("docs/developer-handbook.md");
            commands.add("./gradlew build");
            confidence = 0.85;
        }

        return new DeveloperSupportResolution(
                query.queryId(),
                summary,
                docs,
                commands,
                confidence,
                Instant.now()
        );
    }

    private String buildDocumentMarkdown(GenerateDocumentRequest request) {
        String title = request.title();
        String context = request.context().isBlank() ? "Standard enterprise baseline specification." : request.context();

        return switch (request.type()) {
            case ADR -> String.format(
                    "# ADR: %s%n%n## Status%nAccepted%n%n## Context%n%s%n%n## Decision%nAdopt Clean Hexagonal Modular Monolith in Java 25 LTS.%n%n## Consequences%n- Zero framework lock-in in domain.%n- High modular maintainability.%n",
                    title, context
            );
            case TECHNICAL_SPECIFICATION -> String.format(
                    "# Technical Specification: %s%n%n## Executive Summary%n%s%n%n## Layer Invariants%n- Controller Layer (api)%n- Service Layer (application)%n- Domain Layer (domain)%n- Repository Layer (infrastructure)%n",
                    title, context
            );
            case POST_MORTEM_RUNBOOK -> String.format(
                    "# Incident Post-Mortem & Runbook: %s%n%n## Incident Background%n%s%n%n## Root Cause Analysis%nAutomated triage confirmed category and affected services.%n%n## Immediate Action Items%n- Verify SLO error budget.%n- Execute canary failover.%n",
                    title, context
            );
            case DEVELOPER_GUIDE -> String.format(
                    "# Developer Guide: %s%n%n## Overview%n%s%n%n## Getting Started%n1. Clone repository%n2. Run `./gradlew test`%n3. Run `npm test -- --run`%n",
                    title, context
            );
            case API_CONTRACT -> String.format(
                    "# API Contract Specification: %s%n%n## Description%n%s%n%n## Standard Envelope%nAll endpoints wrap responses in ApiResponse<T> envelope.%n",
                    title, context
            );
        };
    }
}
