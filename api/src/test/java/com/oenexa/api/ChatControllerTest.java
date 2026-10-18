package com.oenexa.api;

import com.oenexa.application.ChatService;
import com.oenexa.domain.ChatDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatController Zero-Mock Unit Tests")
class ChatControllerTest {

    private final ChatService chatService = new ChatService();
    private final ChatController controller = new ChatController(chatService);

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
        // With explicit request
        AutoChatTurnRequest req = new AutoChatTurnRequest("conv-auto-endpoint", "public record Order() {}", "Build Order Microservice");
        ResponseEntity<ChatResponse> response = controller.autoChatTurn(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("conv-auto-endpoint", response.getBody().conversationId());
        assertEquals("TESTING_SYNTHESIS", response.getBody().intent());

        // With null request (uses default safe turn)
        ResponseEntity<ChatResponse> defaultResp = controller.autoChatTurn(null);
        assertEquals(HttpStatus.OK, defaultResp.getStatusCode());
        assertNotNull(defaultResp.getBody());
        assertEquals("ARCHITECTURE_DESIGN", defaultResp.getBody().intent());
    }
}

