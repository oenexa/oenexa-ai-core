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
}
