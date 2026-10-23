package com.oenexa.domain.model.chat;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain contracts for Interactive Chat, Reasoning, Tool Calling, and Context Windows.
 * Follows modern Java 25 record idioms with defensive copies and non-null validation.
 */
public class ChatDomain {

    public enum Role {
        SYSTEM, USER, ASSISTANT, TOOL
    }

    public record ToolCall(String callId, String toolName, Map<String, Object> arguments) {
        public ToolCall {
            Objects.requireNonNull(callId, "callId must not be null");
            Objects.requireNonNull(toolName, "toolName must not be null");
            arguments = (arguments != null) ? Map.copyOf(arguments) : Map.of();
        }
        public ToolCall(String callId, String toolName) {
            this(callId, toolName, Map.of());
        }
    }

    public record ToolResult(String callId, String toolName, String output, boolean isError) {
        public ToolResult {
            Objects.requireNonNull(callId, "callId must not be null");
            Objects.requireNonNull(toolName, "toolName must not be null");
            output = (output != null) ? output : "";
        }
        public ToolResult(String callId, String toolName, String output) {
            this(callId, toolName, output, false);
        }
    }

    public record CodeContextAttachment(String filePath, String language, int startLine, int endLine, String content) {
        public CodeContextAttachment {
            Objects.requireNonNull(filePath, "filePath must not be null");
            language = (language != null) ? language : "plaintext";
            content = (content != null) ? content : "";
        }
        public CodeContextAttachment(String filePath, String content) {
            this(filePath, "plaintext", 1, 1, content);
        }
    }

    public record CodeArtifact(String artifactId, String title, String filePath, String language, String codeContent, String action) {
        public CodeArtifact {
            Objects.requireNonNull(artifactId, "artifactId must not be null");
            Objects.requireNonNull(title, "title must not be null");
            language = (language != null) ? language : "java";
            codeContent = (codeContent != null) ? codeContent : "";
            action = (action != null) ? action : "CREATE";
        }
        public CodeArtifact(String title, String filePath, String codeContent) {
            this(UUID.randomUUID().toString(), title, filePath, "java", codeContent, "CREATE");
        }
    }

    public record ConversationMetadata(
            String conversationId, String tenantId, String userId, String title,
            String model, Instant createdAt, Instant updatedAt, int messageCount, boolean pinned
    ) {
        public ConversationMetadata {
            Objects.requireNonNull(conversationId, "conversationId must not be null");
            tenantId = (tenantId != null) ? tenantId : "default-tenant";
            userId = (userId != null) ? userId : "default-user";
            title = (title != null) ? title : "New Conversation";
            model = (model != null) ? model : "gemini-2.5-flash";
            createdAt = (createdAt != null) ? createdAt : Instant.now();
            updatedAt = (updatedAt != null) ? updatedAt : Instant.now();
        }
        public ConversationMetadata(String conversationId, String title) {
            this(conversationId, "default-tenant", "default-user", title, "gemini-2.5-flash", Instant.now(), Instant.now(), 0, false);
        }
    }

    public record Message(
            Role role, String content, Instant timestamp, String thoughtProcess, String model,
            List<CodeContextAttachment> attachments, List<ToolCall> toolCalls,
            List<ToolResult> toolResults, List<CodeArtifact> artifacts
    ) {
        public Message {
            Objects.requireNonNull(role, "role must not be null");
            content = (content != null) ? content : "";
            timestamp = (timestamp != null) ? timestamp : Instant.now();
            attachments = (attachments != null) ? List.copyOf(attachments) : List.of();
            toolCalls = (toolCalls != null) ? List.copyOf(toolCalls) : List.of();
            toolResults = (toolResults != null) ? List.copyOf(toolResults) : List.of();
            artifacts = (artifacts != null) ? List.copyOf(artifacts) : List.of();
        }
        public Message(Role role, String content, Instant timestamp) {
            this(role, content, timestamp, null, "gemini-2.5-flash", List.of(), List.of(), List.of(), List.of());
        }
        public Message(Role role, String content, Instant timestamp, String thoughtProcess, String model) {
            this(role, content, timestamp, thoughtProcess, model, List.of(), List.of(), List.of(), List.of());
        }
    }

    public record Conversation(String conversationId, String userId, List<Message> messages) {
        public Conversation {
            Objects.requireNonNull(conversationId, "conversationId must not be null");
            userId = (userId != null) ? userId : "default-user";
            messages = (messages != null) ? List.copyOf(messages) : List.of();
        }
    }

