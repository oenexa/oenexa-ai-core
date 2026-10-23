package com.oenexa.domain;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Enterprise Domain Records for OpenAI Wire-Compatible Tool Calling, Structured Outputs, and Responses API.
 */
public final class OpenAiToolProtocolDomain {

    private OpenAiToolProtocolDomain() {}

    public record OpenAiToolDefinition(
            String type,
            OpenAiFunctionDefinition function
    ) {
        public OpenAiToolDefinition {
            type = (type != null) ? type : "function";
            Objects.requireNonNull(function, "function must not be null");
        }

        public static OpenAiToolDefinition function(String name, String description, Map<String, Object> parameters) {
            return new OpenAiToolDefinition("function", new OpenAiFunctionDefinition(name, description, parameters));
        }
    }

    public record OpenAiFunctionDefinition(
            String name,
            String description,
            Map<String, Object> parameters
    ) {
        public OpenAiFunctionDefinition {
            Objects.requireNonNull(name, "name must not be null");
            description = (description != null) ? description : "";
            parameters = (parameters != null) ? Map.copyOf(parameters) : Map.of();
        }
    }

    public record OpenAiToolCall(
            String id,
            String type,
            OpenAiFunctionCall function
    ) {
        public OpenAiToolCall {
            Objects.requireNonNull(id, "id must not be null");
            type = (type != null) ? type : "function";
            Objects.requireNonNull(function, "function must not be null");
        }
    }

    public record OpenAiFunctionCall(
            String name,
            String arguments
    ) {
        public OpenAiFunctionCall {
            Objects.requireNonNull(name, "name must not be null");
            arguments = (arguments != null) ? arguments : "{}";
        }
    }

    public record OpenAiResponseFormat(
            String type,
            Map<String, Object> jsonSchema
    ) {
        public OpenAiResponseFormat {
            type = (type != null) ? type : "text";
            jsonSchema = (jsonSchema != null) ? Map.copyOf(jsonSchema) : Map.of();
        }
    }

    public record OpenAiResponsesRequest(
            String model,
            String input,
            List<OpenAiToolDefinition> tools,
            String reasoningEffort
    ) {
        public OpenAiResponsesRequest {
            model = (model != null && !model.isBlank()) ? model : "oenexa-neo-gpt-o1";
            input = (input != null) ? input : "";
            tools = (tools != null) ? List.copyOf(tools) : List.of();
            reasoningEffort = (reasoningEffort != null && !reasoningEffort.isBlank()) ? reasoningEffort : "high";
        }
    }

    public record OpenAiResponseOutputItem(
            String type,
            String role,
            String content
    ) {
        public OpenAiResponseOutputItem {
            type = (type != null) ? type : "message";
            role = (role != null) ? role : "assistant";
            content = (content != null) ? content : "";
        }
    }

    public record OpenAiResponsesResponse(
            String id,
            String object,
            long createdAt,
            String model,
            List<OpenAiResponseOutputItem> output,
            String status,
            OpenAiDomain.OpenAiUsage usage
    ) {
        public OpenAiResponsesResponse {
            Objects.requireNonNull(id, "id must not be null");
            object = (object != null) ? object : "response";
            model = (model != null) ? model : "oenexa-neo-gpt-o1";
            output = (output != null) ? List.copyOf(output) : List.of();
            status = (status != null) ? status : "completed";
            Objects.requireNonNull(usage, "usage must not be null");
        }
    }
}
