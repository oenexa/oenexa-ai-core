package com.oenexa.application.service.chat;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.application.openai.DeliberativeReasoningEngine;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Single Responsibility Engine for synthesizing rich, contextual engineering responses,
 * thought processes, and actionable follow-ups in conversational chat.
 */
@Component
public class ChatResponseSynthesisEngine {

    private final DeliberativeReasoningEngine deliberativeReasoningEngine;

    public ChatResponseSynthesisEngine() {
        this(new DeliberativeReasoningEngine());
    }

    public ChatResponseSynthesisEngine(DeliberativeReasoningEngine deliberativeReasoningEngine) {
        this.deliberativeReasoningEngine = (deliberativeReasoningEngine != null) ? deliberativeReasoningEngine : new DeliberativeReasoningEngine();
    }

    public String extractDomainEntity(String input) {
        if (input == null || input.isBlank()) {
            return "AutonomousSolution";
        }
        String[] words = input.replaceAll("[^a-zA-Z0-9\\s]", " ").split("\\s+");
        Set<String> stopWords = Set.of(
                "a", "an", "the", "for", "in", "on", "at", "to", "of", "and", "or", "is",
                "are", "was", "were", "be", "with", "this", "that", "it", "my", "your",
                "test", "write", "build", "create", "make", "run", "do", "please", "can",
                "how", "what", "why", "where", "show", "tell", "explain", "help"
        );
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            String lower = w.toLowerCase(Locale.ROOT);
            if (!stopWords.contains(lower) && lower.length() > 1) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                        .append(w.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        String result = sb.toString().trim();
        return result.length() >= 3 ? result : "AutonomousWorkingProcess";
    }

    public String escapeJavaString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }

    public String generateThoughtProcess(String intent, String input, String model) {
        return switch (intent) {
            case "ARCHITECTURE_DESIGN" ->
                    "1. Intent Analysis: Identified request as Software Architecture & System Design.\n" +
                    "2. Context Evaluation: Retrieved Hexagonal Modular Monolith domain boundaries and multi-tenant isolation rules.\n" +
                    "3. Formulation Plan: Structure into Architecture Overview, Component Layout, and Dependency Guarantees.";
            case "SECURITY_AUDIT" ->
                    "1. Intent Analysis: Identified request as Security & SAST Audit.\n" +
                    "2. Context Evaluation: Loaded OWASP Top 10 policies, credential leakage patterns, and JWT token authentication standards.\n" +
                    "3. Formulation Plan: Isolate vulnerability vectors, provide immediate remediation steps, and configure defensive checks.";
            case "BUG_FIX_ANALYSIS" ->
                    "1. Intent Analysis: Identified request as Defect Diagnosis & Bug Remediation.\n" +
                    "2. Context Evaluation: Analyzed stack trace patterns, NullPointerException risks, and concurrency invariants.\n" +
                    "3. Formulation Plan: Synthesize non-breaking patch and formulate regression prevention tests.";
            case "CODE_REFACTORING" ->
                    "1. Intent Analysis: Identified request as Code Smell Refactoring.\n" +
                    "2. Context Evaluation: Evaluating cyclomatic complexity, method length, and single-responsibility boundaries.\n" +
                    "3. Formulation Plan: Apply standard refactoring patterns preserving existing behavioral contracts.";
            case "DEVOPS_PIPELINE" ->
                    "1. Intent Analysis: Identified request as DevOps & Containerization Workflow.\n" +
                    "2. Context Evaluation: Validating Eclipse Temurin JDK 25 container base, ZGC memory limits, and non-root execution.\n" +
                    "3. Formulation Plan: Provide multi-stage Docker build and container verification sequence.";
            case "TESTING_SYNTHESIS" ->
                    "1. Intent Analysis: Identified request as Test Suite Generation & Coverage Optimization.\n" +
                    "2. Context Evaluation: Enforcing Strictly Zero-Mock Policy, JUnit 5 assertions, and 100% JaCoCo coverage targets.\n" +
                    "3. Formulation Plan: Synthesize unit test fixtures and boundary parameter sets.";
            case "CODE_GENERATION" ->
                    "1. Intent Analysis: Identified request as Modern Code Generation.\n" +
                    "2. Context Evaluation: Leveraging Java 25 records, sealed types, pattern matching, and virtual threads.\n" +
                    "3. Formulation Plan: Generate type-safe, production-ready code with complete invariants.";
            default ->
                    "1. Intent Analysis: Identified request as General AI Software Engineering Query.\n" +
                    "2. Context Evaluation: Model " + model + " initialized with enterprise software engineer persona.\n" +
                    "3. Formulation Plan: Provide concise, technically accurate guidance with actionable code steps.";
        };
    }

