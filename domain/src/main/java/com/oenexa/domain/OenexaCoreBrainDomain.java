package com.oenexa.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Enterprise Domain Records & Invariants for OENEXA-AI-CORE Central Brain.
 * Encapsulates the 12 core engines, unified brain directives, security assessments,
 * performance telemetry, and observability contracts.
 */
public final class OenexaCoreBrainDomain {

    private OenexaCoreBrainDomain() {}

    public enum CoreModuleType {
        AI_GATEWAY("AI Gateway", "Central entry point, request routing, and model selection"),
        MODEL_ROUTER("Model Router", "Dynamic model selection, local/cloud routing, and fallback handling"),
        REASONING_ENGINE("Reasoning Engine", "Multi-step reasoning, task decomposition, and decision workflows"),
        AGENT_ENGINE("Agent Engine", "Single/multi-agent collaboration, task execution, and goal management"),
        MEMORY_ENGINE("Memory Engine", "7-layer memory hierarchy, context management, and SSD compression"),
        KNOWLEDGE_ENGINE("Knowledge Engine", "Enterprise RAG, document processing, and knowledge graph"),
        TOOL_EXECUTION_ENGINE("Tool Execution Engine", "API calling, database, file system, and MCP execution"),
        SELF_IMPROVEMENT_ENGINE("Self-Improvement Engine", "Autonomous failure analysis, pattern learning, and self-upgrades"),
        CODE_INTELLIGENCE_ENGINE("Code Intelligence Engine", "Repository analysis, code generation, refactoring, and testing"),
        SECURITY_ENGINE("Security Engine", "RBAC access control, encryption, threat detection, and audit logging"),
        PERFORMANCE_ENGINE("Performance Engine", "Smart L1/L2 caching, virtual thread load balancing, and token optimization"),
        OBSERVABILITY_ENGINE("Observability Engine", "Distributed tracing, real-time metrics, error tracking, and AI analytics");

        private final String displayName;
        private final String mission;

        CoreModuleType(String displayName, String mission) {
            this.displayName = displayName;
            this.mission = mission;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getMission() {
            return mission;
        }
    }

    public enum CoreModuleStatus {
        HEALTHY,
        DEGRADED,
        OPTIMIZING
    }

    public record CoreModuleInfo(
            CoreModuleType type,
            String displayName,
            String mission,
            CoreModuleStatus status,
            double healthScore,
            long processedRequests
    ) {
        public CoreModuleInfo {
            Objects.requireNonNull(type, "type must not be null");
            displayName = (displayName != null) ? displayName : type.getDisplayName();
            mission = (mission != null) ? mission : type.getMission();
            status = (status != null) ? status : CoreModuleStatus.HEALTHY;
            healthScore = Math.clamp(healthScore, 0.0, 1.0);
        }

        public static CoreModuleInfo healthy(CoreModuleType type, long processedRequests) {
            return new CoreModuleInfo(type, type.getDisplayName(), type.getMission(), CoreModuleStatus.HEALTHY, 1.0, processedRequests);
        }
    }

    public record CoreBrainDirective(
            String directiveId,
            String tenantId,
            String userId,
            String objective,
            String preferredModel,
            boolean localFirstRequired,
            String reasoningEffort,
            Map<String, Object> parameters
    ) {
        public CoreBrainDirective {
            directiveId = (directiveId != null && !directiveId.isBlank()) ? directiveId : "dir-" + UUID.randomUUID();
            tenantId = (tenantId != null && !tenantId.isBlank()) ? tenantId : "default";
            userId = (userId != null && !userId.isBlank()) ? userId : "engineer";
            Objects.requireNonNull(objective, "objective must not be null");
            if (objective.isBlank()) {
                throw new IllegalArgumentException("objective must not be blank");
            }
            preferredModel = (preferredModel != null && !preferredModel.isBlank()) ? preferredModel : "oenexa-neo-gpt-o3-max";
            reasoningEffort = (reasoningEffort != null && !reasoningEffort.isBlank()) ? reasoningEffort : "high";
            parameters = (parameters != null) ? Map.copyOf(parameters) : Map.of();
        }

        public static CoreBrainDirective of(String objective) {
            return new CoreBrainDirective(null, "default", "engineer", objective, "oenexa-neo-gpt-o3-max", true, "high", Map.of());
        }
    }

