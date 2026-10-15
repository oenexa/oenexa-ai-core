package ai.oenexa.devops.domain;

import java.util.Objects;

/**
 * Request to analyze a Docker, Compose, Kubernetes, or Helm manifest.
 */
public record ManifestAnalysisRequest(
        String filename,
        ManifestType type,
        String content
) {
    public ManifestAnalysisRequest {
        Objects.requireNonNull(filename, "filename must not be null");
        if (type == null) {
            type = ManifestType.DOCKERFILE;
        }
        if (content == null) {
            content = "";
        }
    }
}
