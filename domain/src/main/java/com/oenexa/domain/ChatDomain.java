package com.oenexa.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Domain contracts for Interactive Chat, Gemini Reasoning, Tool Calling, and Context Windows.
 */
public class ChatDomain {

    public enum Role {
        SYSTEM, USER, ASSISTANT, TOOL
    }

    public record ToolCall(String callId, String toolName, Map<String, Object> arguments) {
        public ToolCall(String callId, String toolName) {
            this(callId, toolName, Map.of());
        }
    }

    public record ToolResult(String callId, String toolName, String output, boolean isError) {
        public ToolResult(String callId, String toolName, String output) {
            this(callId, toolName, output, false);
        }
    }

    public record CodeContextAttachment(
            String filePath,
            String language,
            int startLine,
            int endLine,
            String content
    ) {
        public CodeContextAttachment(String filePath, String content) {
            this(filePath, "plaintext", 1, 1, content);
        }
    }

    public record CodeArtifact(
            String artifactId,
            String title,
            String filePath,
            String language,
            String codeContent,
            String action
    ) {
        public CodeArtifact(String title, String filePath, String codeContent) {
            this(java.util.UUID.randomUUID().toString(), title, filePath, "java", codeContent, "CREATE");
        }
    }

    public record ConversationMetadata(
            String conversationId,
            String tenantId,
            String userId,
            String title,
            String model,
            Instant createdAt,
            Instant updatedAt,
            int messageCount,
            boolean pinned
    ) {
        public ConversationMetadata(String conversationId, String title) {
            this(conversationId, "default-tenant", "default-user", title, "gemini-2.5-flash", Instant.now(), Instant.now(), 0, false);
        }
    }

    public record Message(
            Role role,
            String content,
            Instant timestamp,
            String thoughtProcess,
            String model,
            List<CodeContextAttachment> attachments,
            List<ToolCall> toolCalls,
            List<ToolResult> toolResults,
            List<CodeArtifact> artifacts
    ) {
        public Message(Role role, String content, Instant timestamp) {
            this(role, content, timestamp, null, "gemini-2.5-flash", List.of(), List.of(), List.of(), List.of());
        }

        public Message(Role role, String content, Instant timestamp, String thoughtProcess, String model) {
            this(role, content, timestamp, thoughtProcess, model, List.of(), List.of(), List.of(), List.of());
        }
    }

    public record Conversation(String conversationId, String userId, List<Message> messages) {}

    public record ChatRequest(
            String message,
            String conversationId,
            String model,
            boolean includeThinking,
            List<CodeContextAttachment> attachments
    ) {
        public ChatRequest(String message, String conversationId) {
            this(message, conversationId, "gemini-2.5-flash", true, List.of());
        }

        public ChatRequest(String message, String conversationId, String model, boolean includeThinking) {
            this(message, conversationId, model, includeThinking, List.of());
        }
    }

    public record ChatResponse(
            String reply,
            String conversationId,
            int contextTokensUsed,
            String thoughtProcess,
            String model,
            String intent,
            List<String> suggestedFollowUps,
            List<ToolCall> toolCalls,
            List<CodeArtifact> artifacts,
            double confidenceScore
    ) {
        public ChatResponse(String reply, String conversationId, int contextTokensUsed) {
            this(reply, conversationId, contextTokensUsed, null, "gemini-2.5-flash", "GENERAL_ASSISTANT", List.of(), List.of(), List.of(), 0.95);
        }

        public ChatResponse(
                String reply,
                String conversationId,
                int contextTokensUsed,
                String thoughtProcess,
                String model,
                String intent,
                List<String> suggestedFollowUps
        ) {
            this(reply, conversationId, contextTokensUsed, thoughtProcess, model, intent, suggestedFollowUps, List.of(), List.of(), 0.95);
        }
    }

    public enum ModelProvider {
        OPENAI_GPT, ANTHROPIC_CLAUDE, GEMINI, DEEPSEEK, MISTRAL, LLAMA, OENEXA_CUSTOM
    }

    public enum TaskType {
        CODING, REASONING, FAST_CHAT, REPO_ANALYSIS, BUG_FIX, SECURITY_AUDIT, ARCHITECTURE
    }

    public enum ConversationCategory {
        GENERAL, ARCHITECTURE, CODE_ENGINEERING, BUG_FIXING, TESTING, DEVOPS, SECURITY
    }

    public enum SoftwareEngineerAgentType {
        CODE_AGENT, BUG_FIX_AGENT, TEST_AGENT, DEVOPS_AGENT, DOCUMENTATION_AGENT, SECURITY_AGENT, ARCHITECTURE_AGENT
    }

    public record UserMemoryRecord(
            String memoryId,
            String userId,
            String tenantId,
            String memoryKey,
            String memoryValue,
            String category,
            double confidence,
            Instant learnedAt
    ) {
        public UserMemoryRecord(String userId, String memoryKey, String memoryValue) {
            this(java.util.UUID.randomUUID().toString(), userId, "default-tenant", memoryKey, memoryValue, "USER_PREFERENCE", 0.95, Instant.now());
        }
    }

