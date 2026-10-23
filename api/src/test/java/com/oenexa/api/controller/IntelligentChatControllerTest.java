package com.oenexa.api.controller;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;
import com.oenexa.domain.model.chat.ChatDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IntelligentChatController Zero-Mock Unit Tests")
class IntelligentChatControllerTest {

    private MemoryService memoryService;
    private MultiModelRouterService routerService;
    private RepositoryKnowledgeService repositoryKnowledgeService;
    private CollaborativeAgentService agentService;
    private ChatService chatService;
    private IntelligentConversationService intelligentConversationService;
    private IntelligentChatController controller;

    @BeforeEach
    void setUp() {
        memoryService = new MemoryService();
        routerService = new MultiModelRouterService();
        repositoryKnowledgeService = new RepositoryKnowledgeService();
        agentService = new CollaborativeAgentService();
        chatService = new ChatService();
        intelligentConversationService = new IntelligentConversationService(
                memoryService,
                routerService,
                repositoryKnowledgeService,
                agentService,
                chatService
        );
        controller = new IntelligentChatController(
                intelligentConversationService,
                memoryService,
                repositoryKnowledgeService,
                routerService,
                agentService
        );
    }

    @Test
    @DisplayName("Should process intelligent message and return 200 OK")
    void shouldProcessIntelligentMessage() {
        IntelligentChatRequest req = new IntelligentChatRequest("Explain system architecture and design", "conv-intel-1");
        ResponseEntity<IntelligentChatResponse> resp = controller.sendIntelligentMessage(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("conv-intel-1", resp.getBody().conversationId());
        assertTrue(resp.getBody().reply().contains("OENEXA Architecture"));
        assertNotNull(resp.getBody().thoughtProcess());
        assertNotNull(resp.getBody().contextSummary());
    }

    @Test
    @DisplayName("Should reject invalid intelligent message with 400 Bad Request")
    void shouldRejectInvalidIntelligentMessage() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.sendIntelligentMessage(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.sendIntelligentMessage(new IntelligentChatRequest(null, "c1")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.sendIntelligentMessage(new IntelligentChatRequest("   ", "c1")).getStatusCode());
    }

    @Test
    @DisplayName("Should stream tokens via SSE emitter")
    void shouldStreamTokens() {
        IntelligentChatRequest req = new IntelligentChatRequest("Hello AI Engineer", "conv-stream");
        SseEmitter emitter = controller.streamIntelligentMessage(req);
        assertNotNull(emitter);

        SseEmitter invalidEmitter = controller.streamIntelligentMessage(null);
        assertNotNull(invalidEmitter);

        SseEmitter blankEmitter = controller.streamIntelligentMessage(new IntelligentChatRequest("", "c1"));
        assertNotNull(blankEmitter);
    }

    @Test
    @DisplayName("Should record and retrieve user memories")
    void shouldManageUserMemories() {
        UserMemoryRecord record = new UserMemoryRecord("user-charlie", "PREFERRED_FRAMEWORK", "Spring Boot 3");
        ResponseEntity<UserMemoryRecord> postResp = controller.recordUserMemory(record);
        assertEquals(HttpStatus.OK, postResp.getStatusCode());
        assertEquals("Spring Boot 3", postResp.getBody().memoryValue());

        // Get all memories
        ResponseEntity<List<UserMemoryRecord>> getResp = controller.getUserMemories("user-charlie", null);
        assertEquals(HttpStatus.OK, getResp.getStatusCode());
        assertEquals(1, getResp.getBody().size());

        // Search memories with query
        ResponseEntity<List<UserMemoryRecord>> searchResp = controller.getUserMemories("user-charlie", "Spring");
        assertEquals(HttpStatus.OK, searchResp.getStatusCode());
        assertEquals(1, searchResp.getBody().size());

        // Invalid memory post
        assertEquals(HttpStatus.BAD_REQUEST, controller.recordUserMemory(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.recordUserMemory(new UserMemoryRecord(null, null, "val")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.recordUserMemory(new UserMemoryRecord("u1", null, "val")).getStatusCode());
    }

    @Test
    @DisplayName("Should index repository, fetch knowledge map, and search repository chunks")
    void shouldHandleRepositoryEndpoints() {
        String repoId = "repo-backend";
        var indexReq = new IntelligentChatController.IndexRepositoryRequest(repoId, Map.of(
                "src/Main.java", "package com.example;\npublic class Main {}"
        ));
        ResponseEntity<RepositoryKnowledgeMap> indexResp = controller.indexRepository(indexReq);
        assertEquals(HttpStatus.OK, indexResp.getStatusCode());
        assertEquals(repoId, indexResp.getBody().repositoryId());

        // Fetch knowledge map
        ResponseEntity<RepositoryKnowledgeMap> mapResp = controller.getKnowledgeMap(repoId);
        assertEquals(HttpStatus.OK, mapResp.getStatusCode());
        assertEquals(repoId, mapResp.getBody().repositoryId());

        // Search repository
        var searchReq = new IntelligentChatController.RepositorySearchRequest("Main class", 2);
        ResponseEntity<List<ProjectKnowledgeChunk>> searchResp = controller.searchRepository(repoId, searchReq);
        assertEquals(HttpStatus.OK, searchResp.getStatusCode());
        assertFalse(searchResp.getBody().isEmpty());

        // Search with null request
        ResponseEntity<List<ProjectKnowledgeChunk>> defaultSearch = controller.searchRepository(repoId, null);
        assertEquals(HttpStatus.OK, defaultSearch.getStatusCode());

        // Invalid index requests
        assertEquals(HttpStatus.BAD_REQUEST, controller.indexRepository(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.indexRepository(new IntelligentChatController.IndexRepositoryRequest(null, Map.of())).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.indexRepository(new IntelligentChatController.IndexRepositoryRequest("r1", null)).getStatusCode());
    }

    @Test
    @DisplayName("Should route models based on task and user preferences")
    void shouldRouteModels() {
        var req = new IntelligentChatController.ModelRouteRequest("Fix NPE bug in controller", "auto");
        ResponseEntity<ModelRoutingDecision> resp = controller.routeModel(req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(TaskType.BUG_FIX, resp.getBody().taskType());
        assertEquals(ModelProvider.OPENAI_GPT, resp.getBody().provider());

        // Null request uses defaults
        ResponseEntity<ModelRoutingDecision> defaultResp = controller.routeModel(null);
        assertEquals(HttpStatus.OK, defaultResp.getStatusCode());
        assertEquals(TaskType.FAST_CHAT, defaultResp.getBody().taskType());
    }

    @Test
    @DisplayName("Should run collaborative multi-agent pipeline and fetch status")
    void shouldRunAgentCollaboration() {
        var req = new IntelligentChatController.AgentCollaborationRequest("pipe-api-1", "Build User Service");
        ResponseEntity<List<AgentCollaborationTask>> resp = controller.collaborate(req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(6, resp.getBody().size());

        // Fetch pipeline tasks
        ResponseEntity<List<AgentCollaborationTask>> fetched = controller.getPipelineTasks("pipe-api-1");
        assertEquals(HttpStatus.OK, fetched.getStatusCode());
        assertEquals(6, fetched.getBody().size());

        // Invalid collaboration requests
        assertEquals(HttpStatus.BAD_REQUEST, controller.collaborate(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.collaborate(new IntelligentChatController.AgentCollaborationRequest("p1", null)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.collaborate(new IntelligentChatController.AgentCollaborationRequest("p1", "   ")).getStatusCode());
    }
}




