package ai.oenexa.documentation.service;

import ai.oenexa.documentation.domain.DiagramRequest;
import ai.oenexa.documentation.domain.DiagramResult;
import ai.oenexa.documentation.domain.DocFormat;
import ai.oenexa.documentation.domain.DocType;
import ai.oenexa.documentation.domain.DocumentationRequest;
import ai.oenexa.documentation.domain.DocumentationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class DefaultDocumentationService implements DocumentationService {

    @Override
    public DocumentationResult generateDocumentation(DocumentationRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String title = request.title();
        DocType type = request.docType();
        DocFormat format = request.format();
        String context = request.context();
        List<String> components = request.components();

        List<String> sections = new ArrayList<>();
        StringBuilder content = new StringBuilder();

        switch (type) {
            case ADR -> {
                sections.addAll(List.of("Status", "Context", "Decision", "Consequences"));
                content.append("# ADR: ").append(title).append("\n\n")
                        .append("## 1. Status\n**ACCEPTED** (Target Runtime: Java 25+, Spring Boot 4.1.1)\n\n")
                        .append("## 2. Context\n").append(!context.isEmpty() ? context : "Architectural requirement evaluation.").append("\n\n")
                        .append("## 3. Decision\nAdopt modern immutable Java records, virtual threads, and clean hexagonal architecture.\n\n")
                        .append("## 4. Consequences\nPositive: Zero boilerplate, verified thread-safety, standard 100% test coverage.\n");
            }
            case API_REFERENCE -> {
                sections.addAll(List.of("Overview", "Authentication", "Endpoints", "Error Codes"));
                content.append("# API Reference: ").append(title).append("\n\n")
                        .append("## 1. Overview\nREST API reference specification with standard JSON payloads.\n\n")
                        .append("## 2. Endpoints\n")
                        .append("| Method | Path | Description | Access |\n")
                        .append("|:---|:---|:---|:---|\n");
                if (components.isEmpty()) {
                    content.append("| GET | /api/v1/health | Liveness & Readiness check | Public |\n")
                            .append("| POST | /api/v1/auth/login | Token issuance | Public |\n");
                } else {
                    for (String comp : components) {
                        content.append("| POST | /api/v1/").append(comp.toLowerCase()).append(" | Operations for ").append(comp).append(" | Authenticated |\n");
                    }
                }
                content.append("\n## 3. Error Codes\nRFC 7807 problem details format returned on errors.\n");
            }
            case MERMAID_DIAGRAM -> {
                sections.addAll(List.of("Diagram", "Explanation"));
                DiagramResult diagram = generateDiagram(new DiagramRequest("flowchart TD", title, components, List.of()));
                content.append("# Diagram: ").append(title).append("\n\n")
                        .append("```mermaid\n").append(diagram.mermaidSource()).append("\n```\n\n")
                        .append("## Explanation\nStructural relationship overview between declared system components.\n");
            }
            case ARCHITECTURE -> {
                sections.addAll(List.of("Introduction", "Components Topology", "Data Flow", "Quality Attributes"));
                content.append("# Architecture Specification: ").append(title).append("\n\n")
                        .append("## 1. Introduction\n").append(!context.isEmpty() ? context : "High-level architecture design.").append("\n\n")
                        .append("## 2. Components Topology\n");
                if (components.isEmpty()) {
                    content.append("- Core Monolith Engine (`api`)\n- Gateway Service (`oenexa-gateway`)\n");
                } else {
                    for (String comp : components) {
                        content.append("- Subsystem Component: `").append(comp).append("`\n");
                    }
                }
                content.append("\n## 3. Data Flow\nEvent-driven orchestration using Kafka and non-blocking Spring MVC handlers.\n\n")
                        .append("## 4. Quality Attributes\nStrict zero-mock policy with >= 80% JaCoCo coverage gate.\n");
            }
        }

        String contentStr = content.toString();
        int wordCount = contentStr.isEmpty() ? 0 : contentStr.split("\\s+").length;

        return new DocumentationResult(title, type, format, contentStr, wordCount, sections);
    }

    @Override
    public DiagramResult generateDiagram(DiagramRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String type = request.diagramType();
        String title = request.title();
        List<String> nodes = request.nodes();
        List<String> connections = request.connections();

        StringBuilder sb = new StringBuilder();
        sb.append(type).append("\n");

        if (nodes.isEmpty()) {
            sb.append("    Client[\"Browser / Frontend UI\"] --> API[\"Spring Boot MVC Backend\"]\n")
                    .append("    API --> DB[(\"PostgreSQL Database\")]\n")
                    .append("    API --> AI[\"Spring AI Orchestrator\"]\n");
        } else {
            for (int i = 0; i < nodes.size(); i++) {
                String node = nodes.get(i);
                sb.append("    N").append(i).append("[\"").append(node).append("\"]\n");
            }
            if (connections.isEmpty() && nodes.size() > 1) {
                for (int i = 0; i < nodes.size() - 1; i++) {
                    sb.append("    N").append(i).append(" --> N").append(i + 1).append("\n");
                }
            } else {
                for (String conn : connections) {
                    sb.append("    ").append(conn).append("\n");
                }
            }
        }

        return new DiagramResult(title, type, sb.toString().stripTrailing());
    }
}
