package ai.oenexa.security.domain;

import java.util.List;
import java.util.Objects;

/**
 * Request payload for security scanning.
 */
public record SecurityScanRequest(
        String repository,
        String sourceCode,
        String filename,
        List<String> scanTypes
) {
    public SecurityScanRequest {
        if (repository == null) repository = "local";
        if (sourceCode == null) sourceCode = "";
        if (filename == null) filename = "Source.java";
        if (scanTypes == null) scanTypes = List.of("SAST", "SECRETS");
    }
}
