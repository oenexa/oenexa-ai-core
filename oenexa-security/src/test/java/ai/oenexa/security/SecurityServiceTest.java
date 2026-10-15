package ai.oenexa.security;

import ai.oenexa.security.domain.SecretFinding;
import ai.oenexa.security.domain.SecurityScanRequest;
import ai.oenexa.security.domain.SecurityScanResult;
import ai.oenexa.security.domain.VulnerabilityFinding;
import ai.oenexa.security.service.DefaultSecurityService;
import ai.oenexa.security.web.SecurityController;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OENEXA Security Service & Controller Tests (Zero-Mock Policy)")
class SecurityServiceTest {

    private DefaultSecurityService securityService;
    private SecurityController securityController;

    @BeforeEach
    void setUp() {
        securityService = new DefaultSecurityService();
        securityController = new SecurityController(securityService);
    }

    @Test
    @DisplayName("Secret masking utility invariant")
    void testSecretMasking() {
        assertEquals("******", SecretFinding.mask("12345"));
        assertEquals("******", SecretFinding.mask(null));
        assertEquals("AKI...XYZ", SecretFinding.mask("AKIA1234567890XYZ"));
    }

    @Test
    @DisplayName("Clean source code passes security scan")
    void testCleanCodeScan() {
        String cleanSource = """
                package com.example;
                public record CleanEntity(String id, String name) {}
                """;
        SecurityScanRequest req = new SecurityScanRequest("test-repo", cleanSource, "CleanEntity.java", List.of("SAST"));
        SecurityScanResult result = securityService.scan(req);

        assertTrue(result.passed());
        assertEquals(0, result.totalIssues());
        assertEquals(0, result.criticalCount());
        assertEquals(0, result.highCount());
        assertTrue(result.vulnerabilities().isEmpty());
    }

    @Test
    @DisplayName("Detect hardcoded AWS access key and GitHub token")
    void testDetectSecrets() {
        String codeWithSecrets = """
                String awsKey = "AKIA1234567890ABCDEF";
                String ghToken = "ghp_1234567890abcdefghijklmnopqrstuvwxyz";
                String privateKey = "-----BEGIN RSA PRIVATE KEY-----";
                String apiKey = "1234567890123456789012345";
                """;
        List<SecretFinding> secrets = securityService.scanSecrets(codeWithSecrets, "Config.java");
        assertEquals(4, secrets.size());
        assertTrue(secrets.stream().anyMatch(s -> s.type() == SecretFinding.SecretType.AWS_ACCESS_KEY));
        assertTrue(secrets.stream().anyMatch(s -> s.type() == SecretFinding.SecretType.GITHUB_TOKEN));
        assertTrue(secrets.stream().anyMatch(s -> s.type() == SecretFinding.SecretType.PRIVATE_KEY));
        assertTrue(secrets.stream().anyMatch(s -> s.type() == SecretFinding.SecretType.GENERIC_API_KEY));
    }

    @Test
    @DisplayName("Detect SQL Injection and Weak Cryptography")
    void testDetectSastVulnerabilities() {
        String vulnerableSource = """
                public class UserDao {
                    public void findUser(String username) {
                        String query = "SELECT * FROM users WHERE username = '" + username + "'";
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        Runtime.getRuntime().exec("sh script.sh " + username);
                    }
                }
                """;
        SecurityScanRequest req = new SecurityScanRequest("repo", vulnerableSource, "UserDao.java", List.of("SAST"));
        SecurityScanResult result = securityService.scan(req);

        assertFalse(result.passed());
        assertTrue(result.totalIssues() >= 3);
        assertTrue(result.criticalCount() >= 1); // Runtime.exec
        assertTrue(result.highCount() >= 1);     // SQL Injection
        assertTrue(result.mediumCount() >= 1);   // MD5
        assertTrue(result.vulnerabilities().stream().anyMatch(v -> v.category() == VulnerabilityFinding.OwaspCategory.A03_INJECTION));
        assertTrue(result.vulnerabilities().stream().anyMatch(v -> v.category() == VulnerabilityFinding.OwaspCategory.A02_CRYPTOGRAPHIC_FAILURES));
    }

    @Test
    @DisplayName("SecurityController scan and secrets endpoints return valid responses")
    void testSecurityController() {
        SecurityScanRequest req = new SecurityScanRequest("local", "public class A {}", "A.java", null);
        ResponseEntity<SecurityScanResult> scanResp = securityController.scan(req);
        assertEquals(200, scanResp.getStatusCode().value());
        assertTrue(scanResp.getBody().passed());

        ResponseEntity<List<SecretFinding>> secResp = securityController.scanSecrets(Map.of("code", "AKIA1234567890ABCDEF", "filename", "Creds.txt"));
        assertEquals(200, secResp.getStatusCode().value());
        assertEquals(1, secResp.getBody().size());
    }

    @Test
    @DisplayName("SecurityModule metadata and initialization")
    void testSecurityModuleMetadata() {
        SecurityModule module = new SecurityModule();
        assertEquals("security", module.id());
        assertEquals("OENEXA Security Center", module.displayName());
        assertTrue(module.capabilities().contains("owasp-top-10"));
    }

    @Test
    @DisplayName("Domain records nullability and fallback invariants")
    void testDomainRecordsEdgeCases() {
        SecretFinding secret = new SecretFinding(SecretFinding.SecretType.AWS_ACCESS_KEY, "******", null, 1);
        assertEquals("unknown", secret.file());

        VulnerabilityFinding vuln = new VulnerabilityFinding(
                "v-1", "RULE-1", VulnerabilityFinding.OwaspCategory.A01_BROKEN_ACCESS_CONTROL,
                VulnerabilityFinding.Severity.INFO, null, 1, "desc", null
        );
        assertEquals("unknown", vuln.file());
        assertEquals("", vuln.remediation());

        SecurityScanResult scanRes = new SecurityScanResult("s-1", true, 0, 0, 0, 0, 0, null, null, null);
        assertTrue(scanRes.vulnerabilities().isEmpty());
        assertTrue(scanRes.secrets().isEmpty());
        assertEquals("", scanRes.summary());

        assertEquals(0, securityService.scanSecrets(null, "test.java").size());
        assertEquals(0, securityService.scanSecrets("", "test.java").size());

        // Test alreadyMatched branch where AWS key line also has generic key token
        String multiTokenLine = "String key = \"AKIA1234567890ABCDEF\"; // apiKey = \"1234567890123456789012345\"";
        List<SecretFinding> findings = securityService.scanSecrets(multiTokenLine, "Keys.java");
        assertFalse(findings.isEmpty());

        // Test scan with secrets to trigger vulnerability conversion loop
        SecurityScanRequest secretScanReq = new SecurityScanRequest("repo", multiTokenLine, "Keys.java", null);
        SecurityScanResult secretScanResult = securityService.scan(secretScanReq);
        assertFalse(secretScanResult.passed());
        assertTrue(secretScanResult.criticalCount() >= 1);
        assertTrue(secretScanResult.vulnerabilities().stream().anyMatch(v -> v.category() == VulnerabilityFinding.OwaspCategory.SECRETS_LEAK));
    }
}
