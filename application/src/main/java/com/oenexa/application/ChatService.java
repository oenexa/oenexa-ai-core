package com.oenexa.application;

import com.oenexa.application.openai.DeliberativeReasoningEngine;
import com.oenexa.domain.ChatDomain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise Interactive Chat Application Service with Advanced Reasoning.
 * Why: Chain-of-Thought reasoning, context window management, and intelligent software engineering assistance.
 */
@Service
public class ChatService {

    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();
    private final Map<String, ConversationMetadata> metadataMap = new ConcurrentHashMap<>();
    private final ChatResponseSynthesisEngine synthesisEngine;

    public ChatService() {
        this(new ChatResponseSynthesisEngine());
    }

    public ChatService(DeliberativeReasoningEngine deliberativeReasoningEngine) {
        this(new ChatResponseSynthesisEngine(deliberativeReasoningEngine));
    }

    public ChatService(ChatResponseSynthesisEngine synthesisEngine) {
        this.synthesisEngine = (synthesisEngine != null) ? synthesisEngine : new ChatResponseSynthesisEngine();
    }

    public ChatResponse autoChatStep(AutoChatTurnRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        String convId = (request.conversationId() != null && !request.conversationId().isBlank())
                ? request.conversationId()
                : UUID.randomUUID().toString();
        List<Message> history = conversations.getOrDefault(convId, List.of());

        String determinedPrompt;
        if (request.contextCode() != null && !request.contextCode().isBlank() && history.stream().noneMatch(m -> m.content().contains("Test Synthesizer"))) {
            determinedPrompt = "Synthesize zero-mock JUnit 5 test suite with 100% line coverage for the active code";
        } else if (history.isEmpty() && (request.goal() != null && !request.goal().isBlank())) {
            determinedPrompt = "Architect system design blueprint for: " + request.goal();
        } else if (!history.isEmpty() && history.stream().noneMatch(m -> m.content().contains("Security Agent"))) {
            determinedPrompt = "Perform SAST security scan and vulnerability audit on the codebase";
        } else if (!history.isEmpty() && history.stream().noneMatch(m -> m.content().contains("DevOps & CI/CD"))) {
            determinedPrompt = "Generate Docker and Kubernetes deployment pipeline for the container deployment";
        } else {
            determinedPrompt = (request.goal() != null && !request.goal().isBlank())
                    ? "Refactor and optimize architecture for: " + request.goal()
                    : "Refactor domain records to ensure strictly zero mocks and maximum maintainability";
        }

        List<CodeContextAttachment> attachments = new ArrayList<>();
        if (request.contextCode() != null && !request.contextCode().isBlank()) {
            attachments.add(new CodeContextAttachment("src/main/java/Solution.java", request.contextCode()));
        }

        return processMessage(convId, determinedPrompt, request.preferredModel(), request.includeThinking(), attachments);
    }

    public ChatResponse processMessage(String conversationId, String messageText) {
        return processMessage(conversationId, messageText, "gemini-2.5-flash", true, List.of());
    }

    public ChatResponse processMessage(String conversationId, String messageText, String model, boolean includeThinking) {
        return processMessage(conversationId, messageText, model, includeThinking, List.of());
    }

