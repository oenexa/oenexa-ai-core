package com.oenexa.application;

import com.oenexa.domain.ChatDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IntelligentConversationService Zero-Mock Unit Tests")
class IntelligentConversationServiceTest {

    private MemoryService memoryService;
    private MultiModelRouterService routerService;
    private RepositoryKnowledgeService repositoryKnowledgeService;
    private CollaborativeAgentService agentService;
    private ChatService chatService;
    private IntelligentConversationService service;

    @BeforeEach
    void setUp() {
        memoryService = new MemoryService();
        routerService = new MultiModelRouterService();
        repositoryKnowledgeService = new RepositoryKnowledgeService();
        agentService = new CollaborativeAgentService();
        chatService = new ChatService();
        service = new IntelligentConversationService(
                memoryService,
                routerService,
                repositoryKnowledgeService,
                agentService,
                chatService
        );
    }

    @Test
    @DisplayName("Should execute complete intelligent conversation flow")
    void shouldExecuteIntelligentConversationFlow() {
        // Index a sample repository
        String repoId = "repo-omega";
        repositoryKnowledgeService.indexRepository(repoId, Map.of(
                "src/main/AuthFilter.java", "package com.oenexa;\npublic class AuthFilter {\n  public boolean authenticate() { return true; }\n}"
        ));

        // Send a message expressing a preference and asking for code
        IntelligentChatRequest request = new IntelligentChatRequest(
                "We use PostgreSQL and I prefer hexagonal architecture. Write code for AuthFilter",
                "conv-101",
                "user-alex",
                repoId,
                "auto",
                true,
                List.of(new CodeContextAttachment("src/main/AuthFilter.java", "java", 1, 5, "public class AuthFilter {}"))
        );

        IntelligentChatResponse response = service.process(request);

        assertNotNull(response);
        assertEquals("conv-101", response.conversationId());
        assertNotNull(response.reply());
        assertNotNull(response.modelUsed());
        assertNotNull(response.provider());
        assertNotNull(response.taskType());
        assertNotNull(response.thoughtProcess());
        assertNotNull(response.contextSummary());
        assertTrue(response.rankingScore() >= 0.90);
        assertTrue(response.confidenceScore() >= 0.95);

        // Verify that long-term memory was updated
        List<UserMemoryRecord> userMemories = memoryService.getUserMemories("user-alex");
        assertFalse(userMemories.isEmpty());

        // Verify repository citations
        assertFalse(response.repositoryCitations().isEmpty());

        // Verify short-term memory contains 2 messages (user and assistant)
        List<Message> history = memoryService.getShortTermMemory("conv-101");
        assertEquals(2, history.size());
        assertEquals(Role.USER, history.get(0).role());
        assertEquals(Role.ASSISTANT, history.get(1).role());
    }

    @Test
    @DisplayName("Should stream tokens word-by-word with consumer callback")
    void shouldStreamTokensWordByWord() {
        IntelligentChatRequest request = new IntelligentChatRequest("Explain system context architecture", "conv-stream");
        List<String> collectedTokens = new ArrayList<>();

        service.streamTokens(request, collectedTokens::add);

        assertFalse(collectedTokens.isEmpty());
        String reconstructed = String.join("", collectedTokens);
        assertTrue(reconstructed.contains("OENEXA Architecture"));
    }

    @Test
    @DisplayName("Should enforce null safety checks")
    void shouldEnforceInvariants() {
        assertThrows(NullPointerException.class, () -> new IntelligentConversationService(null, routerService, repositoryKnowledgeService, agentService, chatService));
        assertThrows(NullPointerException.class, () -> new IntelligentConversationService(memoryService, null, repositoryKnowledgeService, agentService, chatService));
        assertThrows(NullPointerException.class, () -> new IntelligentConversationService(memoryService, routerService, null, agentService, chatService));
        assertThrows(NullPointerException.class, () -> new IntelligentConversationService(memoryService, routerService, repositoryKnowledgeService, null, chatService));
        assertThrows(NullPointerException.class, () -> new IntelligentConversationService(memoryService, routerService, repositoryKnowledgeService, agentService, null));
        assertThrows(NullPointerException.class, () -> service.process(null));
        assertThrows(NullPointerException.class, () -> service.streamTokens(null, s -> {}));
        assertThrows(NullPointerException.class, () -> service.streamTokens(new IntelligentChatRequest("hi", "c1"), null));
    }
}
