package ai.oenexa.code.domain;

import java.util.Map;
import java.util.Objects;

/**
 * Request payload for code generation.
 */
public record CodeGenerationRequest(
        String prompt,
        String language,
        String context,
        Map<String, String> parameters
) {
    public CodeGenerationRequest {
        Objects.requireNonNull(prompt, "prompt must not be null");
        if (language == null || language.isBlank()) {
            language = "java";
        }
        if (context == null) {
            context = "";
        }
        if (parameters == null) {
            parameters = Map.of();
        }
    }
}
