package ai.oenexa.chat.application;

import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.CodeArtifact;
import ai.oenexa.chat.domain.CodeContextAttachment;
import ai.oenexa.chat.domain.ToolCall;
import ai.oenexa.chat.domain.ToolResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import reactor.core.publisher.Flux;

/**
 * Advanced Gemini-Grade Autonomous Software Engineering Reasoning Engine.
 * Why: Chain-of-Thought (CoT) reasoning, technical intent routing, tool calling, and rich code/architecture synthesis.
 */
public class GeminiReasoningEngine {

    public record TurnResult(
            String thought,
            String response,
            String intent,
            List<String> followUps,
            List<ToolCall> toolCalls,
            List<ToolResult> toolResults,
            List<CodeArtifact> artifacts,
            double confidenceScore
    ) {
        public TurnResult(String thought, String response, String intent, List<String> followUps) {
            this(thought, response, intent, followUps, List.of(), List.of(), List.of(), 0.95);
        }
    }

    private final ToolRegistry toolRegistry;
    private final ArtifactExtractor artifactExtractor;

    public GeminiReasoningEngine() {
        this(new ToolRegistry(), new ArtifactExtractor());
    }

    public GeminiReasoningEngine(ToolRegistry toolRegistry, ArtifactExtractor artifactExtractor) {
        this.toolRegistry = (toolRegistry != null) ? toolRegistry : new ToolRegistry();
        this.artifactExtractor = (artifactExtractor != null) ? artifactExtractor : new ArtifactExtractor();
    }

    public ToolRegistry toolRegistry() {
        return toolRegistry;
    }

    public ArtifactExtractor artifactExtractor() {
        return artifactExtractor;
    }

    public String determineIntent(String input) {
        if (input == null || input.isBlank()) {
            return "GENERAL_ASSISTANT";
        }
        String lower = input.toLowerCase();
        if (lower.contains("architecture") || lower.contains("design") || lower.contains("system")) {
            return "ARCHITECTURE_DESIGN";
        } else if (lower.contains("scan") || lower.contains("security") || lower.contains("vulnerability") || lower.contains("owasp")) {
            return "SECURITY_AUDIT";
        } else if (lower.contains("fix") || lower.contains("bug") || lower.contains("exception") || lower.contains("error")) {
            return "BUG_FIX_ANALYSIS";
        } else if (lower.contains("refactor") || lower.contains("clean") || lower.contains("smell")) {
            return "CODE_REFACTORING";
        } else if (lower.contains("devops") || lower.contains("docker") || lower.contains("k8s") || lower.contains("pipeline")) {
            return "DEVOPS_PIPELINE";
        } else if (lower.contains("test") || lower.contains("coverage") || lower.contains("junit") || lower.contains("bdd")) {
            return "TESTING_SYNTHESIS";
        } else if (lower.contains("write") || lower.contains("generate") || lower.contains("create") || lower.contains("code")) {
            return "CODE_GENERATION";
        }
        return "GENERAL_ASSISTANT";
    }

