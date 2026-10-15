package ai.oenexa.security.service;

import ai.oenexa.security.domain.SecretFinding;
import ai.oenexa.security.domain.SecurityScanRequest;
import ai.oenexa.security.domain.SecurityScanResult;
import java.util.List;

/**
 * Service contract for SAST vulnerability detection and secrets auditing.
 */
public interface SecurityService {

    SecurityScanResult scan(SecurityScanRequest request);

    List<SecretFinding> scanSecrets(String sourceCode, String filename);
}
