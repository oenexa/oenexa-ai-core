package com.oenexa.api.dto.mcp;

import java.util.Map;

/**
 * Standard Data Transfer Objects for Model Context Protocol (MCP v1.0).
 */
public final class McpDtos {

    private McpDtos() {}

    public record McpToolExecutionRequestDto(
            String toolName,
            Map<String, Object> arguments
    ) {}

    public record McpToolExecutionResultDto(
            String toolName,
            boolean success,
            Map<String, Object> output,
            String errorMessage
    ) {}

    public record McpRpcRequestDto(
            String jsonrpc,
            String method,
            Map<String, Object> params,
            String id
    ) {}

    public record McpRpcResponseDto(
            String jsonrpc,
            Object result,
            Object error,
            String id
    ) {}
}
