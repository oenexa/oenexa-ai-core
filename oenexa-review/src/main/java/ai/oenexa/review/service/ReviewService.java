package ai.oenexa.review.service;

import ai.oenexa.review.domain.PullRequest;
import ai.oenexa.review.domain.ReviewResult;

/**
 * Service contract for automated Pull Request and code review.
 */
public interface ReviewService {

    ReviewResult reviewPullRequest(PullRequest pullRequest);

    ReviewResult reviewDiff(String diffContent);
}
