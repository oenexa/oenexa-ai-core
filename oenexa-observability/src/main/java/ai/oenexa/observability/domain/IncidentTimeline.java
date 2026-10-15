package ai.oenexa.observability.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Incident timeline documenting progression of system degradation and resolution.
 */
public record IncidentTimeline(
        String incidentId,
        String title,
        IncidentSeverity severity,
        Instant startTime,
        Instant resolvedTime,
        List<String> eventSequence,
        String suggestedRootCause
) {
    public IncidentTimeline {
        Objects.requireNonNull(incidentId, "incidentId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        if (severity == null) {
            severity = IncidentSeverity.P3_MEDIUM;
        }
        if (startTime == null) {
            startTime = Instant.now();
        }
        if (eventSequence == null) {
            eventSequence = List.of();
        }
        if (suggestedRootCause == null) {
            suggestedRootCause = "Root cause under active investigation";
        }
    }
}