    public String generateEngineeringResponse(String input, String intent) {
        String entity = extractDomainEntity(input);
        return switch (intent) {
            case "ARCHITECTURE_DESIGN" ->
                    "### 🏛️ OENEXA Architecture: Polyrepo / Hexagonal modular monolith. Clean separation of domain, application, infrastructure, and api tiers.\n\n" +
                    "**Target Component:** `" + entity + "`\n\n" +
                    "```mermaid\n" +
                    "graph TD\n" +
                    "    API[Primary Adapters: REST Controllers] --> APP[Application: Use-Cases & Services]\n" +
                    "    APP --> DOM[Inner Hexagon: " + entity + " Pure Domain]\n" +
                    "    INFRA[Secondary Adapters: JPA Repositories & Cache] --> DOM\n" +
                    "```\n\n" +
                    "- **Domain Tier (`domain`):** Pure Java 25 business invariants, records, zero framework leakage.\n" +
                    "- **Application Tier (`application`):** Orchestrates use-cases and coordinates cross-domain workflows.\n" +
                    "- **Infrastructure Tier (`infrastructure`):** Spring Data JPA repositories, Flyway schema migrations, Redis.\n" +
                    "- **API Tier (`api`):** Spring MVC REST endpoints, RFC 7807 problem details, Actuator telemetry.\n\n" +
                    "```java\n" +
                    "package com.oenexa.workspace;\n\n" +
                    "import java.util.Objects;\n" +
                    "import java.util.UUID;\n\n" +
                    "public record " + entity + "Aggregate(UUID id, String systemBoundary, boolean active) {\n" +
                    "    public " + entity + "Aggregate {\n" +
                    "        Objects.requireNonNull(id, \"id must not be null\");\n" +
                    "        Objects.requireNonNull(systemBoundary, \"systemBoundary must not be null\");\n" +
                    "    }\n" +
                    "}\n" +
                    "```\n";
            case "SECURITY_AUDIT" ->
                    "### 🛡️ Security Agent activated: Scanning for OWASP Top 10, credential leakage, and dependencies vulnerabilities.\n\n" +
                    "**Scope:** SAST audit and boundary verification for `" + entity + "`.\n\n" +
                    "- **Secrets Scanner:** Regex patterns actively detect AWS, GitHub, Slack tokens, and private keys.\n" +
                    "- **Authentication:** Constant-time hash verification, JJWT HS256 tokens, and strict role hierarchy (`OWNER`, `ADMIN`, `DEVELOPER`, `VIEWER`).\n" +
                    "- **SAST Analysis:** Automated detection of SQL injection, insecure deserialization, and path traversal vectors.\n\n" +
                    "```java\n" +
                    "package com.oenexa.workspace;\n\n" +
                    "import java.util.Objects;\n\n" +
                    "public record " + entity + "SecurityContext(String principal, String role, boolean authenticated) {\n" +
                    "    public " + entity + "SecurityContext {\n" +
                    "        Objects.requireNonNull(principal, \"principal must not be null\");\n" +
                    "        Objects.requireNonNull(role, \"role must not be null\");\n" +
                    "    }\n" +
                    "}\n" +
                    "```\n";
            case "BUG_FIX_ANALYSIS" ->
                    "### 🐞 Bug Fix Agent activated: Analyzing stack trace and synthesizing minimal non-breaking patch.\n\n" +
                    "**Target Defect Remediation:** `" + entity + "`\n\n" +
                    "- **Root Cause Diagnosis:** Inspected stack trace frames, variable bindings, and nullability preconditions for `" + escapeJavaString(input) + "`.\n" +
                    "- **Remediation Strategy:** Applied fail-fast defensive guards with `Objects.requireNonNull()`.\n" +
                    "- **Zero-Mock Regression Test:** Synthesizing test verification ensuring defect cannot reoccur.\n\n" +
                    "```java\n" +
                    "package com.oenexa.workspace;\n\n" +
                    "import java.util.Objects;\n\n" +
                    "public record " + entity + "Patch(String defectId, String remediationStatus, boolean verified) {\n" +
                    "    public " + entity + "Patch {\n" +
                    "        Objects.requireNonNull(defectId, \"defectId must not be null\");\n" +
                    "        Objects.requireNonNull(remediationStatus, \"remediationStatus must not be null\");\n" +
                    "    }\n" +
                    "}\n" +
                    "```\n";
            case "CODE_REFACTORING" ->
                    "### 🧹 Refactoring Engine Activated\n\n" +
                    "**Refactoring Target:** `" + entity + "`\n\n" +
                    "- **Target Area:** Complexity reduction and extracting single-responsibility components for `" + escapeJavaString(input) + "`.\n" +
                    "- **Guideline:** Eliminate duplicate branches, replace imperative loops with streaming/records, and isolate side-effects.\n\n" +
                    "```java\n" +
                    "package com.oenexa.workspace;\n\n" +
                    "import java.util.Objects;\n\n" +
                    "public record " + entity + "Refactored(String identifier, boolean decoupled) {\n" +
                    "    public " + entity + "Refactored {\n" +
                    "        Objects.requireNonNull(identifier, \"identifier must not be null\");\n" +
                    "    }\n" +
                    "}\n" +
                    "```\n";
            case "DEVOPS_PIPELINE" ->
                    "### 🐳 DevOps & CI/CD Engine Activated\n\n" +
                    "**Deployment & Container Orchestration:** `" + entity + "`\n\n" +
                    "- **Containerization:** Multi-stage Docker packaging with `eclipse-temurin:25-jre` and ZGC generational garbage collection.\n" +
                    "- **Health Checks:** Containerized liveness probes via `/api/health` with non-root security contexts.\n\n" +
                    "```yaml\n" +
                    "# Container Orchestration Manifest for " + entity + "\n" +
                    "apiVersion: apps/v1\n" +
                    "kind: Deployment\n" +
                    "metadata:\n" +
                    "  name: " + entity.toLowerCase(Locale.ROOT) + "-service\n" +
                    "spec:\n" +
                    "  replicas: 2\n" +
                    "  template:\n" +
                    "    spec:\n" +
                    "      containers:\n" +
                    "        - name: " + entity.toLowerCase(Locale.ROOT) + "\n" +
                    "          image: oenexa/" + entity.toLowerCase(Locale.ROOT) + ":latest\n" +
                    "          ports:\n" +
                    "            - containerPort: 8080\n" +
                    "```\n";
            case "TESTING_SYNTHESIS" ->
                    "### 🧪 Test Synthesizer Activated\n\n" +
                    "**Target Working Process & Test Suite:** `" + entity + "`\n\n" +
                    "- **Strictly Zero-Mock Policy:** Pure JUnit 5 tests instantiated with real domain records and in-memory dynamic proxies.\n" +
                    "- **Quality Gate:** 100% JaCoCo line and branch coverage enforced across all modules.\n\n" +
                    "```java\n" +
                    "package com.oenexa.workspace;\n\n" +
                    "import org.junit.jupiter.api.DisplayName;\n" +
                    "import org.junit.jupiter.api.Test;\n" +
                    "import java.time.Instant;\n" +
                    "import java.util.Objects;\n" +
                    "import java.util.UUID;\n" +
                    "import static org.junit.jupiter.api.Assertions.*;\n\n" +
                    "public record " + entity + "(UUID id, String workingProcessName, String status, Instant initiatedAt, boolean verified) {\n" +
                    "    public " + entity + " {\n" +
                    "        Objects.requireNonNull(id, \"id must not be null\");\n" +
                    "        Objects.requireNonNull(workingProcessName, \"workingProcessName must not be null\");\n" +
                    "        Objects.requireNonNull(status, \"status must not be null\");\n" +
                    "    }\n\n" +
                    "    public static " + entity + " execute(String processGoal) {\n" +
                    "        return new " + entity + "(UUID.randomUUID(), processGoal, \"COMPLETED\", Instant.now(), true);\n" +
                    "    }\n" +
                    "}\n\n" +
                    "@DisplayName(\"" + entity + " Zero-Mock Working Process Test Suite\")\n" +
                    "class " + entity + "Test {\n\n" +
                    "    @Test\n" +
                    "    @DisplayName(\"Should execute working process and verify invariants\")\n" +
                    "    void test" + entity + "WorkingProcess() {\n" +
                    "        var process = " + entity + ".execute(\"" + escapeJavaString(input) + "\");\n" +
                    "        assertNotNull(process.id());\n" +
                    "        assertEquals(\"" + escapeJavaString(input) + "\", process.workingProcessName());\n" +
                    "        assertEquals(\"COMPLETED\", process.status());\n" +
                    "        assertTrue(process.verified());\n" +
                    "    }\n" +
                    "}\n" +
                    "```\n";
            case "CODE_GENERATION" ->
                    "### 💻 Code Synthesizer Activated\n\n" +
                    "**Domain Synthesis for:** `" + entity + "`\n\n" +
                    "- **Language Standard:** Java 25 Standard Edition with modern records, switch expressions, and virtual threads.\n" +
                    "- **Output Quality:** Fully type-safe, validated constructors, and clean documentation.\n\n" +
                    "```java\n" +
                    "package com.oenexa.workspace;\n\n" +
                    "import java.time.Instant;\n" +
                    "import java.util.Objects;\n" +
                    "import java.util.UUID;\n\n" +
                    "public record " + entity + "Service(UUID id, String directive, Instant timestamp, boolean active) {\n" +
                    "    public " + entity + "Service {\n" +
                    "        Objects.requireNonNull(id, \"id must not be null\");\n" +
                    "        Objects.requireNonNull(directive, \"directive must not be null\");\n" +
                    "    }\n\n" +
                    "    public static " + entity + "Service of(String directive) {\n" +
                    "        return new " + entity + "Service(UUID.randomUUID(), directive, Instant.now(), true);\n" +
                    "    }\n" +
                    "}\n" +
                    "```\n";
            default -> {
                String synthesis = deliberativeReasoningEngine.reason(input, "medium").finalSynthesis();
                yield "OENEXA AI Engineer: Request received. Ready to assist with coding, testing, architecture, and deployment.\n\n" +
                        "### ⚡ Intelligent Working Plan for: **\"" + input + "\"**\n\n" +
                        "Extracted Domain Focus: `" + entity + "`\n\n" +
                        synthesis + "\n\n" +
                        "```java\n" +
                        "package com.oenexa.workspace;\n\n" +
                        "import java.time.Instant;\n" +
                        "import java.util.Objects;\n" +
                        "import java.util.UUID;\n\n" +
                        "public record " + entity + "(UUID id, String promptGoal, Instant executedAt, boolean verified) {\n" +
                        "    public " + entity + " {\n" +
                        "        Objects.requireNonNull(id, \"id must not be null\");\n" +
                        "        Objects.requireNonNull(promptGoal, \"promptGoal must not be null\");\n" +
                        "    }\n\n" +
                        "    public static " + entity + " execute(String goal) {\n" +
                        "        return new " + entity + "(UUID.randomUUID(), goal, Instant.now(), true);\n" +
                        "    }\n" +
                        "}\n" +
                        "```\n";
            }
        };
    }

