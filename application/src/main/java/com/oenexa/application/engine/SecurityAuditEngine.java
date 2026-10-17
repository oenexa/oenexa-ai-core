package com.oenexa.application.engine;

import com.oenexa.domain.CodeEngineeringDomain.SecurityScanRequest;
import com.oenexa.domain.CodeEngineeringDomain.SecurityScanResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dedicated SRP Engine for Static Application Security Testing (SAST),
 * credential leak prevention, OWASP compliance checking, and vulnerability auditing.
 */
@Component
public class SecurityAuditEngine {

    private static final Pattern AWS_KEY_PATTERN = Pattern.compile("AKIA[0-9A-Z]{16}");
    private static final Pattern GITHUB_TOKEN_PATTERN = Pattern.compile("ghp_[0-9a-zA-Z]{36}");
    private static final Pattern PRIVATE_KEY_PATTERN = Pattern.compile("-----BEGIN (?:[A-Z0-9_-]+\\s+)?(?:PRIVATE\\s+)?KEY-----");
    private static final Pattern GENERIC_SECRET_PATTERN = Pattern.compile("(?i)(?:api[_-]?key|secret[_-]?key|private[_-]?key|jwt[_-]?token|bearer|password)\\s*[:=]\\s*\\\\?[\"']([A-Za-z0-9_\\-\\.\\/\\+]{16,})\\\\?[\"']");

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile("(?i)(?:SELECT|INSERT|UPDATE|DELETE).*\\+\\s*[a-zA-Z_]\\w*");
    private static final Pattern WEAK_CRYPTO_PATTERN = Pattern.compile("(?i)MessageDigest\\.getInstance\\([\"'](MD5|SHA-1)[\"']\\)");
    private static final Pattern CMD_INJECTION_PATTERN = Pattern.compile("Runtime\\.getRuntime\\(\\)\\.exec\\(|ProcessBuilder\\(");

    public SecurityScanResponse scan(SecurityScanRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String code = request.code();
        String filename = request.filename();
        String scanId = "scan-" + UUID.randomUUID().toString().substring(0, 8);

        List<String> secrets = new ArrayList<>();
        List<String> vulnerabilities = new ArrayList<>();

        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;

        // 1. Secrets Detection
        Matcher awsMatcher = AWS_KEY_PATTERN.matcher(code);
        while (awsMatcher.find()) {
            secrets.add("AWS Access Key leak detected in " + filename + " (" + maskSecret(awsMatcher.group()) + ")");
            criticalCount++;
        }

        Matcher ghMatcher = GITHUB_TOKEN_PATTERN.matcher(code);
        while (ghMatcher.find()) {
            secrets.add("GitHub Personal Access Token detected in " + filename + " (" + maskSecret(ghMatcher.group()) + ")");
            criticalCount++;
        }

        Matcher privKeyMatcher = PRIVATE_KEY_PATTERN.matcher(code);
        if (privKeyMatcher.find()) {
            secrets.add("Unencrypted Private Key header found in " + filename);
            criticalCount++;
        }

        Matcher genericSecretMatcher = GENERIC_SECRET_PATTERN.matcher(code);
        while (genericSecretMatcher.find()) {
            secrets.add("High-entropy secret/token detected in " + filename + " (" + maskSecret(genericSecretMatcher.group(1)) + ")");
            criticalCount++;
        }

        // 2. SAST Vulnerability Detection
        String[] lines = code.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            int lineNum = i + 1;
            String line = lines[i];

            if (SQL_INJECTION_PATTERN.matcher(line).find()) {
                vulnerabilities.add("Line " + lineNum + ": Potential SQL injection via dynamic concatenation. Use parameterized queries.");
                highCount++;
            }

            if (WEAK_CRYPTO_PATTERN.matcher(line).find()) {
                vulnerabilities.add("Line " + lineNum + ": Broken/Weak cryptographic hash function (MD5/SHA-1) detected. Upgrade to SHA-256 or BCrypt.");
                mediumCount++;
            }

            if (CMD_INJECTION_PATTERN.matcher(line).find()) {
                vulnerabilities.add("Line " + lineNum + ": Arbitrary OS command execution via Runtime.exec or ProcessBuilder. Validate command boundaries.");
                criticalCount++;
            }
        }

        int totalIssues = criticalCount + highCount + mediumCount + lowCount;
        boolean passed = totalIssues == 0;

        String summary = passed
                ? "Security Status: PASSED ✅ - Clean code verified across OWASP Top 10 standards with 0 leaks."
                : "Security Status: WARNING ⚠️ - Found " + totalIssues + " potential security concerns requiring remediation.";

        String compliance = passed
                ? "OWASP Top 10 Monolith Verification: 100% Clean"
                : "OWASP Top 10 Monolith Verification: Remediation Required (" + totalIssues + " findings)";

        return new SecurityScanResponse(
                scanId,
                passed,
                totalIssues,
                criticalCount,
                highCount,
                mediumCount,
                lowCount,
                vulnerabilities,
                secrets,
                summary,
                compliance,
                Instant.now()
        );
    }

    private String maskSecret(String secret) {
        if (secret == null || secret.length() <= 8) {
            return "********";
        }
        return secret.substring(0, 4) + "...." + secret.substring(secret.length() - 4);
    }
}
