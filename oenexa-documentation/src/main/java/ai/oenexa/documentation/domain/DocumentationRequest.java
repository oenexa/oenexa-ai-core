package ai.oenexa.documentation.domain;

import java.util.List;
import java.util.Objects;

/**
 * Request payload for synthesizing technical documentation.
 */
public record DocumentationRequest(
        String title,
        DocType docType,
        DocFormat format,
        String context,
        List<String> components
) {
    public DocumentationRequest {
        Objects.requireNonNull(title, "title must not be null");
        if (docType == null) {
            docType = DocType.ARCHITECTURE;
        }
        if (format == null) {
            format = DocFormat.MARKDOWN;
        }
        if (context == null) {
            context = "";
        }
        if (components == null) {
            components = List.of();
        }
    }
}
