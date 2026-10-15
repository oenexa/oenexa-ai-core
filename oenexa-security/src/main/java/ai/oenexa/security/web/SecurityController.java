package ai.oenexa.security.web;

import ai.oenexa.security.domain.SecretFinding;
import ai.oenexa.security.domain.SecurityScanRequest;
import ai.oenexa.security.domain.SecurityScanResult;
import ai.oenexa.security.service.SecurityService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint exposing SAST vulnerability scanning and secrets auditing.
 */
@RestController
@RequestMapping("/api/v1/security")
public class SecurityController {

    private final SecurityService securityService;

    public SecurityController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @PostMapping("/scan")
    public ResponseEntity<SecurityScanResult> scan(@RequestBody SecurityScanRequest request) {
        return ResponseEntity.ok(securityService.scan(request));
    }

    @PostMapping("/secrets")
    public ResponseEntity<List<SecretFinding>> scanSecrets(@RequestBody Map<String, String> payload) {
        String code = payload.getOrDefault("code", "");
        String filename = payload.getOrDefault("filename", "input.txt");
        return ResponseEntity.ok(securityService.scanSecrets(code, filename));
    }
}
