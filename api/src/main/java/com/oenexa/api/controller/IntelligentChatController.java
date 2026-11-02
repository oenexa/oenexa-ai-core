package com.oenexa.api.controller;

import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;
import com.oenexa.domain.model.chat.ChatDomain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Enterprise Intelligent Chat & Multi-Agent Collaboration REST & SSE Controller.
 * Exposes endpoints for Phase 2 Intelligent Chat and Phase 3 Repository Understanding.
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(originPatterns = "*")
public class IntelligentChatController {

    public record IndexRepositoryRequest(String repositoryId, Map<String, String> files) {}
    public record RepositorySearchRequest(String query, Integer topK) {}
    public record ModelRouteRequest(String message, String preferredModel) {}
    public record AgentCollaborationRequest(String pipelineId, String goal) {}

    private final IntelligentConversationService intelligentConversationService;
    private final MemoryService memoryService;
    private final RepositoryKnowledgeService repositoryKnowledgeService;
    private final MultiModelRouterService routerService;
    private final CollaborativeAgentService agentService;

    public IntelligentChatController(
            IntelligentConversationService intelligentConversationService,
            MemoryService memoryService,
            RepositoryKnowledgeService repositoryKnowledgeService,
            MultiModelRouterService routerService,
            CollaborativeAgentService agentService
    ) {
        this.intelligentConversationService = Objects.requireNonNull(intelligentConversationService, "intelligentConversationService must not be null");
        this.memoryService = Objects.requireNonNull(memoryService, "memoryService must not be null");
        this.repositoryKnowledgeService = Objects.requireNonNull(repositoryKnowledgeService, "repositoryKnowledgeService must not be null");
        this.routerService = Objects.requireNonNull(routerService, "routerService must not be null");
        this.agentService = Objects.requireNonNull(agentService, "agentService must not be null");
    }

    @PostMapping("/chat/intelligent/message")
    public ResponseEntity<IntelligentChatResponse> sendIntelligentMessage(@RequestBody IntelligentChatRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        IntelligentChatResponse response = intelligentConversationService.process(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/chat/intelligent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamIntelligentMessage(@RequestBody IntelligentChatRequest request) {
        SseEmitter emitter = new SseEmitter(60_000L);
        if (request == null || request.message() == null || request.message().isBlank()) {
            emitter.completeWithError(new IllegalArgumentException("Message must not be empty"));
            return emitter;
        }

        try {
            intelligentConversationService.streamTokens(request, token -> {
                try {
                    emitter.send(SseEmitter.event().data(token));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            });
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @GetMapping("/chat/intelligent/memory/{userId}")
    public ResponseEntity<List<UserMemoryRecord>> getUserMemories(
            @PathVariable String userId,
            @RequestParam(required = false) String query
    ) {
        List<UserMemoryRecord> list = (query != null && !query.isBlank())
                ? memoryService.searchUserMemories(userId, query)
                : memoryService.getUserMemories(userId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/chat/intelligent/memory")
    public ResponseEntity<UserMemoryRecord> recordUserMemory(@RequestBody UserMemoryRecord record) {
        if (record == null || record.userId() == null || record.memoryKey() == null) {
            return ResponseEntity.badRequest().build();
        }
        memoryService.recordUserMemory(record);
        return ResponseEntity.ok(record);
    }

    @PostMapping("/repository/index")
    public ResponseEntity<RepositoryKnowledgeMap> indexRepository(@RequestBody IndexRepositoryRequest request) {
        if (request == null || request.repositoryId() == null || request.files() == null) {
            return ResponseEntity.badRequest().build();
        }
        RepositoryKnowledgeMap map = repositoryKnowledgeService.indexRepository(request.repositoryId(), request.files());
        return ResponseEntity.ok(map);
    }

    @GetMapping("/repository/{repositoryId}/knowledge-map")
    public ResponseEntity<RepositoryKnowledgeMap> getKnowledgeMap(@PathVariable String repositoryId) {
        return ResponseEntity.ok(repositoryKnowledgeService.getKnowledgeMap(repositoryId));
    }

    @PostMapping("/repository/{repositoryId}/search")
    public ResponseEntity<List<ProjectKnowledgeChunk>> searchRepository(
            @PathVariable String repositoryId,
            @RequestBody RepositorySearchRequest request
    ) {
        String query = (request != null && request.query() != null) ? request.query() : "";
        int topK = (request != null && request.topK() != null) ? request.topK() : 5;
        return ResponseEntity.ok(repositoryKnowledgeService.searchSimilarChunks(repositoryId, query, topK));
    }

    @PostMapping("/chat/intelligent/route")
    public ResponseEntity<ModelRoutingDecision> routeModel(@RequestBody ModelRouteRequest request) {
        String msg = (request != null) ? request.message() : "";
        String pref = (request != null) ? request.preferredModel() : "auto";
        return ResponseEntity.ok(routerService.routeMessage(msg, pref));
    }

    @PostMapping("/agents/collaborate")
    public ResponseEntity<List<AgentCollaborationTask>> collaborate(@RequestBody AgentCollaborationRequest request) {
        if (request == null || request.goal() == null || request.goal().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        List<AgentCollaborationTask> tasks = agentService.runCollaborativePipeline(request.pipelineId(), request.goal());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/agents/pipeline/{pipelineId}")
    public ResponseEntity<List<AgentCollaborationTask>> getPipelineTasks(@PathVariable String pipelineId) {
        return ResponseEntity.ok(agentService.getPipelineTasks(pipelineId));
    }
}



