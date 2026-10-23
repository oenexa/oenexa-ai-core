package com.oenexa.application.validation;

import com.oenexa.domain.model.chat.ChatDomain.ChatRequest;
import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Validation Layer for Chat & Conversation Domain.
 * Enforces non-blank message prompts, maximum context token boundaries, and conversation IDs.
 */
@Component
public class ChatValidator {

    private static final int MAX_PROMPT_LENGTH = 32_000;

    public void validateChatRequest(ChatRequest request) {
        if (request == null) {
            throw new OenexaValidationException("Chat request payload must not be null");
        }
        List<String> violations = new ArrayList<>();

        if (request.message() == null || request.message().trim().isBlank()) {
            violations.add("message content must not be blank");
        } else if (request.message().length() > MAX_PROMPT_LENGTH) {
            violations.add("message length exceeds maximum supported limit of " + MAX_PROMPT_LENGTH + " characters");
        }

        if (request.conversationId() != null && request.conversationId().length() > 128) {
            violations.add("conversationId must not exceed 128 characters");
        }

        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Chat request validation failed", violations);
        }
    }
}

