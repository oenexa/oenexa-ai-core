package ai.oenexa.code.domain;

import java.util.List;
import java.util.Objects;

/**
 * Result payload containing generated code and metadata.
 */
public record CodeGenerationResult(
        String generatedCode,
        String language,
        String explanation,
        List<String> suggestedTests,
        int tokenEstimate
) {
    public CodeGenerationResult {
        Objects.requireNonNull(generatedCode, "generatedCode must not be null");
        Objects.requireNonNull(language, "language must not be null");
        if (explanation == null) explanation = "";
        if (suggestedTests == null) suggestedTests = List.of();
    }
}
