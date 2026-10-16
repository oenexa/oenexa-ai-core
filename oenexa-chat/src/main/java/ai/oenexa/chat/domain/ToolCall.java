package ai.oenexa.chat.domain;

import java.util.Map;

public record ToolCall(String callId, String toolName, Map<String, Object> arguments) {
    public ToolCall(String callId, String toolName) {
        this(callId, toolName, Map.of());
    }
}
