package ai.oenexa.code.domain;

import java.util.List;
import java.util.Objects;

/**
 * Result payload containing refactored code and applied transformations.
 */
public record RefactorResult(
        String originalCode,
        String refactoredCode,
        String goal,
        List<String> appliedTransformations,
        int complexityDelta
) {
    public RefactorResult {
        Objects.requireNonNull(originalCode, "originalCode must not be null");
        Objects.requireNonNull(refactoredCode, "refactoredCode must not be null");
        Objects.requireNonNull(goal, "goal must not be null");
        if (appliedTransformations == null) {
            appliedTransformations = List.of();
        }
    }
}