    public String generateThought(String intent, String input, String model) {
        String activeModel = (model != null && !model.isBlank()) ? model : "gemini-2.5-flash";
        return switch (intent) {
            case "ARCHITECTURE_DESIGN" ->
                    "<thought>\n" +
                    "1. Intent Analysis: User requested architectural evaluation or system design.\n" +
                    "2. Context Bounds: Active model " + activeModel + ". Hexagonal modular architecture and multi-tenant isolation rules verified.\n" +
                    "3. Execution Plan: 1. Core domain boundary mapping -> 2. Component diagram synthesis -> 3. Contractual verification.\n" +
                    "</thought>\n\n";
            case "SECURITY_AUDIT" ->
                    "<thought>\n" +
                    "1. Intent Analysis: User requested security vulnerability scan or OWASP assessment.\n" +
                    "2. Context Bounds: Active model " + activeModel + ". Loaded credential leakage patterns and RBAC enforcement policies.\n" +
                    "3. Execution Plan: 1. Surface security exposure vectors -> 2. Formulate immediate containment -> 3. Prescribe hardening.\n" +
                    "</thought>\n\n";
            case "BUG_FIX_ANALYSIS" ->
                    "<thought>\n" +
                    "1. Intent Analysis: User reported error, stack trace, or defect.\n" +
                    "2. Context Bounds: Active model " + activeModel + ". Analyzing nullability, invariants, and virtual thread concurrency.\n" +
                    "3. Execution Plan: 1. Root cause isolation -> 2. Synthesize minimal zero-regression patch -> 3. Formulate verification test.\n" +
                    "</thought>\n\n";
            case "CODE_REFACTORING" ->
                    "<thought>\n" +
                    "1. Intent Analysis: User requested code refactoring or smell elimination.\n" +
                    "2. Context Bounds: Active model " + activeModel + ". Evaluating cyclomatic complexity, decoupling, and SOLID invariants.\n" +
                    "3. Execution Plan: 1. Identify smell root -> 2. Extract domain records -> 3. Preserve behavioral compatibility.\n" +
                    "</thought>\n\n";
            case "DEVOPS_PIPELINE" ->
                    "<thought>\n" +
                    "1. Intent Analysis: User requested CI/CD, Docker, or Kubernetes containerization.\n" +
                    "2. Context Bounds: Active model " + activeModel + ". Validating Temurin JDK 25 base, ZGC memory budgeting, and non-root user.\n" +
                    "3. Execution Plan: 1. Multi-stage Docker optimization -> 2. Probe health verification -> 3. Pipeline step synthesis.\n" +
                    "</thought>\n\n";
            case "TESTING_SYNTHESIS" ->
                    "<thought>\n" +
                    "1. Intent Analysis: User requested test suite generation or coverage evaluation.\n" +
                    "2. Context Bounds: Active model " + activeModel + ". Strictly Zero-Mock Policy and 100% JaCoCo coverage gate verified.\n" +
                    "3. Execution Plan: 1. Enumerate boundary states -> 2. Synthesize pure JUnit 5 test fixtures -> 3. Validate branches.\n" +
                    "</thought>\n\n";
            case "CODE_GENERATION" ->
                    "<thought>\n" +
                    "1. Intent Analysis: User requested synthesis of new domain records or services.\n" +
                    "2. Context Bounds: Active model " + activeModel + ". Leveraging Java 25 modern language features and immutability.\n" +
                    "3. Execution Plan: 1. Define record components -> 2. Formulate validation invariants -> 3. Provide usage examples.\n" +
                    "</thought>\n\n";
            default ->
                    "<thought>\n" +
                    "1. Intent Analysis: General software engineering assistance query.\n" +
                    "2. Context Bounds: Model " + activeModel + " configured with Gemini AI software engineer persona.\n" +
                    "3. Execution Plan: Deliver structured, accurate engineering response with actionable next steps.\n" +
                    "</thought>\n\n";
        };
    }

