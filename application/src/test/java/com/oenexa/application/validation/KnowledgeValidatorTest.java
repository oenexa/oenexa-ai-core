package com.oenexa.application.validation;

import com.oenexa.domain.OenexaExceptions.OenexaValidationException;
import com.oenexa.domain.RagDocument.DocumentInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("KnowledgeValidator Zero-Mock Unit Tests")
class KnowledgeValidatorTest {

    private final KnowledgeValidator validator = new KnowledgeValidator();

    @Test
    @DisplayName("Should accept valid document input and knowledge base creation")
    void shouldAcceptValidInputs() {
        DocumentInput doc = new DocumentInput("Clean Architecture Guide", "Hexagonal boundaries decouple core domain.", "repo-1");
        assertDoesNotThrow(() -> validator.validateDocumentInput(doc));
        assertDoesNotThrow(() -> validator.validateKnowledgeBaseCreation("Engineering Docs", "tenant-100"));
    }

    @Test
    @DisplayName("Should reject blank document input attributes")
    void shouldRejectInvalidDocumentInput() {
        assertThrows(OenexaValidationException.class, () -> validator.validateDocumentInput(null));
        assertThrows(OenexaValidationException.class, () -> validator.validateDocumentInput(new DocumentInput("", "content", "repo-1")));
        assertThrows(OenexaValidationException.class, () -> validator.validateDocumentInput(new DocumentInput("Title", "", "repo-1")));
    }

    @Test
    @DisplayName("Should reject blank knowledge base creation parameters")
    void shouldRejectInvalidKnowledgeBaseCreation() {
        assertThrows(OenexaValidationException.class, () -> validator.validateKnowledgeBaseCreation("", "tenant-1"));
        assertThrows(OenexaValidationException.class, () -> validator.validateKnowledgeBaseCreation("Name", ""));
    }
}
