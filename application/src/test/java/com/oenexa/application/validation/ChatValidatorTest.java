package com.oenexa.application.validation;

import com.oenexa.domain.ChatDomain.ChatRequest;
import com.oenexa.domain.OenexaExceptions.OenexaValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatValidator Zero-Mock Unit Tests")
class ChatValidatorTest {

    private final ChatValidator validator = new ChatValidator();

    @Test
    @DisplayName("Should accept valid chat request")
    void shouldAcceptValidChatRequest() {
        ChatRequest req = new ChatRequest("Explain Hexagonal Architecture", "conv-123");
        assertDoesNotThrow(() -> validator.validateChatRequest(req));
    }

    @Test
    @DisplayName("Should reject null or blank chat message")
    void shouldRejectBlankChatMessage() {
        assertThrows(OenexaValidationException.class, () -> validator.validateChatRequest(null));
        assertThrows(OenexaValidationException.class, () -> validator.validateChatRequest(new ChatRequest("", "c1")));
        assertThrows(OenexaValidationException.class, () -> validator.validateChatRequest(new ChatRequest("   ", "c1")));
    }

    @Test
    @DisplayName("Should reject overly long conversation ID")
    void shouldRejectLongConversationId() {
        String longId = "a".repeat(150);
        ChatRequest req = new ChatRequest("Hello Oenexa", longId);
        assertThrows(OenexaValidationException.class, () -> validator.validateChatRequest(req));
    }
}