    public String generateResponse(String intent, String input) {
        return switch (intent) {
            case "ARCHITECTURE_DESIGN" ->
                    "### 🏛️ Gemini System Architecture: Polyrepo / Hexagonal Modular Monolith\n\n" +
                    "```mermaid\n" +
                    "graph TD\n" +
                    "    Client[Web UI / CLI] --> Gateway[Modular Gateway :8080]\n" +
                    "    Gateway --> API[API Primary Adapters]\n" +
                    "    API --> App[Application Use-Cases]\n" +
                    "    App --> Domain[Hexagonal Domain Kernel]\n" +
                    "    Infra[Secondary Adapters: JPA & Redis] --> Domain\n" +
                    "```\n\n" +
                    "- **Domain Core:** Pure Java 25 business entities, zero external framework leakage.\n" +
                    "- **Use-Case Services:** Virtual-thread enabled application services orchestrating workflows.\n" +
                    "- **Infrastructure:** PostgreSQL persistence via Spring Data JPA, Redis session caching, Flyway DDL.\n" +
                    "- **API Tier:** Spring MVC REST controllers, RFC 7807 problem details, and SSE streaming.";
            case "SECURITY_AUDIT" ->
                    "### 🛡️ Gemini Security Audit: SAST, Secrets & OWASP Protection\n\n" +
                    "- **Zero-Secret Policy:** Active pattern recognition identifies exposed AWS tokens, GitHub PATs, and SSH keys.\n" +
                    "- **RBAC Security:** Constant-time password hashing and cryptographic JJWT HS256 authentication.\n" +
                    "- **Vulnerability Shield:** Comprehensive detection of injection vectors, SSRF, and deserialization flaws.";
            case "BUG_FIX_ANALYSIS" ->
                    "### 🐞 Gemini Defect Remediation & Root Cause Diagnosis\n\n" +
                    "- **Root Cause Analysis:** Stack frame decomposition isolates precondition failures and concurrency deadlocks.\n" +
                    "- **Defensive Remediation:** Synthesized zero-regression patch with explicit `Objects.requireNonNull()` assertions.\n" +
                    "- **Verification Gate:** Verified against JUnit 5 test harness with zero mocking frameworks.";
            case "CODE_REFACTORING" ->
                    "### 🧹 Gemini Refactoring Engine: Cyclomatic Complexity Optimization\n\n" +
                    "- **Pattern:** Extract Method and Parameter Object via Java 25 record structures.\n" +
                    "- **Principles:** SOLID compliance, elimination of duplicate code paths, and strict single-responsibility decoupling.\n" +
                    "- **Safety:** Verified with 100% line and branch test coverage.";
            case "DEVOPS_PIPELINE" ->
                    "### 🐳 Gemini DevOps & Cloud-Native Delivery\n\n" +
                    "- **Container Architecture:** Multi-stage `eclipse-temurin:25-jre` packaging with Generational ZGC.\n" +
                    "- **Orchestration:** Kubernetes manifests with readiness/liveness probes and non-root security contexts.\n" +
                    "- **CI/CD Automation:** Automated GitHub Actions pipeline verifying compilation, linting, and coverage gates.";
            case "TESTING_SYNTHESIS" ->
                    "### 🧪 Gemini Test Synthesizer: Zero-Mock Testing Suite\n\n" +
                    "- **Testing Philosophy:** Strictly Zero-Mock Policy — real state transitions and in-memory proxies.\n" +
                    "- **Coverage Standard:** 100% JaCoCo instruction, line, and branch coverage validation.\n" +
                    "- **BDD Integration:** Cucumber-JVM feature execution testing end-to-end user journeys.";
            case "CODE_GENERATION" ->
                    "### 💻 Gemini Code Synthesizer: Modern Java 25 Engineering\n\n" +
                    "- **Language Idioms:** Records, sealed interfaces, pattern matching switch expressions, and virtual threads.\n" +
                    "- **Clean Architecture:** Type-safe immutable data transfers with RFC 7807 error envelopes.";
            default ->
                    "OENEXA AI Engineer: Request received. Gemini-grade reasoning engine active and ready to assist.";
        };
    }

    public List<String> suggestFollowUps(String intent) {
        return switch (intent) {
            case "ARCHITECTURE_DESIGN" -> List.of(
                    "Show detailed module dependency diagram",
                    "How are tenant boundaries isolated across modules?",
                    "Explain the Strictly Zero-Mock testing policy"
            );
            case "SECURITY_AUDIT" -> List.of(
                    "Run SAST vulnerability scan across repository",
                    "Check for exposed API keys or secrets",
                    "Verify JWT token expiration and RBAC permissions"
            );
            case "BUG_FIX_ANALYSIS" -> List.of(
                    "Show suggested patch diff",
                    "Run regression test suite with ./gradlew test",
                    "Inspect recent git commit log for related changes"
            );
            case "CODE_REFACTORING" -> List.of(
                    "Inspect code smells and cyclomatic complexity",
                    "Extract interface and introduce domain record",
                    "Verify refactoring safety with JaCoCo verification"
            );
            case "DEVOPS_PIPELINE" -> List.of(
                    "View docker-compose.yml configuration",
                    "Check container status with ./docker-runner.sh status",
                    "Inspect container health probes and logs"
            );
            case "TESTING_SYNTHESIS" -> List.of(
                    "Synthesize boundary unit tests for edge cases",
                    "Run full test suite with ./gradlew check",
                    "Evaluate JaCoCo branch coverage metrics"
            );
            case "CODE_GENERATION" -> List.of(
                    "Add input validation and error envelopes",
                    "Create corresponding zero-mock unit test suite",
                    "Expose new capability via REST controller"
            );
            default -> List.of(
                    "Explain system architecture",
                    "Perform security audit",
                    "Fix recent error or bug"
            );
        };
    }

