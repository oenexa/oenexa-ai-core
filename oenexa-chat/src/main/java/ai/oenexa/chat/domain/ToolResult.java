package ai.oenexa.chat.domain;

public record ToolResult(String callId, String toolName, String output, boolean isError) {
    public ToolResult(String callId, String toolName, String output) {
        this(callId, toolName, output, false);
    }
}
