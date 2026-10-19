package ai.oenexa.chat.domain;

import java.util.Map;

/**
 * Contract for executable tools invoked by the Gemini conversational reasoning engine.
 */
public interface ChatTool {

    String name();

    String description();

    Map<String, Object> parameterSchema();

    String execute(Map<String, Object> parameters);
}
