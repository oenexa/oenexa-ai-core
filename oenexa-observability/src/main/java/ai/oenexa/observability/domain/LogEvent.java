package ai.oenexa.observability.domain;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Structured telemetry log event.
 */
public record LogEvent(
        String id,
        Instant timestamp,
        LogLevel level,
        String service,
        String logger,
        String message,
        String stackTrace,
        Map<String, String> tags
) {
    public LogEvent {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(level, "level must not be null");
        Objects.requireNonNull(message, "message must not be null");
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (service == null || service.isBlank()) {
            service = "default-service";
        }
        if (logger == null) {
            logger = "ROOT";
        }
        if (stackTrace == null) {
            stackTrace = "";
        }
        if (tags == null) {
            tags = Map.of();
        }
    }
}
