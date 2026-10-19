package ai.oenexa.code.domain;

import java.util.Objects;

/**
 * Request payload for code refactoring.
 */
public record RefactorRequest(
        String sourceCode,
        String language,
        String goal
) {
    public RefactorRequest {
        Objects.requireNonNull(sourceCode, "sourceCode must not be null");
        if (language == null || language.isBlank()) {
            language = "java";
        }
        if (goal == null || goal.isBlank()) {
            goal = "CLEAN_CODE";
        }
    }
}
