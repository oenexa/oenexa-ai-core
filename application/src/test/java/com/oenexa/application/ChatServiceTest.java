package com.oenexa.application;

import com.oenexa.domain.ChatDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatService Zero-Mock Unit Tests")
class ChatServiceTest {

    private final ChatService chatService = new ChatService();

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
}