    public record ChatRequest(
            String message, String conversationId, String model,
            boolean includeThinking, List<CodeContextAttachment> attachments
    ) {
        public ChatRequest {
            conversationId = (conversationId != null) ? conversationId : UUID.randomUUID().toString();
            model = (model != null) ? model : "gemini-2.5-flash";
            attachments = (attachments != null) ? List.copyOf(attachments) : List.of();
        }
        public ChatRequest(String message, String conversationId) {
            this(message, conversationId, "gemini-2.5-flash", true, List.of());
        }
        public ChatRequest(String message, String conversationId, String model, boolean includeThinking) {
            this(message, conversationId, model, includeThinking, List.of());
        }
    }

    public record ChatResponse(
            String reply, String conversationId, int contextTokensUsed, String thoughtProcess,
            String model, String intent, List<String> suggestedFollowUps,
            List<ToolCall> toolCalls, List<CodeArtifact> artifacts, double confidenceScore
    ) {
        public ChatResponse {
            Objects.requireNonNull(reply, "reply must not be null");
            suggestedFollowUps = (suggestedFollowUps != null) ? List.copyOf(suggestedFollowUps) : List.of();
            toolCalls = (toolCalls != null) ? List.copyOf(toolCalls) : List.of();
            artifacts = (artifacts != null) ? List.copyOf(artifacts) : List.of();
        }
        public ChatResponse(String reply, String conversationId, int contextTokensUsed) {
            this(reply, conversationId, contextTokensUsed, null, "gemini-2.5-flash", "GENERAL_ASSISTANT", List.of(), List.of(), List.of(), 0.95);
        }
        public ChatResponse(String reply, String conversationId, int contextTokensUsed, String thoughtProcess, String model, String intent, List<String> suggestedFollowUps) {
            this(reply, conversationId, contextTokensUsed, thoughtProcess, model, intent, suggestedFollowUps, List.of(), List.of(), 0.95);
        }
    }

    public record AutoChatTurnRequest(String conversationId, String contextCode, String goal, String preferredModel, boolean includeThinking) {
        public AutoChatTurnRequest(String conversationId, String contextCode, String goal) {
            this(conversationId, contextCode, goal, "oenexa-neo-gpt-o3-max", true);
        }
    }

    public enum ModelProvider {
        OPENAI_GPT, ANTHROPIC_CLAUDE, GEMINI, DEEPSEEK, MISTRAL, LLAMA, QWEN, OENEXA_GPT, OENEXA_CUSTOM, LOCAL_OLLAMA, LOCAL_PRIVATE
    }

    public enum TaskType {
        CODING, REASONING, FAST_CHAT, REPO_ANALYSIS, BUG_FIX, SECURITY_AUDIT, ARCHITECTURE, PRODUCT_MANAGEMENT, MULTIMODAL_ANALYSIS
    }

    public enum ConversationCategory {
        GENERAL, ARCHITECTURE, CODE_ENGINEERING, BUG_FIXING, TESTING, DEVOPS, SECURITY, PRODUCT_MANAGEMENT, MULTIMODAL
    }

    public enum SoftwareEngineerAgentType {
        CODE_AGENT, BUG_FIX_AGENT, TEST_AGENT, DEVOPS_AGENT, DOCUMENTATION_AGENT, SECURITY_AGENT, ARCHITECTURE_AGENT, PRODUCT_MANAGER_AGENT
    }

    public record UserMemoryRecord(
            String memoryId, String userId, String tenantId, String memoryKey,
            String memoryValue, String category, double confidence, Instant learnedAt
    ) {
        public UserMemoryRecord(String userId, String memoryKey, String memoryValue) {
            this(UUID.randomUUID().toString(), userId, "default-tenant", memoryKey, memoryValue, "USER_PREFERENCE", 0.95, Instant.now());
        }
    }

    public record ProjectKnowledgeChunk(
            String chunkId, String repositoryId, String filePath, String language,
            int chunkIndex, String content, List<Double> embedding,
            Map<String, String> symbols, Instant createdAt
    ) {
        public ProjectKnowledgeChunk {
            Objects.requireNonNull(chunkId, "chunkId must not be null");
            embedding = (embedding != null) ? List.copyOf(embedding) : List.of();
            symbols = (symbols != null) ? Map.copyOf(symbols) : Map.of();
        }
        public ProjectKnowledgeChunk(String repositoryId, String filePath, String language, String content) {
            this(UUID.randomUUID().toString(), repositoryId, filePath, language, 0, content, List.of(), Map.of(), Instant.now());
        }
    }

    public record ModelRoutingDecision(ModelProvider provider, String modelName, TaskType taskType, String rationale) {}

    public record AgentCollaborationTask(
            String taskId, SoftwareEngineerAgentType agentType, String taskDescription,
            String inputPayload, String outputResult, String status, Instant timestamp
    ) {
        public AgentCollaborationTask(SoftwareEngineerAgentType agentType, String taskDescription) {
            this(UUID.randomUUID().toString(), agentType, taskDescription, "", "", "PENDING", Instant.now());
        }
    }

