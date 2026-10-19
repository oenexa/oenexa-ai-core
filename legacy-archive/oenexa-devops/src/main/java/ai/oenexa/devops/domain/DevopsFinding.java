package ai.oenexa.devops.domain;

import java.util.Objects;

/**
 * Finding or recommendation from DevOps manifest inspection.
 */
public record DevopsFinding(
        String ruleId,
        DiagnosisSeverity severity,
        String resource,
        String message,
        String remediation
) {
    public DevopsFinding {
        Objects.requireNonNull(ruleId, "ruleId must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(message, "message must not be null");
        if (resource == null) {
            resource = "general";
        }
        if (remediation == null) {
            remediation = "";
        }
    }
}
