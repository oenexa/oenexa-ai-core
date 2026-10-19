package ai.oenexa.observability.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Fingerprinted group of related errors and stack traces.
 */
public record ErrorGroup(
        String fingerprint,
        String errorType,
        String rootMessage,
        int occurrenceCount,
        Instant firstSeen,
        Instant lastSeen,
        List<String> affectedServices
) {
    public ErrorGroup {
        Objects.requireNonNull(fingerprint, "fingerprint must not be null");
        Objects.requireNonNull(errorType, "errorType must not be null");
        Objects.requireNonNull(rootMessage, "rootMessage must not be null");
        if (firstSeen == null) {
            firstSeen = Instant.now();
        }
        if (lastSeen == null) {
            lastSeen = Instant.now();
        }
        if (affectedServices == null) {
            affectedServices = List.of();
        }
    }
}
