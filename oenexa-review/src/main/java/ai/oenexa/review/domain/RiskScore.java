package ai.oenexa.review.domain;

import java.util.List;
import java.util.Objects;

/**
 * Risk assessment evaluating the blast-radius of code changes.
 */
public record RiskScore(
        RiskLevel level,
        int blastRadiusScore,
        List<String> riskFactors
) {
    public enum RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }

    public RiskScore {
        Objects.requireNonNull(level, "level must not be null");
        if (riskFactors == null) {
            riskFactors = List.of();
        }
    }

    public boolean requiresHumanApproval() {
        return level == RiskLevel.HIGH || level == RiskLevel.CRITICAL;
    }
}
