package com.oenexa.api;

import com.oenexa.application.ModelContextProtocolService;
import com.oenexa.domain.ModelContextProtocolDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST & JSON-RPC 2.0 API Controller for Model Context Protocol (MCP v1.0).
 * Exposes standardized endpoints for IDE plugins (VS Code / JetBrains),
 * CLI tools, and agent runtimes.
 */
@RestController
@RequestMapping("/api/v4/mcp")
@CrossOrigin(origins = "*")
public class ModelContextProtocolController {

    private final ModelContextProtocolService mcpService;

    public ModelContextProtocolController() {
        this(new ModelContextProtocolService());
    }

    @Autowired
    public ModelContextProtocolController(ModelContextProtocolService mcpService) {
        this.mcpService = Objects.requireNonNull(mcpService, "mcpService must not be null");
    }

    @PostMapping("/rpc")
    public ResponseEntity<McpResponse> handleRpc(@RequestBody McpRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(McpResponse.error("null", -32600, "Invalid Request"));
        }
        return ResponseEntity.ok(mcpService.handleRpc(request));
    }

    @GetMapping("/tools")
    public ResponseEntity<List<McpToolDefinition>> listTools() {
        return ResponseEntity.ok(mcpService.listTools());
    }

    @PostMapping("/tools/{toolName}")
    public ResponseEntity<McpToolCallResult> executeTool(
            @PathVariable String toolName,
            @RequestBody(required = false) Map<String, Object> arguments
    ) {
        return ResponseEntity.ok(mcpService.executeTool(toolName, arguments != null ? arguments : Map.of()));
    }

    @GetMapping("/prompts")
    public ResponseEntity<List<McpPromptDefinition>> listPrompts() {
        return ResponseEntity.ok(mcpService.listPrompts());
    }

    @GetMapping("/resources")
    public ResponseEntity<List<McpResourceDefinition>> listResources() {
        return ResponseEntity.ok(mcpService.listResources());
    }
}
