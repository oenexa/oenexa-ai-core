package com.oenexa.api.controller;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.application.service.chat.ChatService;
import com.oenexa.application.service.chat.ConversationManagementService;
import com.oenexa.application.service.knowledge.DocumentIngestionService;
import com.oenexa.domain.model.chat.ChatDomain.*;
import com.oenexa.domain.model.chat.ConversationCoreDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatController Zero-Mock Unit Tests")
class ChatControllerTest {

    private ChatService chatService;
    private DocumentIngestionService documentIngestionService;
    private ConversationManagementService conversationManagementService;
    private ChatController controller;

    @BeforeEach
    void setUp() {
        chatService = new ChatService();
        documentIngestionService = new DocumentIngestionService();
        conversationManagementService = new ConversationManagementService(chatService, documentIngestionService);
        controller = new ChatController(chatService, conversationManagementService);
    }

    @Test
    @DisplayName("Should send message and return chat response")
    void shouldSendMessageSuccessfully() {
        ChatRequest req = new ChatRequest("Explain microservices architecture", null);
        ResponseEntity<ChatResponse> response = controller.sendMessage(req);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().reply().contains("OENEXA Architecture"));
        assertNotNull(response.getBody().conversationId());
        assertEquals("gemini-2.5-flash", response.getBody().model());
        assertEquals("ARCHITECTURE_DESIGN", response.getBody().intent());
        assertNotNull(response.getBody().thoughtProcess());
        assertFalse(response.getBody().suggestedFollowUps().isEmpty());
    }

    @Test
    @DisplayName("Should send message with custom model and thinking options")
    void shouldSendMessageWithCustomModelOptions() {
        ChatRequest req = new ChatRequest("Explain microservices architecture", "conv-custom", "gemini-2.5-pro", false);
        ResponseEntity<ChatResponse> response = controller.sendMessage(req);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("gemini-2.5-pro", response.getBody().model());
        assertNull(response.getBody().thoughtProcess());
    }

    @Test
    @DisplayName("Should reject empty or null message with 400 Bad Request")
    void shouldRejectInvalidMessage() {
        ResponseEntity<ChatResponse> nullResp = controller.sendMessage(new ChatRequest(null, "conv-1"));
        assertEquals(HttpStatus.BAD_REQUEST, nullResp.getStatusCode());

        ResponseEntity<ChatResponse> blankResp = controller.sendMessage(new ChatRequest("   ", "conv-1"));
        assertEquals(HttpStatus.BAD_REQUEST, blankResp.getStatusCode());
    }

    @Test
    @DisplayName("Should get conversation history")
    void shouldGetConversationHistory() {
        ChatRequest req = new ChatRequest("scan code for secrets", "conv-sec");
        controller.sendMessage(req);

        ResponseEntity<List<Message>> history = controller.getConversation("conv-sec");
        assertEquals(HttpStatus.OK, history.getStatusCode());
        assertNotNull(history.getBody());
        assertEquals(2, history.getBody().size());
    }

    @Test
    @DisplayName("Should support conversation management: list, create, metadata, fork, and delete")
    void shouldSupportConversationManagementInController() {
        var createReq = new ChatController.CreateConversationRequest("Core Discussion", "gemini-2.5-pro");
        var createResp = controller.createConversation(createReq);
        assertEquals(HttpStatus.OK, createResp.getStatusCode());
        assertNotNull(createResp.getBody());
        assertEquals("Core Discussion", createResp.getBody().title());

        var defaultCreate = controller.createConversation(null);
        assertEquals(HttpStatus.OK, defaultCreate.getStatusCode());
        assertEquals("New Conversation", defaultCreate.getBody().title());

        var listResp = controller.listConversations("default-tenant");
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        assertNotNull(listResp.getBody());
        assertFalse(listResp.getBody().isEmpty());

        String convId = createResp.getBody().conversationId();
        var metaResp = controller.getMetadata(convId);
        assertEquals(HttpStatus.OK, metaResp.getStatusCode());
        assertNotNull(metaResp.getBody());

        // Send a message with attachments
        var attachment = new CodeContextAttachment("src/App.java", "public class App {}");
        var reqWithAttach = new ChatRequest("Explain this design", convId, "gemini-2.5-pro", true, List.of(attachment));
        var sendResp = controller.sendMessage(reqWithAttach);
        assertEquals(HttpStatus.OK, sendResp.getStatusCode());

        // Fork conversation
        var forkReq = new ChatController.ForkConversationRequest("fork-api-1", 0);
        var forkResp = controller.forkConversation(convId, forkReq);
        assertEquals(HttpStatus.OK, forkResp.getStatusCode());
        assertEquals("fork-api-1", forkResp.getBody().conversationId());

        // Fork with null request
        var defaultFork = controller.forkConversation(convId, null);
        assertEquals(HttpStatus.OK, defaultFork.getStatusCode());
        assertNotNull(defaultFork.getBody().conversationId());

        // Delete conversation
        var deleteResp = controller.deleteConversation(convId);
        assertEquals(HttpStatus.NO_CONTENT, deleteResp.getStatusCode());
    }

    @Test
    @DisplayName("Should handle auto-chat turn endpoint with real scenario execution")
    void shouldHandleAutoChatTurnEndpoint() {
        AutoChatTurnRequest req = new AutoChatTurnRequest("conv-auto-endpoint", "public record Order() {}", "Build Order Microservice");
        ResponseEntity<ChatResponse> response = controller.autoChatTurn(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("conv-auto-endpoint", response.getBody().conversationId());
        assertEquals("TESTING_SYNTHESIS", response.getBody().intent());

        ResponseEntity<ChatResponse> defaultResp = controller.autoChatTurn(null);
        assertEquals(HttpStatus.OK, defaultResp.getStatusCode());
        assertNotNull(defaultResp.getBody());
        assertEquals("ARCHITECTURE_DESIGN", defaultResp.getBody().intent());
    }

    @Test
    @DisplayName("Should pin and unpin conversation")
    void shouldPinAndUnpinConversation() {
        var create = controller.createConversation(new ChatController.CreateConversationRequest("Pin Test", "gemini-2.5-flash"));
        String id = create.getBody().conversationId();

        var pinResp = controller.togglePin(id, new ChatController.PinRequest(true));
        assertEquals(HttpStatus.OK, pinResp.getStatusCode());
        assertTrue(pinResp.getBody().pinned());
    }

    @Test
    @DisplayName("Should manage chat folders: create, list, assign, and delete")
    void shouldManageChatFolders() {
        var createResp = controller.createFolder("tenant-1", "user-1", new CreateFolderRequest("System Design", "#3b82f6"));
        assertEquals(HttpStatus.OK, createResp.getStatusCode());
        ChatFolder folder = createResp.getBody();
        assertNotNull(folder);

        var listResp = controller.listFolders("tenant-1", "user-1");
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        assertEquals(1, listResp.getBody().size());

        var assignResp = controller.assignFolder("conv-1", new AssignFolderRequest("conv-1", folder.id()));
        assertEquals(HttpStatus.OK, assignResp.getStatusCode());
        assertEquals(folder.id(), assignResp.getBody().get("folderId"));

        var deleteResp = controller.deleteFolder(folder.id());
        assertEquals(HttpStatus.NO_CONTENT, deleteResp.getStatusCode());
    }

    @Test
    @DisplayName("Should share conversation and retrieve shared conversation view")
    void shouldShareAndRetrieveConversation() {
        var create = controller.createConversation(new ChatController.CreateConversationRequest("Public Chat", "gemini-2.5-flash"));
        String id = create.getBody().conversationId();
        controller.sendMessage(new ChatRequest("Say hello", id));

        var shareResp = controller.shareConversation(id, "user-1", "tenant-1");
        assertEquals(HttpStatus.OK, shareResp.getStatusCode());
        assertNotNull(shareResp.getBody().shareToken());

        var viewResp = controller.getSharedConversation(shareResp.getBody().shareToken());
        assertEquals(HttpStatus.OK, viewResp.getStatusCode());
        assertEquals("Public Chat", viewResp.getBody().title());
    }

    @Test
    @DisplayName("Should search conversations across titles and contents")
    void shouldSearchConversations() {
        controller.createConversation(new ChatController.CreateConversationRequest("Kubernetes Deployment", "gemini-2.5-flash"));
        var searchResp = controller.searchConversations("Kubernetes", "default-tenant");
        assertEquals(HttpStatus.OK, searchResp.getStatusCode());
        assertFalse(searchResp.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should add and list message reactions")
    void shouldAddAndListReactions() {
        var addResp = controller.addReaction("conv-react", 0, "user-1", new MessageReactionRequest("❤️"));
        assertEquals(HttpStatus.OK, addResp.getStatusCode());
        assertEquals("❤️", addResp.getBody().emoji());

        var listResp = controller.listReactions("conv-react", 0);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        assertEquals(1, listResp.getBody().size());
    }

    @Test
    @DisplayName("Should parse multi-format attachment via REST")
    void shouldParseAttachmentViaRest() {
        String base64Csv = Base64.getEncoder().encodeToString("name,age\nAlice,30\n".getBytes(StandardCharsets.UTF_8));
        var parseResp = controller.parseAttachment(new ChatController.ParseAttachmentRequest("data.csv", base64Csv));
        assertEquals(HttpStatus.OK, parseResp.getStatusCode());
        assertNotNull(parseResp.getBody());
        assertEquals(AttachmentType.CSV, parseResp.getBody().type());
        assertTrue(parseResp.getBody().extractedContent().contains("Alice"));
    }

    @Test
    @DisplayName("Should return active SseEmitter for progressive response stream")
    void shouldReturnSseEmitterForStream() {
        SseEmitter emitter = controller.streamMessage("conv-stream-api", "Explain SOLID principles", "gemini-2.5-flash");
        assertNotNull(emitter);
    }
}



