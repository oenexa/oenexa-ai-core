package ai.oenexa.review.domain;

import java.util.Objects;

/**
 * Representation of a pull request diff payload.
 */
public record PullRequest(
        String id,
        String repo,
        String branch,
        String title,
        String diffContent
) {
    public PullRequest {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(repo, "repo must not be null");
        if (branch == null) branch = "main";
        if (title == null) title = "Untitled PR";
        if (diffContent == null) diffContent = "";
    }
}
