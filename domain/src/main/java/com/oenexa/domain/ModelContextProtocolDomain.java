package com.oenexa.domain;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Domain specifications for Model Context Protocol (MCP v1.0).
 * Implements JSON-RPC 2.0 protocol for standardized AI tool discovery,
 * tool invocation, prompt templates, and resource access.
 */
public class ModelContextProtocolDomain {

    public record McpRequest(
            String jsonrpc,
            String method,
            Map<String, Object> params,
            String id
    ) {
        public McpRequest {
            jsonrpc = (jsonrpc != null) ? jsonrpc : "2.0";
            Objects.requireNonNull(method, "method must not be null");
        }
    }

    public record McpResponse(
            String jsonrpc,
            Object result,
            McpError error,
            String id
    ) {
        public McpResponse {
            jsonrpc = (jsonrpc != null) ? jsonrpc : "2.0";
        }

        public static McpResponse success(String id, Object result) {
            return new McpResponse("2.0", result, null, id);
        }

        public static McpResponse error(String id, int code, String message) {
            return new McpResponse("2.0", null, new McpError(code, message, null), id);
        }
    }

    public record McpError(int code, String message, Object data) {}

    public record McpToolDefinition(
            String name,
            String description,
            Map<String, Object> inputSchema
    ) {
        public McpToolDefinition {
            Objects.requireNonNull(name, "name must not be null");
            description = (description != null) ? description : "";
            inputSchema = (inputSchema != null) ? Map.copyOf(inputSchema) : Map.of();
        }
    }

    public record McpContentBlock(String type, String text) {
        public McpContentBlock {
            type = (type != null) ? type : "text";
            text = (text != null) ? text : "";
        }
    }

    public record McpToolCallResult(
            List<McpContentBlock> content,
            boolean isError
    ) {
        public McpToolCallResult {
            content = (content != null) ? List.copyOf(content) : List.of();
        }

        public static McpToolCallResult text(String text) {
            return new McpToolCallResult(List.of(new McpContentBlock("text", text)), false);
        }

        public static McpToolCallResult error(String error) {
            return new McpToolCallResult(List.of(new McpContentBlock("text", error)), true);
        }
    }

    public record McpPromptArgument(String name, String description, boolean required) {}

    public record McpPromptDefinition(
            String name,
            String description,
            List<McpPromptArgument> arguments
    ) {
        public McpPromptDefinition {
            Objects.requireNonNull(name, "name must not be null");
            arguments = (arguments != null) ? List.copyOf(arguments) : List.of();
        }
    }

    public record McpResourceDefinition(
            String uri,
            String name,
            String mimeType,
            String description
    ) {
        public McpResourceDefinition {
            Objects.requireNonNull(uri, "uri must not be null");
            Objects.requireNonNull(name, "name must not be null");
        }
    }
}
