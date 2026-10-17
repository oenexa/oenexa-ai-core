package com.oenexa.application.engine;

import com.oenexa.domain.CodeEngineeringDomain.SecurityScanRequest;
import com.oenexa.domain.CodeEngineeringDomain.SecurityScanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityAuditEngine Zero-Mock Unit Tests")
class SecurityAuditEngineTest {

    private SecurityAuditEngine engine;

    @BeforeEach
    void setUp() {
        engine = new SecurityAuditEngine();
    }

    @Test
    @DisplayName("Should reject null request with NullPointerException")
    void shouldRejectNullRequest() {
        assertThrows(NullPointerException.class, () -> engine.scan(null));
    }

    @Test
    @DisplayName("Should pass security scan when code is completely clean")
    void shouldPassCleanCode() {
        String cleanCode = """
                package com.example;

                public class CleanService {
                    public String greet(String name) {
                        return "Hello, " + name;
                    }
                }
                """;

        SecurityScanRequest request = new SecurityScanRequest(cleanCode, "CleanService.java");
        SecurityScanResponse response = engine.scan(request);

        assertNotNull(response);
        assertTrue(response.passed());
        assertEquals(0, response.totalIssues());
        assertEquals(0, response.criticalCount());
        assertEquals(0, response.highCount());
        assertEquals(0, response.mediumCount());
        assertEquals(0, response.lowCount());
        assertTrue(response.vulnerabilities().isEmpty());
        assertTrue(response.secrets().isEmpty());
        assertTrue(response.summary().contains("PASSED"));
        assertTrue(response.compliance().contains("100% Clean"));
        assertNotNull(response.timestamp());
    }

    @Test
    @DisplayName("Should detect AWS access key and mask the secret")
    void shouldDetectAwsAccessKey() {
        String code = "String awsKey = \"AKIA1234567890ABCDEF\";";
        SecurityScanRequest request = new SecurityScanRequest(code, "AwsConfig.java");
        SecurityScanResponse response = engine.scan(request);

        assertNotNull(response);
        assertFalse(response.passed());
        assertTrue(response.criticalCount() >= 1);
        assertEquals(1, response.secrets().size());
        assertTrue(response.secrets().get(0).contains("AKIA....CDEF"));
        assertTrue(response.summary().contains("WARNING"));
    }

    @Test
    @DisplayName("Should detect GitHub token and mask the secret")
    void shouldDetectGitHubToken() {
        String code = "String ghToken = \"ghp_1234567890abcdefghijklmnopqrstuvwxyz\";";
        SecurityScanRequest request = new SecurityScanRequest(code, "GithubService.java");
        SecurityScanResponse response = engine.scan(request);

        assertNotNull(response);
        assertFalse(response.passed());
        assertTrue(response.criticalCount() >= 1);
        assertEquals(1, response.secrets().size());
        assertTrue(response.secrets().get(0).contains("GitHub Personal Access Token"));
    }

    @Test
    @DisplayName("Should detect Private Key header")
    void shouldDetectPrivateKeyHeader() {
        String code = "-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEA...\n-----END RSA PRIVATE KEY-----";
        SecurityScanRequest request = new SecurityScanRequest(code, "id_rsa");
        SecurityScanResponse response = engine.scan(request);

        assertNotNull(response);
        assertFalse(response.passed());
        assertTrue(response.criticalCount() >= 1);
        assertTrue(response.secrets().get(0).contains("Unencrypted Private Key header"));
    }

    @Test
    @DisplayName("Should detect generic high-entropy secret/token")
    void shouldDetectGenericSecret() {
        String code = "String api_key = \"abcdef1234567890xyz\";";
        SecurityScanRequest request = new SecurityScanRequest(code, "Client.java");
        SecurityScanResponse response = engine.scan(request);

        assertNotNull(response);
        assertFalse(response.passed());
        assertTrue(response.criticalCount() >= 1);
        assertTrue(response.secrets().get(0).contains("High-entropy secret/token"));
    }

    @Test
    @DisplayName("Should detect SQL Injection vulnerability")
    void shouldDetectSqlInjection() {
        String code = "String query = \"SELECT * FROM users WHERE id = \" + userId;";
        SecurityScanRequest request = new SecurityScanRequest(code, "UserDao.java");
        SecurityScanResponse response = engine.scan(request);

        assertNotNull(response);
        assertFalse(response.passed());
        assertTrue(response.highCount() >= 1);
        assertTrue(response.vulnerabilities().stream().anyMatch(v -> v.contains("SQL injection")));
    }

    @Test
    @DisplayName("Should detect Weak Cryptography MD5/SHA-1")
    void shouldDetectWeakCryptography() {
        String code = "MessageDigest md = MessageDigest.getInstance(\"MD5\");";
        SecurityScanRequest request = new SecurityScanRequest(code, "Crypto.java");
        SecurityScanResponse response = engine.scan(request);

        assertNotNull(response);
        assertFalse(response.passed());
        assertTrue(response.mediumCount() >= 1);
        assertTrue(response.vulnerabilities().stream().anyMatch(v -> v.contains("Broken/Weak cryptographic hash")));
    }

    @Test
    @DisplayName("Should detect Arbitrary Command Execution")
    void shouldDetectCommandExecution() {
        String code = """
                public void runCmd(String cmd) throws Exception {
                    Runtime.getRuntime().exec(cmd);
                    ProcessBuilder pb = new ProcessBuilder(cmd);
                }
                """;
        SecurityScanRequest request = new SecurityScanRequest(code, "Executor.java");
        SecurityScanResponse response = engine.scan(request);

        assertNotNull(response);
        assertFalse(response.passed());
        assertTrue(response.criticalCount() >= 1);
        assertTrue(response.vulnerabilities().stream().anyMatch(v -> v.contains("Arbitrary OS command execution")));
    }

    @Test
    @DisplayName("Should test SecurityScanRequest default constructors")
    void shouldHandleDefaultConstructors() {
        SecurityScanRequest req = new SecurityScanRequest(null, null);
        assertEquals("", req.code());
        assertEquals("Solution.java", req.filename());

        SecurityScanResponse res = new SecurityScanResponse("scan-123", true, 0, 0, 0, 0, 0, null, null, null, null);
        assertNotNull(res.vulnerabilities());
        assertNotNull(res.secrets());
        assertEquals("", res.summary());
        assertEquals("OWASP Top 10 Monolith Verification", res.compliance());
        assertNotNull(res.timestamp());
    }
}