    public record RepositoryKnowledgeMap(
            String repositoryId, String primaryStack, List<String> detectedTechnologies,
            List<String> modules, List<String> fileTree, Map<String, String> symbolIndex, int vectorChunksCount
    ) {
        public RepositoryKnowledgeMap {
            detectedTechnologies = (detectedTechnologies != null) ? List.copyOf(detectedTechnologies) : List.of();
            modules = (modules != null) ? List.copyOf(modules) : List.of();
            fileTree = (fileTree != null) ? List.copyOf(fileTree) : List.of();
            symbolIndex = (symbolIndex != null) ? Map.copyOf(symbolIndex) : Map.of();
        }
        public RepositoryKnowledgeMap(String repositoryId, String primaryStack) {
            this(repositoryId, primaryStack, List.of(primaryStack), List.of(), List.of(), Map.of(), 0);
        }
    }

    public record IntelligentChatRequest(
            String message, String conversationId, String userId, String repositoryId,
            String preferredModel, boolean includeThinking, List<CodeContextAttachment> attachments
    ) {
        public IntelligentChatRequest(String message, String conversationId) {
            this(message, conversationId, "default-user", "default-repo", "auto", true, List.of());
        }
    }

    public record IntelligentChatResponse(
            String reply, String conversationId, String modelUsed, ModelProvider provider,
            TaskType taskType, String thoughtProcess, String contextSummary,
            List<String> shortTermMemorySnippets, List<UserMemoryRecord> relevantUserMemories,
            List<ProjectKnowledgeChunk> repositoryCitations, List<ToolCall> toolCalls,
            List<CodeArtifact> artifacts, List<String> followUps, double rankingScore, double confidenceScore
    ) {
        public IntelligentChatResponse {
            shortTermMemorySnippets = (shortTermMemorySnippets != null) ? List.copyOf(shortTermMemorySnippets) : List.of();
            relevantUserMemories = (relevantUserMemories != null) ? List.copyOf(relevantUserMemories) : List.of();
            repositoryCitations = (repositoryCitations != null) ? List.copyOf(repositoryCitations) : List.of();
            toolCalls = (toolCalls != null) ? List.copyOf(toolCalls) : List.of();
            artifacts = (artifacts != null) ? List.copyOf(artifacts) : List.of();
            followUps = (followUps != null) ? List.copyOf(followUps) : List.of();
        }
    }

    public enum OenexaExecutionPhase {
        PLANNING, DECOMPOSITION, TOOL_EXECUTION, SELF_HEALING_VERIFICATION, REFLECTION, COMPLETED, FAILED
    }

    public record OenexaPlanStep(
            String stepId, SoftwareEngineerAgentType agentPersona, String description,
            String toolToInvoke, String status, String output, int retryCount
    ) {
        public OenexaPlanStep(String stepId, SoftwareEngineerAgentType agentPersona, String description, String toolToInvoke) {
            this(stepId, agentPersona, description, toolToInvoke, "PENDING", "", 0);
        }
    }

    public record OenexaExecutionEvent(
            String eventId, String goalId, OenexaExecutionPhase phase,
            SoftwareEngineerAgentType agentPersona, String thoughtProcess,
            String toolAction, String outputSnippet, String status, Instant timestamp
    ) {
        public OenexaExecutionEvent(String goalId, OenexaExecutionPhase phase, SoftwareEngineerAgentType agentPersona, String thoughtProcess, String toolAction, String outputSnippet) {
            this(UUID.randomUUID().toString(), goalId, phase, agentPersona, thoughtProcess, toolAction, outputSnippet, "SUCCESS", Instant.now());
        }
    }

    public record OenexaSelfHealingCycle(
            String cycleId, String goalId, String issueDetected,
            String rootCause, String patchDiff, boolean resolved, Instant timestamp
    ) {
        public OenexaSelfHealingCycle(String goalId, String issueDetected, String rootCause, String patchDiff, boolean resolved) {
            this(UUID.randomUUID().toString(), goalId, issueDetected, rootCause, patchDiff, resolved, Instant.now());
        }
    }

    public record OenexaAgentPlan(
            String planId, String goalId, String goalDescription,
            List<OenexaPlanStep> steps, OenexaExecutionPhase currentPhase,
            Instant createdAt, Instant updatedAt
    ) {
        public OenexaAgentPlan {
            steps = (steps != null) ? List.copyOf(steps) : List.of();
        }
        public OenexaAgentPlan(String planId, String goalId, String goalDescription, List<OenexaPlanStep> steps) {
            this(planId, goalId, goalDescription, steps, OenexaExecutionPhase.PLANNING, Instant.now(), Instant.now());
        }
    }

    public enum MultimodalType {
        IMAGE, AUDIO_VOICE, FILE_DOCUMENT
    }

