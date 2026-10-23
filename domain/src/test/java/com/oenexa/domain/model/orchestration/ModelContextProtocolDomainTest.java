package com.oenexa.domain.model.orchestration;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

import com.oenexa.domain.model.orchestration.ModelContextProtocolDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModelContextProtocolDomain Zero-Mock Unit Tests")
class ModelContextProtocolDomainTest {

    @Test
    @DisplayName("Should construct McpRequest and McpResponse correctly")
    void shouldConstructRequestAndResponse() {
        var req = new McpRequest("2.0", "tools/list", Map.of(), "req-1");
        assertEquals("2.0", req.jsonrpc());
        assertEquals("tools/list", req.method());
        assertEquals("req-1", req.id());

        var respSuccess = McpResponse.success("req-1", Map.of("tools", 10));
        assertNull(respSuccess.error());
        assertNotNull(respSuccess.result());

        var respError = McpResponse.error("req-2", -32601, "Method not found");
        assertNotNull(respError.error());
        assertEquals(-32601, respError.error().code());
    }

    @Test
    @DisplayName("Should validate McpToolDefinition and ToolCallResult")
    void shouldValidateTools() {
        var tool = new McpToolDefinition(
                "oenexa_ast_refactor",
                "Refactors Java AST trees without mocks",
                Map.of("type", "object")
        );
        assertEquals("oenexa_ast_refactor", tool.name());
        assertNotNull(tool.inputSchema());

        var result = McpToolCallResult.text("Successfully refactored OrderService.java");
        assertFalse(result.isError());
        assertEquals(1, result.content().size());
        assertEquals("Successfully refactored OrderService.java", result.content().get(0).text());

        var errResult = McpToolCallResult.error("File not found");
        assertTrue(errResult.isError());
    }
}


