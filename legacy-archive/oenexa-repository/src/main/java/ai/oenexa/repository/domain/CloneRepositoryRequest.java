package ai.oenexa.repository.domain;

import java.util.Objects;

/**
 * Request payload for cloning or connecting a remote repository.
 */
public record CloneRepositoryRequest(
        String repositoryUrl,
        VcsProvider provider,
        String targetBranch,
        String token
) {
    public CloneRepositoryRequest {
        Objects.requireNonNull(repositoryUrl, "repositoryUrl must not be null");
        if (provider == null) {
            provider = VcsProvider.GITHUB;
        }
        if (targetBranch == null || targetBranch.isBlank()) {
            targetBranch = "main";
        }
        if (token == null) {
            token = "";
        }
    }
}
