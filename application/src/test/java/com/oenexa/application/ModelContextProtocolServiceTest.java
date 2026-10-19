package com.oenexa.application;

import com.oenexa.domain.ModelContextProtocolDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModelContextProtocolService Zero-Mock Unit Tests")
class ModelContextProtocolServiceTest {

    private ModelContextProtocolService service;

    @BeforeEach
    void setUp() {
        service = new ModelContextProtocolService();
    }

    @Test
    @DisplayName("Should list all registered MCP tools")
    void shouldListTools() {
        var tools = service.listTools();
        assertFalse(tools.isEmpty());
        assertTrue(tools.stream().anyMatch(t -> t.name().equals("oenexa_ast_refactor")));
        assertTrue(tools.stream().anyMatch(t -> t.name().equals("oenexa_zero_mock_test_generator")));
        assertTrue(tools.stream().anyMatch(t -> t.name().equals("oenexa_sast_security_audit")));
        assertTrue(tools.stream().anyMatch(t -> t.name().equals("oenexa_rag_hybrid_query")));
        assertTrue(tools.stream().anyMatch(t -> t.name().equals("oenexa_nvme_context_prefetch")));
    }

    @Test
    @DisplayName("Should execute MCP tool call via executeTool")
    void shouldExecuteToolCall() {
        var result = service.executeTool("oenexa_ast_refactor", Map.of(
                "targetFile", "UserService.java",
                "instruction", "Enforce virtual threads"
        ));

        assertNotNull(result);
        assertFalse(result.isError());
        assertTrue(result.content().get(0).text().contains("UserService.java"));

        var unknown = service.executeTool("unknown_tool", Map.of());
        assertTrue(unknown.isError());
    }

    @Test
    @DisplayName("Should dispatch JSON-RPC 2.0 requests cleanly")
    void shouldHandleJsonRpcRequests() {
        // tools/list
        var listReq = new McpRequest("2.0", "tools/list", Map.of(), "r1");
        var listResp = service.handleRpc(listReq);
        assertEquals("r1", listResp.id());
        assertNull(listResp.error());

        // tools/call
        var callReq = new McpRequest("2.0", "tools/call", Map.of(
                "name", "oenexa_zero_mock_test_generator",
                "arguments", Map.of("targetClass", "PaymentService")
        ), "r2");
        var callResp = service.handleRpc(callReq);
        assertEquals("r2", callResp.id());
        assertNull(callResp.error());
        assertNotNull(callResp.result());

        // prompts/list
        var promptResp = service.handleRpc(new McpRequest("2.0", "prompts/list", Map.of(), "r3"));
        assertNull(promptResp.error());

        // resources/list
        var resResp = service.handleRpc(new McpRequest("2.0", "resources/list", Map.of(), "r4"));
        assertNull(resResp.error());

        // Unknown method
        var errResp = service.handleRpc(new McpRequest("2.0", "invalid/method", Map.of(), "r5"));
        assertNotNull(errResp.error());
        assertEquals(-32601, errResp.error().code());
    }
}