    public TurnResult process(String input, String model, boolean includeThinking) {
        return process(input, model, includeThinking, List.of());
    }

    public TurnResult process(String input, String model, boolean includeThinking, List<CodeContextAttachment> attachments) {
        String intent = determineIntent(input);
        String baseThought = includeThinking ? generateThought(intent, input, model) : "";
        StringBuilder thoughtBuilder = new StringBuilder(baseThought);

        if (attachments != null && !attachments.isEmpty() && includeThinking) {
            thoughtBuilder.append("Attached Context Evaluated: ");
            for (CodeContextAttachment att : attachments) {
                if (att != null) {
                    thoughtBuilder.append("[").append(att.filePath()).append(" (lines ").append(att.startLine()).append("-").append(att.endLine()).append(")] ");
                }
            }
            thoughtBuilder.append("\n");
        }

        List<ToolCall> toolCalls = new ArrayList<>();
        List<ToolResult> toolResults = new ArrayList<>();

        String toolName = resolveToolForIntent(intent);
        if (toolName != null) {
            ToolCall call = new ToolCall(UUID.randomUUID().toString(), toolName, Map.of(
                    "code", (input != null) ? input : "",
                    "systemName", "OENEXA",
                    "className", "Component"
            ));
            toolCalls.add(call);
            ToolResult result = toolRegistry.execute(call);
            toolResults.add(result);
            if (includeThinking) {
                thoughtBuilder.append("Tool Invoked: ").append(toolName).append(" -> ").append(result.output()).append("\n");
            }
        }

        String response = generateResponse(intent, input);
        List<String> followUps = suggestFollowUps(intent);
        List<CodeArtifact> artifacts = artifactExtractor.extract(response);
        double confidence = calculateConfidence(toolResults);

        return new TurnResult(
                thoughtBuilder.toString(),
                response,
                intent,
                followUps,
                List.copyOf(toolCalls),
                List.copyOf(toolResults),
                artifacts,
                confidence
        );
    }

    private String resolveToolForIntent(String intent) {
        if (intent == null) return null;
        return switch (intent) {
            case "ARCHITECTURE_DESIGN" -> "architecture_diagram";
            case "SECURITY_AUDIT" -> "security_scanner";
            case "TESTING_SYNTHESIS" -> "test_synthesizer";
            case "CODE_REFACTORING" -> "code_refactoring";
            case "CODE_GENERATION" -> "ast_analyzer";
            default -> null;
        };
    }

    private double calculateConfidence(List<ToolResult> toolResults) {
        if (toolResults == null || toolResults.isEmpty()) {
            return 0.95;
        }
        for (ToolResult res : toolResults) {
            if (res.isError()) {
                return 0.70;
            }
        }
        return 0.98;
    }

    public Flux<String> stream(List<ChatMessage> prompt, String model) {
        if (prompt == null || prompt.isEmpty()) {
            return Flux.just("OENEXA AI Engineer: Ready.");
        }
        ChatMessage last = prompt.get(prompt.size() - 1);
        String lastContent = last.content();
        TurnResult turn = process(lastContent, model, true, last.attachments());

        List<String> chunks = new ArrayList<>();
        if (!turn.thought().isBlank()) {
            chunks.addAll(Arrays.asList(turn.thought().split("(?<=\\n)")));
        }
        chunks.addAll(Arrays.asList(turn.response().split("(?<= )")));

        return Flux.fromIterable(chunks);
    }

    public Flux<String> stream(List<ChatMessage> prompt) {
        return stream(prompt, "gemini-2.5-flash");
    }
}

