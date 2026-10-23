package com.oenexa.domain.exception;

import com.oenexa.domain.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Exceptions Zero-Mock Unit Tests")
class DomainExceptionsTest {

    @Test
    @DisplayName("AuthDomainException initializes with ErrorCode.UNAUTHORIZED and defaults")
    void testAuthDomainException() {
        AuthDomainException ex = new AuthDomainException("Invalid token");
        assertEquals(ErrorCode.UNAUTHORIZED, ex.code());
        assertEquals("Invalid token", ex.getMessage());
        assertNotNull(ex.remediation());
        assertNotNull(ex.traceId());

        AuthDomainException customEx = new AuthDomainException(ErrorCode.FORBIDDEN, "Forbidden account");
        assertEquals(ErrorCode.FORBIDDEN, customEx.code());
    }

    @Test
    @DisplayName("ChatDomainException initializes with ErrorCode.BAD_REQUEST")
    void testChatDomainException() {
        ChatDomainException ex = new ChatDomainException("Prompt injection detected", "Remove unsafe prompt overrides");
        assertEquals(ErrorCode.BAD_REQUEST, ex.code());
        assertEquals("Prompt injection detected", ex.getMessage());
        assertEquals("Remove unsafe prompt overrides", ex.remediation());
    }

    @Test
    @DisplayName("AgentGoalDomainException initializes with ErrorCode.COGNITIVE_FAULT")
    void testAgentGoalDomainException() {
        AgentGoalDomainException ex = new AgentGoalDomainException("Cognitive decomposition failed");
        assertEquals(ErrorCode.COGNITIVE_FAULT, ex.code());
        assertEquals("Cognitive decomposition failed", ex.getMessage());
    }

    @Test
    @DisplayName("KnowledgeDomainException initializes with ErrorCode.NOT_FOUND")
    void testKnowledgeDomainException() {
        KnowledgeDomainException ex = new KnowledgeDomainException("Document not found");
        assertEquals(ErrorCode.NOT_FOUND, ex.code());
    }

    @Test
    @DisplayName("MemoryDomainException initializes with ErrorCode.NOT_FOUND")
    void testMemoryDomainException() {
        MemoryDomainException ex = new MemoryDomainException("Memory key not found in any tier");
        assertEquals(ErrorCode.NOT_FOUND, ex.code());
    }

    @Test
    @DisplayName("CodeEngineeringDomainException initializes with ErrorCode.VALIDATION")
    void testCodeEngineeringDomainException() {
        CodeEngineeringDomainException ex = new CodeEngineeringDomainException("Cyclomatic complexity exceeded");
        assertEquals(ErrorCode.VALIDATION, ex.code());
    }
}

