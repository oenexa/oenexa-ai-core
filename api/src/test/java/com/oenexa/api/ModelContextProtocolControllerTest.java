package com.oenexa.api;

import com.oenexa.application.ModelContextProtocolService;
import com.oenexa.domain.ModelContextProtocolDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModelContextProtocolController Zero-Mock Unit Tests")
class ModelContextProtocolControllerTest {

    private ModelContextProtocolController controller;

    @BeforeEach
    void setUp() {
        controller = new ModelContextProtocolController(new ModelContextProtocolService());
    }

    @Test
    @DisplayName("Should test no-arg constructor")
    void shouldTestNoArgConstructor() {
        ModelContextProtocolController defaultCtrl = new ModelContextProtocolController();
        assertNotNull(defaultCtrl);
        assertFalse(defaultCtrl.listTools().getBody().isEmpty());
    }

    @Test
    @DisplayName("Should dispatch RPC request via /rpc endpoint")
    void shouldHandleRpc() {
        var req = new McpRequest("2.0", "tools/list", Map.of(), "req-100");
        ResponseEntity<McpResponse> response = controller.handleRpc(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("req-100", response.getBody().id());
        assertNull(response.getBody().error());

        // Null request returns 400 with JSON-RPC error
        ResponseEntity<McpResponse> bad = controller.handleRpc(null);
        assertEquals(HttpStatus.BAD_REQUEST, bad.getStatusCode());
        assertNotNull(bad.getBody().error());
    }

    @Test
    @DisplayName("Should list tools via REST endpoint")
    void shouldListTools() {
        ResponseEntity<List<McpToolDefinition>> response = controller.listTools();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 5);
    }

    @Test
    @DisplayName("Should execute tool directly via REST endpoint")
    void shouldExecuteToolDirectly() {
        ResponseEntity<McpToolCallResult> response = controller.executeTool(
                "oenexa_ast_refactor",
                Map.of("targetFile", "Main.java", "instruction", "virtual threads")
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isError());
        assertTrue(response.getBody().content().get(0).text().contains("Main.java"));

        // Null arguments fallback
        ResponseEntity<McpToolCallResult> nullArgs = controller.executeTool("oenexa_ast_refactor", null);
        assertNotNull(nullArgs.getBody());
    }

    @Test
    @DisplayName("Should list prompts and resources via REST endpoints")
    void shouldListPromptsAndResources() {
        ResponseEntity<List<McpPromptDefinition>> prompts = controller.listPrompts();
        assertEquals(HttpStatus.OK, prompts.getStatusCode());
        assertFalse(prompts.getBody().isEmpty());

        ResponseEntity<List<McpResourceDefinition>> resources = controller.listResources();
        assertEquals(HttpStatus.OK, resources.getStatusCode());
        assertFalse(resources.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should reject null service in constructor")
    void shouldRejectNullService() {
        assertThrows(NullPointerException.class, () -> new ModelContextProtocolController(null));
    }
}