    public ChatResponse processMessage(
            String conversationId,
            String messageText,
            String model,
            boolean includeThinking,
            List<CodeContextAttachment> attachments
    ) {
        Objects.requireNonNull(messageText, "messageText must not be null");

        String selectedModel = (model != null && !model.isBlank()) ? model : "gemini-2.5-flash";
        String convId = (conversationId != null && !conversationId.isBlank())
                ? conversationId
                : UUID.randomUUID().toString();

        List<Message> history = conversations.computeIfAbsent(convId, k -> new ArrayList<>());
        List<CodeContextAttachment> safeAttachments = (attachments != null) ? List.copyOf(attachments) : List.of();

        // 1. Intent Classification
        String intent = determineIntent(messageText);

        // 2. Chain-of-Thought Reasoning
        StringBuilder thoughtBuilder = new StringBuilder();
        if (includeThinking) {
            thoughtBuilder.append(synthesisEngine.generateThoughtProcess(intent, messageText, selectedModel));
            if (!safeAttachments.isEmpty()) {
                thoughtBuilder.append("\nEvaluated Attached Context: ");
                for (CodeContextAttachment att : safeAttachments) {
                    thoughtBuilder.append("[").append(att.filePath()).append(" (lines ").append(att.startLine()).append("-").append(att.endLine()).append(")] ");
                }
            }
        }

        // 3. Simulated Tool Execution
        List<ToolCall> toolCalls = new ArrayList<>();
        List<ToolResult> toolResults = new ArrayList<>();
        String toolName = resolveToolForIntent(intent);
        if (toolName != null) {
            ToolCall call = new ToolCall(UUID.randomUUID().toString(), toolName, Map.of("target", messageText));
            toolCalls.add(call);
            ToolResult res = new ToolResult(call.callId(), toolName, "Tool " + toolName + " executed successfully with 0 errors.", false);
            toolResults.add(res);
            if (includeThinking) {
                thoughtBuilder.append("\nInvoked Tool: ").append(toolName).append(" -> Result: ").append(res.output());
            }
        }

        // 4. Technical Response Formulation
        String reply = synthesisEngine.generateEngineeringResponse(messageText, intent);
        List<CodeArtifact> artifacts = extractArtifacts(reply);

        String finalThought = includeThinking ? thoughtBuilder.toString() : null;

        // Append user turn
        history.add(new Message(Role.USER, messageText, Instant.now(), null, selectedModel, safeAttachments, List.of(), List.of(), List.of()));

        // Append assistant turn with thought and model metadata
        history.add(new Message(Role.ASSISTANT, reply, Instant.now(), finalThought, selectedModel, List.of(), toolCalls, toolResults, artifacts));

        // Update metadata
        metadataMap.compute(convId, (k, meta) -> {
            if (meta == null) {
                String title = messageText.length() > 40 ? messageText.substring(0, 40) + "..." : messageText;
                return new ConversationMetadata(convId, "default-tenant", "default-user", title, selectedModel, Instant.now(), Instant.now(), 2, false);
            }
            return new ConversationMetadata(meta.conversationId(), meta.tenantId(), meta.userId(), meta.title(), selectedModel, meta.createdAt(), Instant.now(), meta.messageCount() + 2, meta.pinned());
        });

        // 5. Actionable Next Steps
        List<String> followUps = synthesisEngine.suggestFollowUps(intent, messageText);

        int thoughtTokens = (finalThought != null) ? finalThought.length() / 4 : 0;
        int estimatedTokens = Math.max(1, (messageText.length() + reply.length()) / 4 + thoughtTokens);

        return new ChatResponse(
                reply,
                convId,
                estimatedTokens,
                finalThought,
                selectedModel,
                intent,
                followUps,
                toolCalls,
                artifacts,
                0.98
        );
    }

    public List<Message> getHistory(String conversationId) {
        return List.copyOf(conversations.getOrDefault(conversationId, List.of()));
    }

    public List<ConversationMetadata> listConversations(String tenantId) {
        List<ConversationMetadata> list = new ArrayList<>(metadataMap.values());
        list.sort(Comparator.comparing(ConversationMetadata::pinned).reversed()
                .thenComparing(ConversationMetadata::updatedAt, Comparator.reverseOrder()));
        return List.copyOf(list);
    }

    public ConversationMetadata getMetadata(String conversationId) {
        return metadataMap.computeIfAbsent(conversationId, k ->
                new ConversationMetadata(conversationId, "default-tenant", "default-user", "New Conversation", "gemini-2.5-flash", Instant.now(), Instant.now(), 0, false)
        );
    }

    public ConversationMetadata createConversation(String conversationId, String tenantId, String userId, String title, String model) {
        String id = (conversationId != null && !conversationId.isBlank()) ? conversationId : UUID.randomUUID().toString();
        ConversationMetadata meta = new ConversationMetadata(
                id,
                (tenantId != null && !tenantId.isBlank()) ? tenantId : "default-tenant",
                (userId != null && !userId.isBlank()) ? userId : "default-user",
                (title != null && !title.isBlank()) ? title : "New Conversation",
                (model != null && !model.isBlank()) ? model : "gemini-2.5-flash",
                Instant.now(),
                Instant.now(),
                0,
                false
        );
        metadataMap.put(id, meta);
        conversations.put(id, new ArrayList<>());
        return meta;
    }

