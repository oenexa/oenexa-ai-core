package ai.oenexa.review.service;

import ai.oenexa.review.domain.PullRequest;
import ai.oenexa.review.domain.QualityScore;
import ai.oenexa.review.domain.ReviewComment;
import ai.oenexa.review.domain.ReviewResult;
import ai.oenexa.review.domain.RiskScore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Production implementation of automated PR code review service.
 */
@Service
public class DefaultReviewService implements ReviewService {

    private static final Pattern DIFF_FILE_PATTERN = Pattern.compile("^\\+\\+\\+\\s+b/(.*)$", Pattern.MULTILINE);

    @Override
    public ReviewResult reviewPullRequest(PullRequest pullRequest) {
        Objects.requireNonNull(pullRequest, "pullRequest must not be null");
        return analyzeInternal(pullRequest.id(), pullRequest.diffContent());
    }

    @Override
    public ReviewResult reviewDiff(String diffContent) {
        return analyzeInternal("diff-" + System.currentTimeMillis(), diffContent != null ? diffContent : "");
    }

    private ReviewResult analyzeInternal(String id, String diff) {
        List<String> affectedFiles = new ArrayList<>();
        Matcher fileMatcher = DIFF_FILE_PATTERN.matcher(diff);
        while (fileMatcher.find()) {
            affectedFiles.add(fileMatcher.group(1));
        }

        List<ReviewComment> comments = new ArrayList<>();
        List<String> riskFactors = new ArrayList<>();
        int blastRadius = Math.max(1, affectedFiles.size() * 5);

        // Analyze diff lines
        String[] lines = diff.split("\\r?\\n");
        String currentFile = "unknown";
        int currentLine = 1;

        for (String line : lines) {
            if (line.startsWith("+++ b/")) {
                currentFile = line.substring(6);
                currentLine = 1;
            } else if (line.startsWith("@@")) {
                // Parse @@ -x,y +z,w @@
                currentLine = 1;
            } else if (line.startsWith("+") && !line.startsWith("+++")) {
                currentLine++;
                String content = line.substring(1).trim();

                // Check for direct print statements
                if (content.contains("System.out.print") || content.contains("System.err.print")) {
                    comments.add(new ReviewComment(
                            currentFile,
                            currentLine,
                            ReviewComment.Severity.WARNING,
                            "Direct stdout/stderr logging detected in production code",
                            "Use structured SLF4J logger instead"
                    ));
                }

                // Check for TODO/FIXME
                if (content.contains("TODO") || content.contains("FIXME")) {
                    comments.add(new ReviewComment(
                            currentFile,
                            currentLine,
                            ReviewComment.Severity.INFO,
                            "Unresolved TODO/FIXME annotation found",
                            "Ensure task is tracked in issue tracker before merging"
                    ));
                }

                // Check for potential credentials
                if (content.matches(".*(password|secret|apiKey|api_key)\\s*=\\s*[\"'][^\"']+[\"'].*")) {
                    comments.add(new ReviewComment(
                            currentFile,
                            currentLine,
                            ReviewComment.Severity.ERROR,
                            "Possible hardcoded secret or API key detected in diff",
                            "Inject credentials via environment variables or secret manager"
                    ));
                }
            }
        }

        // Assess risk factors
        for (String file : affectedFiles) {
            if (file.contains("migration") || file.endsWith(".sql")) {
                riskFactors.add("Database migration or schema modification: " + file);
                blastRadius += 30;
            }
            if (file.contains("security") || file.contains("auth") || file.contains("SecurityConfig")) {
                riskFactors.add("Authentication or security configuration modified: " + file);
                blastRadius += 25;
            }
            if (file.endsWith(".gradle.kts") || file.endsWith(".gradle") || file.equals("pom.xml") || file.equals("Dockerfile")) {
                riskFactors.add("Build or deployment configuration changed: " + file);
                blastRadius += 15;
            }
        }

        RiskScore.RiskLevel riskLevel;
        if (blastRadius >= 50) {
            riskLevel = RiskScore.RiskLevel.CRITICAL;
        } else if (blastRadius >= 30) {
            riskLevel = RiskScore.RiskLevel.HIGH;
        } else if (blastRadius >= 15) {
            riskLevel = RiskScore.RiskLevel.MEDIUM;
        } else {
            riskLevel = RiskScore.RiskLevel.LOW;
        }

        RiskScore riskScore = new RiskScore(riskLevel, blastRadius, riskFactors);

        // Quality scoring
        int maintainability = 95 - (comments.size() * 5);
        int reliability = 90 - (comments.size() * 3);
        int security = 95 - ((int) comments.stream().filter(c -> c.severity() == ReviewComment.Severity.ERROR).count() * 30);
        int overall = (maintainability + reliability + security) / 3;

        QualityScore qualityScore = new QualityScore(maintainability, reliability, security, overall);

        boolean approved = qualityScore.isAcceptable()
                && comments.stream().noneMatch(c -> c.severity() == ReviewComment.Severity.ERROR)
                && !riskScore.requiresHumanApproval();

        String summary = String.format("Automated Review: %s across %d file(s). Found %d comment(s). Quality: %d/100, Risk: %s.",
                approved ? "APPROVED" : "CHANGES_REQUESTED",
                affectedFiles.size(),
                comments.size(),
                overall,
                riskLevel
        );

        return new ReviewResult(id, approved, summary, qualityScore, riskScore, comments);
    }
}
