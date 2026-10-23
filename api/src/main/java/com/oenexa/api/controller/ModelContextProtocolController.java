package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.mcp.McpDtos.*;
import com.oenexa.api.mapper.McpApiMapper;
import com.oenexa.application.security.McpSecurityPolicy;
import com.oenexa.application.service.orchestration.ModelContextProtocolService;
import com.oenexa.application.validation.McpValidator;
import com.oenexa.domain.model.orchestration.ModelContextProtocolDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST & JSON-RPC 2.0 API Controller for Model Context Protocol (MCP v1.0).
 * Supports standard 11-Layer Clean Hexagonal Architecture:
 * Controller -> DTO -> Validation -> Security -> Mapper -> Service -> ApiResponse<T>.
 */
@RestController
@RequestMapping("/api/v4/mcp")
@CrossOrigin(origins = "*")
public class ModelContextProtocolController {

    private final ModelContextProtocolService mcpService;
    private final McpValidator validator;
    private final McpSecurityPolicy securityPolicy;
    private final McpApiMapper mapper;

    public ModelContextProtocolController() {
        this(new ModelContextProtocolService());
    }

    public ModelContextProtocolController(ModelContextProtocolService mcpService) {
        this(mcpService, new McpValidator(), new McpSecurityPolicy(), new McpApiMapper());
    }

    @Autowired
    public ModelContextProtocolController(
            ModelContextProtocolService mcpService,
            McpValidator validator,
            McpSecurityPolicy securityPolicy,
            McpApiMapper mapper
    ) {
        this.mcpService = Objects.requireNonNull(mcpService, "mcpService must not be null");
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.securityPolicy = Objects.requireNonNull(securityPolicy, "securityPolicy must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
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

    // ==========================================
    // 11-Layer Clean Hexagonal Architecture v2 Endpoints
    // ==========================================

    @PostMapping("/v2/tools/{toolName}")
    public ResponseEntity<ApiResponse<McpToolExecutionResultDto>> executeToolV2(
            @PathVariable String toolName,
            @RequestBody(required = false) McpToolExecutionRequestDto req,
            @RequestHeader(value = "X-User-Role", defaultValue = "ROLE_USER") String role
    ) {
        validator.validateToolExecution(toolName);
        securityPolicy.verifyToolAccess(toolName, role);

        Map<String, Object> args = req != null && req.arguments() != null ? req.arguments() : Map.of();
        String cmd = args.get("command") instanceof String s ? s : null;
        securityPolicy.verifyToolSafety(toolName, cmd);

        McpToolCallResult result = mcpService.executeTool(toolName, args);
        return ResponseEntity.ok(ApiResponse.ok("MCP Tool execution successful", mapper.toToolExecutionResultDto(toolName, result)));
    }

    @PostMapping("/v2/rpc")
    public ResponseEntity<ApiResponse<McpRpcResponseDto>> handleRpcV2(@RequestBody McpRpcRequestDto req) {
        validator.validateRpcRequest(req.jsonrpc(), req.method());

        McpRequest request = new McpRequest(req.jsonrpc(), req.method(), req.params(), req.id());
        McpResponse response = mcpService.handleRpc(request);

        return ResponseEntity.ok(ApiResponse.ok("MCP RPC processed", mapper.toRpcResponseDto(response)));
    }
}
