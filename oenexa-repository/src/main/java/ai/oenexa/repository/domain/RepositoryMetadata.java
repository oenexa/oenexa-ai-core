package ai.oenexa.repository.domain;

import java.util.List;
import java.util.Objects;

/**
 * Inspection metadata for a connected git repository.
 */
public record RepositoryMetadata(
        String repositoryUrl,
        VcsProvider provider,
        String defaultBranch,
        long commitCount,
        List<String> detectedLanguages
) {
    public RepositoryMetadata {
        Objects.requireNonNull(repositoryUrl, "repositoryUrl must not be null");
        if (provider == null) {
            provider = VcsProvider.GITHUB;
        }
        if (defaultBranch == null || defaultBranch.isBlank()) {
            defaultBranch = "main";
        }
        if (detectedLanguages == null) {
            detectedLanguages = List.of();
        }
    }
}