    public record GatewayRoutingDecision(
            String selectedModel,
            String provider,
            boolean isLocalFirst,
            double estimatedLatencyMs,
            boolean securityCleared
    ) {
        public GatewayRoutingDecision {
            Objects.requireNonNull(selectedModel, "selectedModel must not be null");
            provider = (provider != null) ? provider : "OENEXA_SOVEREIGN_ENGINE";
        }
    }

    public record SecurityAssessment(
            boolean allowed,
            double threatScore,
            String threatClassification,
            String integrityHash,
            Instant checkedAt
    ) {
        public SecurityAssessment {
            threatClassification = (threatClassification != null) ? threatClassification : "CLEAN";
            integrityHash = (integrityHash != null) ? integrityHash : "sha256:verified";
            checkedAt = (checkedAt != null) ? checkedAt : Instant.now();
        }

        public static SecurityAssessment clean() {
            return new SecurityAssessment(true, 0.0, "CLEAN", "sha256:verified", Instant.now());
        }

        public static SecurityAssessment denied(String reason, double threatScore) {
            return new SecurityAssessment(false, threatScore, reason, "sha256:blocked", Instant.now());
        }
    }

    public record PerformanceSnapshot(
            boolean cacheHit,
            double compressionRatio,
            int activeVirtualThreads,
            long memoryAllocatedBytes,
            long durationMs
    ) {
        public PerformanceSnapshot {
            compressionRatio = Math.max(1.0, compressionRatio);
            activeVirtualThreads = Math.max(1, activeVirtualThreads);
        }
    }

    public record ObservabilitySpan(
            String traceId,
            String spanId,
            String engineName,
            String operation,
            long durationMs,
            String status,
            Map<String, String> tags
    ) {
        public ObservabilitySpan {
            traceId = (traceId != null) ? traceId : UUID.randomUUID().toString();
            spanId = (spanId != null) ? spanId : UUID.randomUUID().toString();
            engineName = (engineName != null) ? engineName : "OENEXA_BRAIN";
            operation = (operation != null) ? operation : "EXECUTE_DIRECTIVE";
            status = (status != null) ? status : "OK";
            tags = (tags != null) ? Map.copyOf(tags) : Map.of();
        }
    }

    public record CoreBrainSynthesis(
            String directiveId,
            String status,
            CoreBrainDirective directive,
            GatewayRoutingDecision routingDecision,
            SecurityAssessment securityAssessment,
            List<String> reasoningSteps,
            List<ChatDomain.CodeArtifact> synthesizedArtifacts,
            PerformanceSnapshot performanceSnapshot,
            ObservabilitySpan telemetrySpan,
            double confidenceScore
    ) {
        public CoreBrainSynthesis {
            Objects.requireNonNull(directiveId, "directiveId must not be null");
            status = (status != null) ? status : "COMPLETED";
            Objects.requireNonNull(directive, "directive must not be null");
            Objects.requireNonNull(routingDecision, "routingDecision must not be null");
            Objects.requireNonNull(securityAssessment, "securityAssessment must not be null");
            reasoningSteps = (reasoningSteps != null) ? List.copyOf(reasoningSteps) : List.of();
            synthesizedArtifacts = (synthesizedArtifacts != null) ? List.copyOf(synthesizedArtifacts) : List.of();
            Objects.requireNonNull(performanceSnapshot, "performanceSnapshot must not be null");
            Objects.requireNonNull(telemetrySpan, "telemetrySpan must not be null");
            confidenceScore = Math.clamp(confidenceScore, 0.0, 1.0);
        }
    }
}
