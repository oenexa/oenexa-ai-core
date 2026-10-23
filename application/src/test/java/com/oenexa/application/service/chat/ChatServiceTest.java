package com.oenexa.application.service.chat;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.chat.ChatDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatService Zero-Mock Unit Tests")
class ChatServiceTest {

    private final ChatService chatService = new ChatService(new ChatResponseSynthesisEngine(), new LocalOllamaClient("offline://local", null));

    @ParameterizedTest(name = "Should generate architecture response for prompt: {0}")
    @ValueSource(strings = {"Tell me about the system architecture", "Show me the service design"})
    void shouldRespondToArchitectureQuestions(String prompt) {
        ChatResponse resp = chatService.processMessage(null, prompt);

        assertNotNull(resp);
        assertNotNull(resp.conversationId());
        assertTrue(resp.reply().contains("OENEXA Architecture: Polyrepo / Hexagonal"));
        assertTrue(resp.contextTokensUsed() > 0);

        List<Message> history = chatService.getHistory(resp.conversationId());
        assertEquals(2, history.size());
        assertEquals(Role.USER, history.get(0).role());
        assertEquals(prompt, history.get(0).content());
        assertEquals(Role.ASSISTANT, history.get(1).role());
        assertEquals(resp.reply(), history.get(1).content());
    }

    @ParameterizedTest(name = "Should generate security response for prompt: {0}")
    @ValueSource(strings = {"Please scan the dependencies", "Perform security check"})
    void shouldRespondToSecurityQuestions(String prompt) {
        ChatResponse resp = chatService.processMessage("conv-sec-1", prompt);

        assertNotNull(resp);
        assertEquals("conv-sec-1", resp.conversationId());
        assertTrue(resp.reply().contains("Security Agent activated: Scanning for OWASP"));

        List<Message> history = chatService.getHistory("conv-sec-1");
        assertFalse(history.isEmpty());
    }

    @ParameterizedTest(name = "Should generate bug fix response for prompt: {0}")
    @ValueSource(strings = {"Please fix the NullPointerException", "There is a bug in the code"})
    void shouldRespondToBugFixQuestions(String prompt) {
        ChatResponse resp = chatService.processMessage("conv-fix-1", prompt);

        assertNotNull(resp);
        assertEquals("conv-fix-1", resp.conversationId());
        assertTrue(resp.reply().contains("Bug Fix Agent activated: Analyzing stack trace"));
    }

    @Test
    @DisplayName("Should generate default engineering assistant response for generic questions")
    void shouldRespondToGenericQuestions() {
        ChatResponse resp = chatService.processMessage("   ", "Hello world");

        assertNotNull(resp);
        assertFalse(resp.conversationId().isBlank());
        assertTrue(resp.reply().contains("OENEXA AI Engineer: Request received"));
        assertEquals("GENERAL_ASSISTANT", resp.intent());
        assertNotNull(resp.thoughtProcess());
        assertFalse(resp.suggestedFollowUps().isEmpty());
    }

    @Test
    @DisplayName("Should respond to refactoring questions")
    void shouldRespondToRefactoringQuestions() {
        ChatResponse resp = chatService.processMessage("conv-refactor", "Please refactor clean code smells");
        assertNotNull(resp);
        assertEquals("CODE_REFACTORING", resp.intent());
        assertTrue(resp.reply().contains("Refactoring Engine Activated"));
        assertTrue(resp.thoughtProcess().contains("Code Smell Refactoring"));
        assertFalse(resp.suggestedFollowUps().isEmpty());
    }

    @Test
    @DisplayName("Should respond to devops questions")
    void shouldRespondToDevOpsQuestions() {
        ChatResponse resp = chatService.processMessage("conv-devops", "Setup devops docker pipeline");
        assertNotNull(resp);
        assertEquals("DEVOPS_PIPELINE", resp.intent());
        assertTrue(resp.reply().contains("DevOps & CI/CD Engine Activated"));
        assertTrue(resp.thoughtProcess().contains("DevOps & Containerization"));
        assertFalse(resp.suggestedFollowUps().isEmpty());
    }

    @Test
    @DisplayName("Should respond to testing synthesis questions")
    void shouldRespondToTestingQuestions() {
        ChatResponse resp = chatService.processMessage("conv-test", "Generate test coverage with junit and bdd");
        assertNotNull(resp);
        assertEquals("TESTING_SYNTHESIS", resp.intent());
        assertTrue(resp.reply().contains("Test Synthesizer Activated"));
        assertTrue(resp.thoughtProcess().contains("Test Suite Generation"));
        assertFalse(resp.suggestedFollowUps().isEmpty());
    }

