package ai.oenexa.documentation.domain;

import java.util.List;
import java.util.Objects;

/**
 * Request payload for synthesizing a Mermaid diagram.
 */
public record DiagramRequest(
        String diagramType,
        String title,
        List<String> nodes,
        List<String> connections
) {
    public DiagramRequest {
        Objects.requireNonNull(title, "title must not be null");
        if (diagramType == null || diagramType.isBlank()) {
            diagramType = "flowchart TD";
        }
        if (nodes == null) {
            nodes = List.of();
        }
        if (connections == null) {
            connections = List.of();
        }
    }
}
