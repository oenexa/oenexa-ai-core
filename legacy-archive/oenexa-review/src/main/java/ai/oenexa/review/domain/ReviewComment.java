package ai.oenexa.review.domain;

import java.util.Objects;

/**
 * Line-level review comment produced by AI reviewer.
 */
public record ReviewComment(
        String path,
        int line,
        Severity severity,
        String message,
        String suggestedFix
) {
    public enum Severity { INFO, WARNING, ERROR }

    public ReviewComment {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(message, "message must not be null");
        if (severity == null) {
            severity = Severity.INFO;
        }
        if (suggestedFix == null) {
            suggestedFix = "";
        }
    }
}
