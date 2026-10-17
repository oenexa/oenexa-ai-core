package com.oenexa.application;

import com.oenexa.domain.ChatDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MemoryService Zero-Mock Unit Tests")
class MemoryServiceTest {

    private final MemoryService memoryService = new MemoryService();

    @Test
    @DisplayName("Should maintain short-term memory sliding window up to 50 messages")
    void shouldMaintainSlidingWindow() {
        String sessionId = "sess-window-1";

        for (int i = 0; i < 60; i++) {
            Message msg = new Message(
                    Role.USER,
                    "Message " + i,
                    Instant.now(),
                    null,
                    "claude-3-5-sonnet",
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
            memoryService.addMessageToShortTerm(sessionId, msg);
        }

        List<Message> memory = memoryService.getShortTermMemory(sessionId);
        assertEquals(50, memory.size());
        assertEquals("Message 10", memory.get(0).content());
        assertEquals("Message 59", memory.get(49).content());

        List<String> snippets = memoryService.getShortTermMemorySnippets(sessionId);
        assertEquals(5, snippets.size());
        assertTrue(snippets.get(4).contains("Message 59"));
    }

    @Test
    @DisplayName("Should handle empty short term memory safely")
    void shouldHandleEmptyShortTermMemory() {
        List<Message> memory = memoryService.getShortTermMemory("unknown-session");
        assertTrue(memory.isEmpty());

        List<String> snippets = memoryService.getShortTermMemorySnippets("unknown-session");
        assertTrue(snippets.isEmpty());

        String summary = memoryService.generateContextSummary("unknown-session");
        assertNotNull(summary);
    }

    @Test
    @DisplayName("Should generate context summary when turns exceed threshold")
    void shouldGenerateContextSummary() {
        String sessionId = "sess-summary-1";
        for (int i = 0; i < 6; i++) {
            memoryService.addMessageToShortTerm(
                    sessionId,
                    new Message(Role.USER, "Turn " + i + " content details", Instant.now(), null, "gpt-4o", List.of(), List.of(), List.of(), List.of())
            );
        }

        String summary = memoryService.generateContextSummary(sessionId);
        assertNotNull(summary);
        assertTrue(summary.contains("Summary of 6 turns"));
    }

    @Test
    @DisplayName("Should learn user preferences, architectural choices, and facts from user turn")
    void shouldLearnFromUserTurn() {
        String userId = "user-alice";

        Optional<UserMemoryRecord> learnedPref = memoryService.learnFromUserTurn(userId, "I prefer hexagonal architecture in my services");
        assertTrue(learnedPref.isPresent());
        assertEquals("ARCHITECTURE_STYLE", learnedPref.get().memoryKey());

        Optional<UserMemoryRecord> learnedDb = memoryService.learnFromUserTurn(userId, "We use PostgreSQL and Redis for our database persistence");
        assertTrue(learnedDb.isPresent());
        assertEquals("PERSISTENCE_STACK", learnedDb.get().memoryKey());

        Optional<UserMemoryRecord> learnedNone = memoryService.learnFromUserTurn(userId, "Can you review this code?");
        assertTrue(learnedNone.isEmpty());

        List<UserMemoryRecord> all = memoryService.getUserMemories(userId);
        assertEquals(2, all.size());

        List<UserMemoryRecord> searchArch = memoryService.searchUserMemories(userId, "hexagonal");
        assertEquals(1, searchArch.size());
        assertEquals("ARCHITECTURE_STYLE", searchArch.get(0).memoryKey());

        List<UserMemoryRecord> searchAll = memoryService.searchUserMemories(userId, "");
        assertEquals(2, searchAll.size());
    }

    @Test
    @DisplayName("Should generate concise session titles")
    void shouldGenerateSessionTitle() {
        assertEquals("New AI Conversation", memoryService.generateSessionTitle(null));
        assertEquals("New AI Conversation", memoryService.generateSessionTitle("   "));
        assertEquals("Build Microservice", memoryService.generateSessionTitle("Build Microservice"));
        assertEquals("How do I implement a secure authentic...",
                memoryService.generateSessionTitle("How do I implement a secure authentication filter in Spring Boot 3?"));
    }

    @Test
    @DisplayName("Should categorize conversations based on topic")
    void shouldCategorizeConversations() {
        assertEquals(ConversationCategory.ARCHITECTURE, memoryService.categorizeConversation("Explain the hexagonal architecture monolith"));
        assertEquals(ConversationCategory.BUG_FIXING, memoryService.categorizeConversation("Fix NPE bug in controller"));
        assertEquals(ConversationCategory.TESTING, memoryService.categorizeConversation("Generate zero-mock junit test coverage"));
        assertEquals(ConversationCategory.DEVOPS, memoryService.categorizeConversation("Configure docker k8s deployment"));
        assertEquals(ConversationCategory.SECURITY, memoryService.categorizeConversation("Run SAST scan for OWASP cve vulnerabilities"));
        assertEquals(ConversationCategory.CODE_ENGINEERING, memoryService.categorizeConversation("Implement new rest api service and controller"));
        assertEquals(ConversationCategory.GENERAL, memoryService.categorizeConversation("Hello, who are you?"));
        assertEquals(ConversationCategory.GENERAL, memoryService.categorizeConversation(null));
    }

    @Test
    @DisplayName("Should throw NPE on null arguments for addMessageToShortTerm and recordUserMemory")
    void shouldEnforceInvariants() {
        assertThrows(NullPointerException.class, () -> memoryService.addMessageToShortTerm(null, null));
        assertThrows(NullPointerException.class, () -> memoryService.recordUserMemory(null));
    }
}