    public record MultimodalAttachment(
            String id, MultimodalType type, String fileName, String mimeType,
            String base64DataOrUrl, String extractedTextOrTranscript, Instant uploadedAt
    ) {
        public MultimodalAttachment(MultimodalType type, String fileName, String mimeType, String base64DataOrUrl, String extractedTextOrTranscript) {
            this(UUID.randomUUID().toString(), type, fileName, mimeType, base64DataOrUrl, extractedTextOrTranscript, Instant.now());
        }
    }

    public record UserStory(
            String id, String title, String asA, String iWant, String soThat,
            List<String> acceptanceCriteria, int storyPoints, String status
    ) {
        public UserStory {
            acceptanceCriteria = (acceptanceCriteria != null) ? List.copyOf(acceptanceCriteria) : List.of();
        }
        public UserStory(String title, String asA, String iWant, String soThat, List<String> acceptanceCriteria) {
            this(UUID.randomUUID().toString(), title, asA, iWant, soThat, acceptanceCriteria, 3, "BACKLOG");
        }
    }

    public record ProductRequirementsDocument(
            String prdId, String title, String targetAudience, String problemStatement,
            List<String> keyFeatures, List<UserStory> userStories,
            List<String> technicalConstraints, String releaseRoadmap, Instant createdAt
    ) {
        public ProductRequirementsDocument {
            keyFeatures = (keyFeatures != null) ? List.copyOf(keyFeatures) : List.of();
            userStories = (userStories != null) ? List.copyOf(userStories) : List.of();
            technicalConstraints = (technicalConstraints != null) ? List.copyOf(technicalConstraints) : List.of();
        }
        public ProductRequirementsDocument(String title, String targetAudience, String problemStatement, List<String> keyFeatures, List<UserStory> userStories) {
            this(UUID.randomUUID().toString(), title, targetAudience, problemStatement, keyFeatures, userStories, List.of("Hexagonal Monolith", "100% Zero-Mock Line Coverage"), "v1.0.0 Sprint Delivery", Instant.now());
        }
    }

    public record LocalPrivacyConfig(
            boolean localOnlyMode, boolean telemetryDisabled, String localModelEndpoint,
            String selectedLocalModel, int maxVramAllocationMb, boolean airGappedVerification
    ) {
        public LocalPrivacyConfig() {
            this(true, true, "in-process-native-neural-engine", "oenexa-neo-gpt-o3-max", 8192, true);
        }
    }

    public record OenexaAutonomousGoalRequest(
            String goal, String repositoryId, String userId,
            boolean autonomousSelfHealing, boolean extendedThinking,
            List<CodeContextAttachment> attachments, boolean localFirstMode,
            List<MultimodalAttachment> multimodalAttachments
    ) {
        public OenexaAutonomousGoalRequest {
            attachments = (attachments != null) ? List.copyOf(attachments) : List.of();
            multimodalAttachments = (multimodalAttachments != null) ? List.copyOf(multimodalAttachments) : List.of();
        }
        public OenexaAutonomousGoalRequest(String goal, String repositoryId, String userId, boolean autonomousSelfHealing, boolean extendedThinking, List<CodeContextAttachment> attachments) {
            this(goal, repositoryId, userId, autonomousSelfHealing, extendedThinking, attachments, true, List.of());
        }
        public OenexaAutonomousGoalRequest(String goal) {
            this(goal, "default-repo", "default-user", true, true, List.of(), true, List.of());
        }
    }

    public record OenexaAutonomousGoalResponse(
            String goalId, String goalDescription, OenexaAgentPlan plan,
            List<OenexaExecutionEvent> events, List<OenexaSelfHealingCycle> selfHealingCycles,
            List<CodeArtifact> artifacts, List<UserStory> userStories,
            ProductRequirementsDocument prd, LocalPrivacyConfig privacyAudit,
            double confidenceScore, String finalSummary, Instant completedAt
    ) {
        public OenexaAutonomousGoalResponse {
            events = (events != null) ? List.copyOf(events) : List.of();
            selfHealingCycles = (selfHealingCycles != null) ? List.copyOf(selfHealingCycles) : List.of();
            artifacts = (artifacts != null) ? List.copyOf(artifacts) : List.of();
            userStories = (userStories != null) ? List.copyOf(userStories) : List.of();
        }
        public OenexaAutonomousGoalResponse(
                String goalId, String goalDescription, OenexaAgentPlan plan,
                List<OenexaExecutionEvent> events, List<OenexaSelfHealingCycle> selfHealingCycles,
                List<CodeArtifact> artifacts, double confidenceScore,
                String finalSummary, Instant completedAt
        ) {
            this(goalId, goalDescription, plan, events, selfHealingCycles, artifacts, List.of(), null, new LocalPrivacyConfig(), confidenceScore, finalSummary, completedAt);
        }
    }
}

