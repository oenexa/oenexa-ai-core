package ai.oenexa.code.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable record representing a managed code snippet.
 */
public record CodeSnippet(
        String id,
        String filename,
        String language,
        String content,
        Instant createdAt
) {
    public CodeSnippet {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(filename, "filename must not be null");
        Objects.requireNonNull(language, "language must not be null");
        Objects.requireNonNull(content, "content must not be null");
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public int lineCount() {
        return content.isEmpty() ? 0 : content.split("\\r?\\n").length;
    }

    public int characterCount() {
        return content.length();
    }
}
