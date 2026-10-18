package com.oenexa.api;

import com.oenexa.application.ChatService;
import com.oenexa.domain.ChatDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Contextual AI Chat & Assistance.
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    public record CreateConversationRequest(String title, String model) {}
    public record ForkConversationRequest(String newConversationId, Integer upToMessageIndex) {}

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        ChatResponse response = chatService.processMessage(
                request.conversationId(),
                request.message(),
                request.model(),
                request.includeThinking(),
                request.attachments()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auto-turn")
    public ResponseEntity<ChatResponse> autoChatTurn(@RequestBody(required = false) AutoChatTurnRequest request) {
        AutoChatTurnRequest safeReq = (request != null)
                ? request
                : new AutoChatTurnRequest(null, null, "Architect autonomous software engineering solution");
        return ResponseEntity.ok(chatService.autoChatStep(safeReq));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationMetadata>> listConversations(@RequestParam(defaultValue = "default-tenant") String tenantId) {
        return ResponseEntity.ok(chatService.listConversations(tenantId));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationMetadata> createConversation(@RequestBody(required = false) CreateConversationRequest request) {
        String title = (request != null) ? request.title() : "New Conversation";
        String model = (request != null) ? request.model() : "gemini-2.5-flash";
        return ResponseEntity.ok(chatService.createConversation(null, "default-tenant", "default-user", title, model));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<List<Message>> getConversation(@PathVariable String id) {
        return ResponseEntity.ok(chatService.getHistory(id));
    }

    @GetMapping("/conversations/{id}/metadata")
    public ResponseEntity<ConversationMetadata> getMetadata(@PathVariable String id) {
        return ResponseEntity.ok(chatService.getMetadata(id));
    }

    @PostMapping("/conversations/{id}/fork")
    public ResponseEntity<ConversationMetadata> forkConversation(
            @PathVariable String id,
            @RequestBody(required = false) ForkConversationRequest request
    ) {
        String newId = (request != null && request.newConversationId() != null) ? request.newConversationId() : java.util.UUID.randomUUID().toString();
        int index = (request != null && request.upToMessageIndex() != null) ? request.upToMessageIndex() : -1;
        return ResponseEntity.ok(chatService.forkConversation(id, newId, index));
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String id) {
        chatService.clearHistory(id);
        return ResponseEntity.noContent().build();
    }
}

