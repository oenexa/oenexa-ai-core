package com.oenexa.application.validation;

import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import com.oenexa.domain.model.knowledge.RagDocument.DocumentInput;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Validation Layer for Knowledge & RAG Domain.
 * Validates document titles, content payloads, and chunking parameters.
 */
@Component
public class KnowledgeValidator {

    public void validateDocumentInput(DocumentInput input) {
        if (input == null) {
            throw new OenexaValidationException("Document input must not be null");
        }
        List<String> violations = new ArrayList<>();
        if (input.title() == null || input.title().trim().isBlank()) {
            violations.add("title must not be blank");
        }
        if (input.content() == null || input.content().trim().isBlank()) {
            violations.add("content must not be blank");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Document input validation failed", violations);
        }
    }

    public void validateKnowledgeBaseCreation(String name, String tenantId) {
        List<String> violations = new ArrayList<>();
        if (name == null || name.trim().isBlank()) {
            violations.add("knowledge base name must not be blank");
        }
        if (tenantId == null || tenantId.trim().isBlank()) {
            violations.add("tenantId must not be blank");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Knowledge base creation validation failed", violations);
        }
    }
}