    public record ProjectKnowledgeChunk(
            String chunkId,
            String repositoryId,
            String filePath,
            String language,
            int chunkIndex,
            String content,
            List<Double> embedding,
            Map<String, String> symbols,
            Instant createdAt
    ) {
        public ProjectKnowledgeChunk(String repositoryId, String filePath, String language, String content) {
            this(java.util.UUID.randomUUID().toString(), repositoryId, filePath, language, 0, content, List.of(), Map.of(), Instant.now());
        }
    }

    public record ModelRoutingDecision(
            ModelProvider provider,
            String modelName,
            TaskType taskType,
            String rationale
    ) {}

    public record AgentCollaborationTask(
            String taskId,
            SoftwareEngineerAgentType agentType,
            String taskDescription,
            String inputPayload,
            String outputResult,
            String status,
            Instant timestamp
    ) {
        public AgentCollaborationTask(SoftwareEngineerAgentType agentType, String taskDescription) {
            this(java.util.UUID.randomUUID().toString(), agentType, taskDescription, "", "", "PENDING", Instant.now());
        }
    }

    public record RepositoryKnowledgeMap(
            String repositoryId,
            String primaryStack,
            List<String> detectedTechnologies,
            List<String> modules,
            List<String> fileTree,
            Map<String, String> symbolIndex,
            int vectorChunksCount
    ) {
        public RepositoryKnowledgeMap(String repositoryId, String primaryStack) {
            this(repositoryId, primaryStack, List.of(primaryStack), List.of(), List.of(), Map.of(), 0);
        }
    }

    public record IntelligentChatRequest(
            String message,
            String conversationId,
            String userId,
            String repositoryId,
            String preferredModel,
            boolean includeThinking,
            List<CodeContextAttachment> attachments
    ) {
        public IntelligentChatRequest(String message, String conversationId) {
            this(message, conversationId, "default-user", "default-repo", "auto", true, List.of());
        }
    }

    public record IntelligentChatResponse(
            String reply,
            String conversationId,
            String modelUsed,
            ModelProvider provider,
            TaskType taskType,
            String thoughtProcess,
            String contextSummary,
            List<String> shortTermMemorySnippets,
            List<UserMemoryRecord> relevantUserMemories,
            List<ProjectKnowledgeChunk> repositoryCitations,
            List<ToolCall> toolCalls,
            List<CodeArtifact> artifacts,
            List<String> followUps,
            double rankingScore,
            double confidenceScore
    ) {}

    public enum ArgonExecutionPhase {
        PLANNING, DECOMPOSITION, TOOL_EXECUTION, SELF_HEALING_VERIFICATION, REFLECTION, COMPLETED, FAILED
    }

    public record ArgonPlanStep(
            String stepId,
            SoftwareEngineerAgentType agentPersona,
            String description,
            String toolToInvoke,
            String status,
            String output,
            int retryCount
    ) {
        public ArgonPlanStep(String stepId, SoftwareEngineerAgentType agentPersona, String description, String toolToInvoke) {
            this(stepId, agentPersona, description, toolToInvoke, "PENDING", "", 0);
        }
    }

    public record ArgonExecutionEvent(
            String eventId,
            String goalId,
            ArgonExecutionPhase phase,
            SoftwareEngineerAgentType agentPersona,
            String thoughtProcess,
            String toolAction,
            String outputSnippet,
            String status,
            Instant timestamp
    ) {
        public ArgonExecutionEvent(String goalId, ArgonExecutionPhase phase, SoftwareEngineerAgentType agentPersona, String thoughtProcess, String toolAction, String outputSnippet) {
            this(java.util.UUID.randomUUID().toString(), goalId, phase, agentPersona, thoughtProcess, toolAction, outputSnippet, "SUCCESS", Instant.now());
        }
    }

    public record ArgonSelfHealingCycle(
            String cycleId,
            String goalId,
            String issueDetected,
            String rootCause,
            String patchDiff,
            boolean resolved,
            Instant timestamp
    ) {
        public ArgonSelfHealingCycle(String goalId, String issueDetected, String rootCause, String patchDiff, boolean resolved) {
            this(java.util.UUID.randomUUID().toString(), goalId, issueDetected, rootCause, patchDiff, resolved, Instant.now());
        }
    }

    public record ArgonAgentPlan(
            String planId,
            String goalId,
            String goalDescription,
            List<ArgonPlanStep> steps,
            ArgonExecutionPhase currentPhase,
            Instant createdAt,
            Instant updatedAt
    ) {
        public ArgonAgentPlan(String planId, String goalId, String goalDescription, List<ArgonPlanStep> steps) {
            this(planId, goalId, goalDescription, steps, ArgonExecutionPhase.PLANNING, Instant.now(), Instant.now());
        }
    }

    public record ArgonAutonomousGoalRequest(
            String goal,
            String repositoryId,
            String userId,
            boolean autonomousSelfHealing,
            boolean extendedThinking,
            List<CodeContextAttachment> attachments
    ) {
        public ArgonAutonomousGoalRequest(String goal) {
            this(goal, "default-repo", "default-user", true, true, List.of());
        }
    }

    public record ArgonAutonomousGoalResponse(
            String goalId,
            String goalDescription,
            ArgonAgentPlan plan,
            List<ArgonExecutionEvent> events,
            List<ArgonSelfHealingCycle> selfHealingCycles,
            List<CodeArtifact> artifacts,
            double confidenceScore,
            String finalSummary,
            Instant completedAt
    ) {}
}


