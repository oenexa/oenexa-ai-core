package ai.oenexa.documentation.domain;

import java.util.Objects;

/**
 * Result payload containing the generated Mermaid diagram code.
 */
public record DiagramResult(
        String title,
        String diagramType,
        String mermaidSource
) {
    public DiagramResult {
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(diagramType, "diagramType must not be null");
        if (mermaidSource == null) {
            mermaidSource = "";
        }
    }
}
