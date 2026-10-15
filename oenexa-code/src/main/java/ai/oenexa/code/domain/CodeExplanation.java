package ai.oenexa.code.domain;

import java.util.List;
import java.util.Objects;

/**
 * Result payload containing architectural, complexity, and algorithmic explanations.
 */
public record CodeExplanation(
        String summary,
        String timeComplexity,
        String spaceComplexity,
        List<String> keyPatterns,
        List<String> potentialPitfalls
) {
    public CodeExplanation {
        Objects.requireNonNull(summary, "summary must not be null");
        if (timeComplexity == null) timeComplexity = "O(1)";
        if (spaceComplexity == null) spaceComplexity = "O(1)";
        if (keyPatterns == null) keyPatterns = List.of();
        if (potentialPitfalls == null) potentialPitfalls = List.of();
    }
}
