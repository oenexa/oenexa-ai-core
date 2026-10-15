package ai.oenexa.security.service;

import ai.oenexa.security.domain.SecretFinding;
import ai.oenexa.security.domain.SecurityScanRequest;
import ai.oenexa.security.domain.SecurityScanResult;
import ai.oenexa.security.domain.VulnerabilityFinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Production implementation of SAST vulnerability scanner and secrets detection.
 */
@Service
public class DefaultSecurityService implements SecurityService {

    private static final Pattern AWS_KEY_PATTERN = Pattern.compile("AKIA[0-9A-Z]{16}");
    private static final Pattern GITHUB_TOKEN_PATTERN = Pattern.compile("ghp_[0-9a-zA-Z]{36}");
    private static final Pattern PRIVATE_KEY_PATTERN = Pattern.compile("-----BEGIN (?:[A-Z0-9_-]+\\s+)?(?:PRIVATE\\s+)?KEY-----");
    private static final Pattern GENERIC_API_KEY_PATTERN = Pattern.compile("(?i)(?:api[_-]?key|secret[_-]?key|private[_-]?key|token)\\s*[:=]\\s*\\\\?[\"']([A-Za-z0-9_\\-]{16,})\\\\?[\"']");

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile("(?i)(?:SELECT|INSERT|UPDATE|DELETE).*\\+\\s*[a-zA-Z_]\\w*");
    private static final Pattern WEAK_CRYPTO_PATTERN = Pattern.compile("(?i)MessageDigest\\.getInstance\\([\"'](MD5|SHA-1)[\"']\\)");
    private static final Pattern CMD_INJECTION_PATTERN = Pattern.compile("Runtime\\.getRuntime\\(\\)\\.exec\\(");

    @Override
    public SecurityScanResult scan(SecurityScanRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String code = request.sourceCode();
        String filename = request.filename();
        String scanId = "scan-" + UUID.randomUUID().toString().substring(0, 8);

        List<VulnerabilityFinding> vulnerabilities = new ArrayList<>();
        List<SecretFinding> secrets = scanSecrets(code, filename);

        // Convert secrets into CRITICAL findings
        for (SecretFinding secret : secrets) {
            vulnerabilities.add(new VulnerabilityFinding(
                    "finding-" + UUID.randomUUID().toString().substring(0, 6),
                    "SEC-SECRETS-001",
                    VulnerabilityFinding.OwaspCategory.SECRETS_LEAK,
                    VulnerabilityFinding.Severity.CRITICAL,
                    secret.file(),
                    secret.line(),
                    "Hardcoded secret detected: " + secret.type() + " (" + secret.maskedValue() + ")",
                    "Remove secret immediately and rotate credential. Use environment variables or Vault."
            ));
        }

        // Line by line SAST inspection
        String[] lines = code.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            int lineNum = i + 1;
            String line = lines[i];

            if (SQL_INJECTION_PATTERN.matcher(line).find()) {
                vulnerabilities.add(new VulnerabilityFinding(
                        "finding-" + UUID.randomUUID().toString().substring(0, 6),
                        "SAST-INJ-001",
                        VulnerabilityFinding.OwaspCategory.A03_INJECTION,
                        VulnerabilityFinding.Severity.HIGH,
                        filename,
                        lineNum,
                        "Potential SQL injection via string concatenation in query",
                        "Use parameterized queries, PreparedStatement, or JPA Criteria API"
                ));
            }

            if (WEAK_CRYPTO_PATTERN.matcher(line).find()) {
                vulnerabilities.add(new VulnerabilityFinding(
                        "finding-" + UUID.randomUUID().toString().substring(0, 6),
                        "SAST-CRYPTO-001",
                        VulnerabilityFinding.OwaspCategory.A02_CRYPTOGRAPHIC_FAILURES,
                        VulnerabilityFinding.Severity.MEDIUM,
                        filename,
                        lineNum,
                        "Weak cryptographic hashing algorithm (MD5/SHA-1) used",
                        "Upgrade to SHA-256, SHA-512, or modern password hashes (PBKDF2, Argon2, BCrypt)"
                ));
            }

            if (CMD_INJECTION_PATTERN.matcher(line).find()) {
                vulnerabilities.add(new VulnerabilityFinding(
                        "finding-" + UUID.randomUUID().toString().substring(0, 6),
                        "SAST-CMD-001",
                        VulnerabilityFinding.OwaspCategory.A03_INJECTION,
                        VulnerabilityFinding.Severity.CRITICAL,
                        filename,
                        lineNum,
                        "Direct OS command execution via Runtime.getRuntime().exec()",
                        "Avoid OS process spawning or strictly validate commands using ProcessBuilder with explicit arguments"
                ));
            }
        }

        int critical = (int) vulnerabilities.stream().filter(v -> v.severity() == VulnerabilityFinding.Severity.CRITICAL).count();
        int high = (int) vulnerabilities.stream().filter(v -> v.severity() == VulnerabilityFinding.Severity.HIGH).count();
        int medium = (int) vulnerabilities.stream().filter(v -> v.severity() == VulnerabilityFinding.Severity.MEDIUM).count();
        int low = (int) vulnerabilities.stream().filter(v -> v.severity() == VulnerabilityFinding.Severity.LOW).count();
        int total = vulnerabilities.size();

        boolean passed = critical == 0 && high == 0;
        String summary = String.format("Security Scan %s: %s. Total Findings: %d (Critical: %d, High: %d, Medium: %d, Low: %d).",
                scanId,
                passed ? "PASSED" : "FAILED",
                total,
                critical,
                high,
                medium,
                low
        );

        return new SecurityScanResult(scanId, passed, total, critical, high, medium, low, vulnerabilities, secrets, summary);
    }

    @Override
    public List<SecretFinding> scanSecrets(String sourceCode, String filename) {
        List<SecretFinding> findings = new ArrayList<>();
        if (sourceCode == null || sourceCode.isEmpty()) {
            return findings;
        }

        String[] lines = sourceCode.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            int lineNum = i + 1;
            String line = lines[i];

            Matcher awsMatcher = AWS_KEY_PATTERN.matcher(line);
            while (awsMatcher.find()) {
                findings.add(new SecretFinding(
                        SecretFinding.SecretType.AWS_ACCESS_KEY,
                        SecretFinding.mask(awsMatcher.group()),
                        filename,
                        lineNum
                ));
            }

            Matcher ghMatcher = GITHUB_TOKEN_PATTERN.matcher(line);
            while (ghMatcher.find()) {
                findings.add(new SecretFinding(
                        SecretFinding.SecretType.GITHUB_TOKEN,
                        SecretFinding.mask(ghMatcher.group()),
                        filename,
                        lineNum
                ));
            }

            Matcher privMatcher = PRIVATE_KEY_PATTERN.matcher(line);
            while (privMatcher.find()) {
                findings.add(new SecretFinding(
                        SecretFinding.SecretType.PRIVATE_KEY,
                        "-----BEGIN PRIVATE KEY-----",
                        filename,
                        lineNum
                ));
            }

            Matcher apiKeyMatcher = GENERIC_API_KEY_PATTERN.matcher(line);
            while (apiKeyMatcher.find()) {
                boolean alreadyMatched = findings.stream().anyMatch(f -> f.line() == lineNum);
                if (!alreadyMatched) {
                    findings.add(new SecretFinding(
                            SecretFinding.SecretType.GENERIC_API_KEY,
                            SecretFinding.mask(apiKeyMatcher.group(1)),
                            filename,
                            lineNum
                    ));
                }
            }
        }

        return findings;
    }
}
