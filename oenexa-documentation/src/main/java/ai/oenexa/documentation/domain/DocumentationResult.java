package ai.oenexa.documentation.domain;

import java.util.List;
import java.util.Objects;

/**
 * Result payload containing synthesized document text and metadata.
 */
public record DocumentationResult(
        String title,
        DocType docType,
        DocFormat format,
        String content,
        int wordCount,
        List<String> generatedSections
) {
    public DocumentationResult {
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(docType, "docType must not be null");
        Objects.requireNonNull(format, "format must not be null");
        if (content == null) {
            content = "";
        }
        if (generatedSections == null) {
            generatedSections = List.of();
        }
    }
}
