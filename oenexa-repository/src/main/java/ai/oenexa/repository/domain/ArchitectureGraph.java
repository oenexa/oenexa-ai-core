package ai.oenexa.repository.domain;

import java.util.List;
import java.util.Objects;

/**
 * Architectural map representing repository modules, dependency topologies, and Mermaid diagram.
 */
public record ArchitectureGraph(
        String repoName,
        List<String> modules,
        List<DependencyNode> dependencies,
        String mermaidDependencyMap
) {
    public ArchitectureGraph {
        Objects.requireNonNull(repoName, "repoName must not be null");
        if (modules == null) {
            modules = List.of();
        }
        if (dependencies == null) {
            dependencies = List.of();
        }
        if (mermaidDependencyMap == null) {
            mermaidDependencyMap = "";
        }
    }
}