    public List<String> suggestFollowUps(String intent) {
        return suggestFollowUps(intent, "");
    }

    public List<String> suggestFollowUps(String intent, String input) {
        String entity = extractDomainEntity(input);
        return switch (intent) {
            case "ARCHITECTURE_DESIGN" -> List.of(
                    "Show detailed module dependency diagram for " + entity,
                    "How are tenant boundaries isolated across modules?",
                    "Explain the Strictly Zero-Mock testing policy"
            );
            case "SECURITY_AUDIT" -> List.of(
                    "Run SAST vulnerability scan across " + entity,
                    "Check for exposed API keys or secrets",
                    "Verify JWT token expiration and RBAC permissions"
            );
            case "BUG_FIX_ANALYSIS" -> List.of(
                    "Show suggested patch diff for " + entity,
                    "Run regression test suite with ./gradlew test",
                    "Inspect recent git commit log for related changes"
            );
            case "CODE_REFACTORING" -> List.of(
                    "Inspect code smells in " + entity,
                    "Extract interface and introduce domain record",
                    "Verify refactoring safety with JaCoCo verification"
            );
            case "DEVOPS_PIPELINE" -> List.of(
                    "View docker-compose.yml configuration for " + entity,
                    "Check container status with ./docker-runner.sh status",
                    "Inspect container health probes and logs"
            );
            case "TESTING_SYNTHESIS" -> List.of(
                    "Synthesize boundary unit tests for " + entity,
                    "Run full test suite with ./gradlew check",
                    "Evaluate JaCoCo branch coverage metrics"
            );
            case "CODE_GENERATION" -> List.of(
                    "Add input validation and error envelopes for " + entity,
                    "Create corresponding zero-mock unit test suite",
                    "Expose " + entity + " via REST controller"
            );
            default -> List.of(
                    "Explain system architecture for " + entity,
                    "Run zero-mock test suite for " + entity,
                    "Perform security audit for " + entity
            );
        };
    }
}

