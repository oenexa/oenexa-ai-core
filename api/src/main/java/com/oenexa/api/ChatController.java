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
                request.includeThinking()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<List<Message>> getConversation(@PathVariable String id) {
        return ResponseEntity.ok(chatService.getHistory(id));
    }
}
