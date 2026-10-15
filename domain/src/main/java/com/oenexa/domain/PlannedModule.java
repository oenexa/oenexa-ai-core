package com.oenexa.domain;

import java.util.List;

/**
 * Standard definitions for all 20 OENEXA enterprise platform modules.
 */
public record PlannedModule(String id, String displayName, String description,
                            List<String> capabilities, ModuleStatus status) implements PlatformModule {

    public static List<PlatformModule> allModules() {
        return List.of(
                new PlannedModule("auth", "Authentication & RBAC", "JWT issuing, validation, roles & permissions",
                        List.of("login", "register", "token-refresh", "rbac"), ModuleStatus.ACTIVE),
                new PlannedModule("agent", "Multi-Agent Coordinator", "15 autonomous agents, 4 ordered workflows, delegation",
                        List.of("workflow-run", "agent-delegation", "timeline"), ModuleStatus.ACTIVE),
                new PlannedModule("chat", "AI Assistant Chat", "Context window management, conversational reasoning",
                        List.of("chat-completions", "context-window"), ModuleStatus.ACTIVE),
                new PlannedModule("rag", "RAG Knowledge Base", "Semantic indexing, chunking, and similarity search",
                        List.of("document-ingest", "vector-search"), ModuleStatus.ACTIVE),
                new PlannedModule("gateway", "API Gateway", "Route dispatching, CORS, rate limiting",
                        List.of("routing", "cors", "filtering"), ModuleStatus.ACTIVE),
                new PlannedModule("core", "Core Domain Kernel", "Shared value objects, invariants, base types",
                        List.of("domain-events", "records"), ModuleStatus.ACTIVE),
                new PlannedModule("common", "Common Utilities", "Cross-cutting error envelopes, logging, tenants",
                        List.of("rfc7807-errors", "tenant-context"), ModuleStatus.ACTIVE),
                new PlannedModule("code", "Code Generation Engine", "Monaco editor integration, AST analysis, refactoring",
                        List.of("ast-parse", "code-gen"), ModuleStatus.ACTIVE),
                new PlannedModule("repository", "Repository Service", "Git repo synchronization, branch indexing, tree analysis",
                        List.of("git-clone", "branch-inspect"), ModuleStatus.ACTIVE),
                new PlannedModule("review", "AI Code Review", "Automated PR reviews, quality scores, risk assessments",
                        List.of("pr-review", "quality-score"), ModuleStatus.ACTIVE),
                new PlannedModule("security", "SAST & Security Audit", "OWASP Top 10 scanning, CVE checks, secrets detection",
                        List.of("vuln-scan", "cve-audit"), ModuleStatus.ACTIVE),
                new PlannedModule("testing", "Testing & Verification", "BDD/TDD test generation, JaCoCo coverage gates",
                        List.of("bdd-gen", "coverage-gate"), ModuleStatus.ACTIVE),
                new PlannedModule("devops", "DevOps & CI/CD", "Dockerfile generation, pipeline linting, container builds",
                        List.of("docker-gen", "ci-lint"), ModuleStatus.ACTIVE),
                new PlannedModule("pipeline", "Pipeline Orchestration", "Async task execution, stage sequencing, DAG runs",
                        List.of("dag-run", "task-queue"), ModuleStatus.ACTIVE),
                new PlannedModule("observability", "Observability & SRE", "OpenTelemetry tracing, Prometheus metrics, health checks",
                        List.of("otel-trace", "metrics"), ModuleStatus.ACTIVE),
                new PlannedModule("search", "Enterprise Search Engine", "Full-text indexing, code search, semantic filters",
                        List.of("code-search", "text-index"), ModuleStatus.ACTIVE),
                new PlannedModule("documentation", "Documentation Engine", "Markdown generation, API specs, architectural diagrams",
                        List.of("doc-gen", "diagram-export"), ModuleStatus.ACTIVE),
                new PlannedModule("workspace", "Workspace Management", "Project isolation, polyrepo sync, developer sandboxes",
                        List.of("workspace-env", "sandbox"), ModuleStatus.ACTIVE),
                new PlannedModule("admin", "Platform Administration", "Tenant onboarding, user provisioning, system audits",
                        List.of("tenant-admin", "audit-log"), ModuleStatus.ACTIVE),
                new PlannedModule("ui", "Web Frontend Suite", "React 18, Vite 5, Tailwind CSS, Monaco Editor",
                        List.of("web-ui", "monaco-editor"), ModuleStatus.ACTIVE)
        );
    }
}
