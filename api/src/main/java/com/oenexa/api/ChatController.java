package com.oenexa.api;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.chat.ChatDtos.ChatTurnResponseDto;
import com.oenexa.api.dto.chat.ChatDtos.ConversationSummaryDto;
import com.oenexa.api.dto.chat.ChatDtos.SendMessageRequestDto;
import com.oenexa.api.mapper.ChatApiMapper;
import com.oenexa.application.ChatService;
import com.oenexa.application.ConversationManagementService;
import com.oenexa.application.DocumentIngestionService;
import com.oenexa.application.security.ChatSecurityPolicy;
import com.oenexa.application.validation.ChatValidator;
import com.oenexa.domain.ChatDomain.*;
import com.oenexa.domain.ConversationCoreDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * REST API for Contextual AI Chat, Enterprise Folders, Pinning, Sharing,
 * Reactions, Multi-Format Document Ingestion, and Progressive Streaming.
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    public record CreateConversationRequest(String title, String model) {}
    public record ForkConversationRequest(String newConversationId, Integer upToMessageIndex) {}
    public record PinRequest(boolean pinned) {}
    public record ParseAttachmentRequest(String fileName, String base64Content) {}

    private final ChatService chatService;
    private final ConversationManagementService conversationManagementService;
    private final ChatValidator chatValidator;
    private final ChatSecurityPolicy chatSecurityPolicy;
    private final ChatApiMapper chatApiMapper;

    public ChatController(ChatService chatService) {
        this(chatService, new ConversationManagementService(chatService, new DocumentIngestionService()),
                new ChatValidator(), new ChatSecurityPolicy(), new ChatApiMapper());
    }

    public ChatController(ChatService chatService, ConversationManagementService conversationManagementService) {
        this(chatService, conversationManagementService,
                new ChatValidator(), new ChatSecurityPolicy(), new ChatApiMapper());
    }

    @Autowired
    public ChatController(
            ChatService chatService,
            ConversationManagementService conversationManagementService,
            ChatValidator chatValidator,
            ChatSecurityPolicy chatSecurityPolicy,
            ChatApiMapper chatApiMapper
    ) {
        this.chatService = Objects.requireNonNull(chatService, "chatService must not be null");
        this.conversationManagementService = Objects.requireNonNull(conversationManagementService, "conversationManagementService must not be null");
        this.chatValidator = chatValidator != null ? chatValidator : new ChatValidator();
        this.chatSecurityPolicy = chatSecurityPolicy != null ? chatSecurityPolicy : new ChatSecurityPolicy();
        this.chatApiMapper = chatApiMapper != null ? chatApiMapper : new ChatApiMapper();
    }

    public ChatValidator getChatValidator() {
        return chatValidator;
    }

    public ChatSecurityPolicy getChatSecurityPolicy() {
        return chatSecurityPolicy;
    }

    public ChatApiMapper getChatApiMapper() {
        return chatApiMapper;
    }

    @PostMapping("/v2/messages")
    public ResponseEntity<ApiResponse<ChatTurnResponseDto>> sendMessageV2(@RequestBody SendMessageRequestDto requestDto) {
        if (requestDto == null || requestDto.message() == null || requestDto.message().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        chatSecurityPolicy.verifyPromptIntegrity(requestDto.message());
        ChatRequest request = chatApiMapper.toDomainRequest(requestDto);
        chatValidator.validateChatRequest(request);
        ChatResponse response = chatService.processMessage(
                request.conversationId(),
                request.message(),
                request.model(),
                request.includeThinking(),
                request.attachments()
        );
        return ResponseEntity.ok(ApiResponse.ok("Message processed successfully", chatApiMapper.toTurnResponseDto(response)));
    }

    @GetMapping("/v2/conversations/{id}/summary")
    public ResponseEntity<ApiResponse<ConversationSummaryDto>> getConversationSummaryV2(@PathVariable String id) {
        ConversationMetadata metadata = chatService.getMetadata(id);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.ok("Conversation summary retrieved", chatApiMapper.toSummaryDto(metadata)));
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        chatSecurityPolicy.verifyPromptIntegrity(request.message());
        chatValidator.validateChatRequest(request);
        ChatResponse response = chatService.processMessage(
                request.conversationId(),
                request.message(),
                request.model(),
                request.includeThinking(),
                request.attachments()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(
            @RequestParam(required = false) String conversationId,
            @RequestParam String message,
            @RequestParam(defaultValue = "gemini-2.5-flash") String model
    ) {
        SseEmitter emitter = new SseEmitter(120_000L);
        emitter.onTimeout(() -> {
            try {
                emitter.send(SseEmitter.event().name("timeout").data(Map.of("code", "GATEWAY_TIMEOUT", "message", "Stream timed out after 120s")));
            } catch (Exception ignored) {}
            emitter.complete();
        });
        emitter.onError(ex -> emitter.complete());

        CompletableFuture.runAsync(() -> {
            try {
                conversationManagementService.streamProgressiveResponse(conversationId, message, model, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().name("chunk").data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(Map.of(
                            "code", "STREAM_ERROR",
                            "message", e.getMessage() != null ? e.getMessage() : "Stream execution failed"
                    )));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });
        return emitter;
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
        String title = (request != null && request.title() != null) ? request.title() : "New Conversation";
        String model = (request != null && request.model() != null) ? request.model() : "gemini-2.5-flash";
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

    @PutMapping("/conversations/{id}/pin")
    public ResponseEntity<ConversationMetadata> togglePin(
            @PathVariable String id,
            @RequestBody PinRequest request
    ) {
        return ResponseEntity.ok(conversationManagementService.togglePin(id, request.pinned()));
    }

    @PostMapping("/conversations/{id}/share")
    public ResponseEntity<ConversationShare> shareConversation(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId
    ) {
        return ResponseEntity.ok(conversationManagementService.shareConversation(id, userId, tenantId));
    }

    @GetMapping("/shared/{shareToken}")
    public ResponseEntity<SharedConversationView> getSharedConversation(@PathVariable String shareToken) {
        return ResponseEntity.ok(conversationManagementService.getSharedConversation(shareToken));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ConversationMetadata>> searchConversations(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "default-tenant") String tenantId
    ) {
        return ResponseEntity.ok(conversationManagementService.searchConversations(query, tenantId));
    }

    @GetMapping("/folders")
    public ResponseEntity<List<ChatFolder>> listFolders(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId,
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId
    ) {
        return ResponseEntity.ok(conversationManagementService.listFolders(tenantId, userId));
    }

    @PostMapping("/folders")
    public ResponseEntity<ChatFolder> createFolder(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default-tenant") String tenantId,
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @RequestBody CreateFolderRequest request
    ) {
        return ResponseEntity.ok(conversationManagementService.createFolder(tenantId, userId, request));
    }

    @DeleteMapping("/folders/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable String id) {
        conversationManagementService.deleteFolder(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/conversations/{id}/folder")
    public ResponseEntity<Map<String, String>> assignFolder(
            @PathVariable String id,
            @RequestBody AssignFolderRequest request
    ) {
        conversationManagementService.assignConversationToFolder(id, request.folderId());
        return ResponseEntity.ok(Map.of("conversationId", id, "folderId", request.folderId()));
    }

    @PostMapping("/conversations/{id}/messages/{index}/reactions")
    public ResponseEntity<MessageReaction> addReaction(
            @PathVariable String id,
            @PathVariable int index,
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @RequestBody MessageReactionRequest request
    ) {
        return ResponseEntity.ok(conversationManagementService.addReaction(id, index, userId, request.emoji()));
    }

    @GetMapping("/conversations/{id}/messages/{index}/reactions")
    public ResponseEntity<List<MessageReaction>> listReactions(
            @PathVariable String id,
            @PathVariable int index
    ) {
        return ResponseEntity.ok(conversationManagementService.listReactions(id, index));
    }

    @PostMapping("/attachments/parse")
    public ResponseEntity<ParsedDocument> parseAttachment(@RequestBody ParseAttachmentRequest request) {
        byte[] bytes = java.util.Base64.getDecoder().decode(request.base64Content());
        ParsedDocument parsed = conversationManagementService.parseUploadedAttachment(request.fileName(), bytes);
        return ResponseEntity.ok(parsed);
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
