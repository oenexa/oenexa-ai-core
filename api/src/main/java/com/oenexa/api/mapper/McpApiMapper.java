package com.oenexa.api.mapper;

import com.oenexa.api.dto.mcp.McpDtos.*;
import com.oenexa.domain.model.orchestration.ModelContextProtocolDomain.*;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Enterprise API Mapper for Model Context Protocol (MCP v1.0) DTOs.
 */
@Component
public class McpApiMapper {

    public McpToolExecutionResultDto toToolExecutionResultDto(String toolName, McpToolCallResult result) {
        if (result == null) {
            return new McpToolExecutionResultDto(toolName, false, Map.of(), "Execution returned null result");
        }
        return new McpToolExecutionResultDto(
                toolName,
                !result.isError(),
                Map.of("content", result.content() != null ? result.content() : java.util.List.of()),
                result.isError() ? "MCP Tool execution failed" : null
        );
    }

    public McpRpcResponseDto toRpcResponseDto(McpResponse response) {
        if (response == null) {
            return null;
        }
        return new McpRpcResponseDto(
                response.jsonrpc(),
                response.result(),
                response.error(),
                response.id()
        );
    }
}
