package ai.oenexa.review.web;

import ai.oenexa.review.domain.PullRequest;
import ai.oenexa.review.domain.ReviewResult;
import ai.oenexa.review.service.ReviewService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint exposing automated PR and diff review services.
 */
@RestController
@RequestMapping("/api/v1/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/pr")
    public ResponseEntity<ReviewResult> reviewPullRequest(@RequestBody PullRequest pullRequest) {
        return ResponseEntity.ok(reviewService.reviewPullRequest(pullRequest));
    }

    @PostMapping("/diff")
    public ResponseEntity<ReviewResult> reviewDiff(@RequestBody Map<String, String> payload) {
        String diff = payload.getOrDefault("diff", "");
        return ResponseEntity.ok(reviewService.reviewDiff(diff));
    }
}
