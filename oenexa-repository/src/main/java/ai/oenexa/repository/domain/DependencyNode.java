package ai.oenexa.repository.domain;

import java.util.List;
import java.util.Objects;

/**
 * Node representing an individual artifact dependency.
 */
public record DependencyNode(
        String artifactId,
        String version,
        String scope,
        List<String> transitiveDependencies
) {
    public DependencyNode {
        Objects.requireNonNull(artifactId, "artifactId must not be null");
        if (version == null || version.isBlank()) {
            version = "latest";
        }
        if (scope == null || scope.isBlank()) {
            scope = "implementation";
        }
        if (transitiveDependencies == null) {
            transitiveDependencies = List.of();
        }
    }
}