    @Test
    @DisplayName("Should respond to code generation questions")
    void shouldRespondToCodeGenerationQuestions() {
        ChatResponse resp = chatService.processMessage("conv-code", "Write and generate code for user record");
        assertNotNull(resp);
        assertEquals("CODE_GENERATION", resp.intent());
        assertTrue(resp.reply().contains("Code Synthesizer Activated"));
        assertTrue(resp.thoughtProcess().contains("Modern Code Generation"));
        assertFalse(resp.suggestedFollowUps().isEmpty());
    }

    @Test
    @DisplayName("Should support model selection and disabling thinking mode")
    void shouldSupportModelSelectionAndDisableThinking() {
        ChatResponse resp = chatService.processMessage("conv-custom", "Hello", "gemini-2.5-pro", false);
        assertNotNull(resp);
        assertEquals("gemini-2.5-pro", resp.model());
        assertNull(resp.thoughtProcess());

        ChatResponse respDefault = chatService.processMessage("conv-custom-2", "Hello", "  ", true);
        assertEquals("gemini-2.5-flash", respDefault.model());
        assertNotNull(respDefault.thoughtProcess());
    }

    @Test
    @DisplayName("Should return empty list for unknown conversation history")
    void shouldReturnEmptyHistoryForUnknownConversation() {
        List<Message> history = chatService.getHistory("non-existent-conv");
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    @DisplayName("Should throw NullPointerException when messageText is null")
    void shouldThrowNpeOnNullMessage() {
        assertThrows(NullPointerException.class, () -> chatService.processMessage("conv-1", null));
    }

    @Test
    @DisplayName("Should process message with code context attachments and extract artifacts")
    void shouldProcessMessageWithAttachmentsAndArtifacts() {
        var attachment = new CodeContextAttachment("src/Main.java", "java", 1, 10, "public class Main {}");
        ChatResponse resp = chatService.processMessage(
                "conv-attach",
                "Explain the architecture and design",
                "gemini-2.5-pro",
                true,
                List.of(attachment)
        );

        assertNotNull(resp);
        assertNotNull(resp.thoughtProcess());
        assertTrue(resp.thoughtProcess().contains("Evaluated Attached Context"));
        assertTrue(resp.thoughtProcess().contains("src/Main.java"));
        assertFalse(resp.toolCalls().isEmpty());
        assertFalse(resp.artifacts().isEmpty());
        assertEquals(0.98, resp.confidenceScore());

        // Verify conversation history has attachments and tools
        List<Message> history = chatService.getHistory("conv-attach");
        assertEquals(2, history.size());
        assertEquals(1, history.get(0).attachments().size());
        assertFalse(history.get(1).toolCalls().isEmpty());
        assertFalse(history.get(1).artifacts().isEmpty());
    }

    @Test
    @DisplayName("Should support conversation metadata, listing, forking, and clearing")
    void shouldSupportConversationManagement() {
        ConversationMetadata created = chatService.createConversation(
                "custom-conv-id", "tenant-alpha", "user-beta", "Project Blueprint", "gemini-2.5-pro"
        );
        assertEquals("custom-conv-id", created.conversationId());
        assertEquals("Project Blueprint", created.title());
        assertEquals("gemini-2.5-pro", created.model());

        // Process a message in this conversation
        chatService.processMessage("custom-conv-id", "Write code for user authentication");

        List<ConversationMetadata> list = chatService.listConversations("tenant-alpha");
        assertFalse(list.isEmpty());

        ConversationMetadata meta = chatService.getMetadata("custom-conv-id");
        assertNotNull(meta);
        assertEquals("custom-conv-id", meta.conversationId());
        assertTrue(meta.messageCount() >= 2);

        // Fork conversation
        ConversationMetadata forked = chatService.forkConversation("custom-conv-id", "forked-conv-id", 0);
        assertEquals("forked-conv-id", forked.conversationId());
        assertTrue(forked.title().contains("Fork of Project Blueprint"));
        List<Message> forkedHistory = chatService.getHistory("forked-conv-id");
        assertEquals(1, forkedHistory.size());

        // Fork without newId and index -1
        ConversationMetadata forkedDefault = chatService.forkConversation("custom-conv-id", null, -1);
        assertNotNull(forkedDefault.conversationId());

        // Clear history
        chatService.clearHistory("custom-conv-id");
        assertTrue(chatService.getHistory("custom-conv-id").isEmpty());

        // Clear unknown conversation is safe
        chatService.clearHistory("unknown-id");

        // getMetadata for unrecorded conversation creates default metadata
        ConversationMetadata freshMeta = chatService.getMetadata("brand-new-conversation");
        assertNotNull(freshMeta);
        assertEquals("brand-new-conversation", freshMeta.conversationId());
        assertEquals("New Conversation", freshMeta.title());
    }

    @Test
    @DisplayName("Should execute auto-chat real scenario autonomous steps across full lifecycle")
    void shouldExecuteAutoChatStepLifecycle() {
        assertThrows(NullPointerException.class, () -> chatService.autoChatStep(null));

        // Step 1: Initial empty history with goal -> Architecture
        AutoChatTurnRequest req1 = new AutoChatTurnRequest("auto-conv-1", null, "Reactive Microservice");
        ChatResponse resp1 = chatService.autoChatStep(req1);
        assertNotNull(resp1);
        assertEquals("ARCHITECTURE_DESIGN", resp1.intent());
        assertTrue(resp1.reply().contains("OENEXA Architecture"));

        // Step 2: Context code present, no tests yet -> Test Synthesis
        AutoChatTurnRequest req2 = new AutoChatTurnRequest("auto-conv-1", "public record User(UUID id) {}", "Reactive Microservice");
        ChatResponse resp2 = chatService.autoChatStep(req2);
        assertNotNull(resp2);
        assertEquals("TESTING_SYNTHESIS", resp2.intent());
        assertTrue(resp2.reply().contains("Test Synthesizer Activated"));

        // Step 3: Architecture and tests present, no security yet -> Security Audit
        AutoChatTurnRequest req3 = new AutoChatTurnRequest("auto-conv-1", null, "Reactive Microservice");
        ChatResponse resp3 = chatService.autoChatStep(req3);
        assertNotNull(resp3);
        assertEquals("SECURITY_AUDIT", resp3.intent());
        assertTrue(resp3.reply().contains("Security Agent activated"));

        // Step 4: Security present, no devops yet -> DevOps Pipeline
        AutoChatTurnRequest req4 = new AutoChatTurnRequest("auto-conv-1", null, "Reactive Microservice");
        ChatResponse resp4 = chatService.autoChatStep(req4);
        assertNotNull(resp4);
        assertEquals("DEVOPS_PIPELINE", resp4.intent());
        assertTrue(resp4.reply().contains("DevOps & CI/CD Engine Activated"));

        // Step 5: All phases present with goal -> Refactoring / Optimization
        AutoChatTurnRequest req5 = new AutoChatTurnRequest("auto-conv-1", null, "Reactive Microservice");
        ChatResponse resp5 = chatService.autoChatStep(req5);
        assertNotNull(resp5);
        assertEquals("CODE_REFACTORING", resp5.intent());
        assertTrue(resp5.reply().contains("Refactoring Engine Activated"));

        // Step 6: All phases present without goal -> General Refactoring
        AutoChatTurnRequest req6 = new AutoChatTurnRequest("auto-conv-1", null, null);
        ChatResponse resp6 = chatService.autoChatStep(req6);
        assertNotNull(resp6);
        assertEquals("CODE_REFACTORING", resp6.intent());

        // Step 7: Blank convId auto-generates UUID
        AutoChatTurnRequest req7 = new AutoChatTurnRequest("   ", null, "Fresh Goal");
        ChatResponse resp7 = chatService.autoChatStep(req7);
        assertNotNull(resp7);
        assertFalse(resp7.conversationId().isBlank());
    }

    @Test
    @DisplayName("Should process message using live LocalOllamaClient and parse thoughts and code blocks")
    void shouldProcessMessageUsingLiveLocalOllamaClient() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        server.createContext("/generate", exchange -> {
            String json = """
                    {
                      "model": "deepseek-r1:8b",
                      "response": "<think>Deliberating step by step on system architecture.</think>### Architecture Blueprint\\n```java\\npackage com.oenexa.workspace;\\npublic record EngineCore(String name) {}\\n```",
                      "done": true
                    }
                    """;
            byte[] bytes = json.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            LocalOllamaClient ollama = new LocalOllamaClient("http://localhost:" + port + "/", client);
            ChatService liveService = new ChatService(new ChatResponseSynthesisEngine(), ollama);

            ChatResponse resp = liveService.processMessage("live-conv-1", "Design engine core", "deepseek-r1:8b", true);

            assertNotNull(resp);
            assertEquals("deepseek-r1:8b", resp.model());
            assertTrue(resp.thoughtProcess().contains("Deliberating step by step on system architecture."));
            assertTrue(resp.reply().contains("EngineCore"));
            assertFalse(resp.artifacts().isEmpty());
            assertEquals("EngineCore", resp.artifacts().get(0).title());
        } finally {
            server.stop(0);
        }
    }
}