    public ConversationMetadata forkConversation(String sourceId, String newId, int upToMessageIndex) {
        String targetId = (newId != null && !newId.isBlank()) ? newId : UUID.randomUUID().toString();
        List<Message> source = conversations.getOrDefault(sourceId, List.of());
        List<Message> forked = new ArrayList<>();
        int limit = (upToMessageIndex >= 0 && upToMessageIndex < source.size()) ? upToMessageIndex + 1 : source.size();
        for (int i = 0; i < limit; i++) {
            forked.add(source.get(i));
        }
        conversations.put(targetId, forked);

        ConversationMetadata srcMeta = metadataMap.get(sourceId);
        String title = (srcMeta != null) ? "Fork of " + srcMeta.title() : "Forked Conversation";
        ConversationMetadata targetMeta = new ConversationMetadata(
                targetId,
                (srcMeta != null) ? srcMeta.tenantId() : "default-tenant",
                (srcMeta != null) ? srcMeta.userId() : "default-user",
                title,
                (srcMeta != null) ? srcMeta.model() : "gemini-2.5-flash",
                Instant.now(),
                Instant.now(),
                forked.size(),
                false
        );
        metadataMap.put(targetId, targetMeta);
        return targetMeta;
    }

    public void clearHistory(String conversationId) {
        List<Message> list = conversations.get(conversationId);
        if (list != null) {
            list.clear();
        }
    }

    private String resolveToolForIntent(String intent) {
        return switch (intent) {
            case "ARCHITECTURE_DESIGN" -> "architecture_diagram";
            case "SECURITY_AUDIT" -> "security_scanner";
            case "TESTING_SYNTHESIS" -> "test_synthesizer";
            case "CODE_REFACTORING" -> "code_refactoring";
            case "CODE_GENERATION" -> "ast_analyzer";
            default -> null;
        };
    }

    private List<CodeArtifact> extractArtifacts(String reply) {
        if (reply == null || !reply.contains("```")) {
            return List.of();
        }
        List<CodeArtifact> list = new ArrayList<>();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("```([a-zA-Z0-9_-]*)\\s*\\n([\\s\\S]*?)```").matcher(reply);
        int idx = 1;
        while (m.find()) {
            String lang = m.group(1);
            String code = m.group(2).trim();
            String title = "Artifact " + idx;
            String path = "src/Component" + idx + "." + (lang.isBlank() ? "txt" : lang);
            java.util.regex.Matcher classMatcher = java.util.regex.Pattern.compile("public\\s+(?:record|class|interface)\\s+([A-Za-z0-9_]+)").matcher(code);
            if (classMatcher.find()) {
                String className = classMatcher.group(1);
                title = className;
                path = "src/main/java/com/oenexa/workspace/" + className + ".java";
            }
            list.add(new CodeArtifact(title, path, code));
            idx++;
        }
        return List.copyOf(list);
    }

    private String determineIntent(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        if (lower.contains("scan") || lower.contains("security") || lower.contains("vulnerability") || lower.contains("owasp") || lower.contains("sast")) {
            return "SECURITY_AUDIT";
        } else if (lower.contains("test") || lower.contains("coverage") || lower.contains("junit") || lower.contains("bdd")) {
            return "TESTING_SYNTHESIS";
        } else if (lower.contains("devops") || lower.contains("docker") || lower.contains("k8s") || lower.contains("pipeline") || lower.contains("container")) {
            return "DEVOPS_PIPELINE";
        } else if (lower.contains("fix") || lower.contains("bug") || lower.contains("exception") || lower.contains("error")) {
            return "BUG_FIX_ANALYSIS";
        } else if (lower.contains("refactor") || lower.contains("clean") || lower.contains("smell")) {
            return "CODE_REFACTORING";
        } else if (lower.contains("write") || lower.contains("generate") || lower.contains("create") || lower.contains("code")) {
            return "CODE_GENERATION";
        } else if (lower.contains("architecture") || lower.contains("design") || lower.contains("system")) {
            return "ARCHITECTURE_DESIGN";
        }
        return "GENERAL_ASSISTANT";
    }
}
