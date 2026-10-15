package ai.oenexa.observability.service;

import ai.oenexa.observability.domain.ErrorGroup;
import ai.oenexa.observability.domain.IncidentSeverity;
import ai.oenexa.observability.domain.IncidentTimeline;
import ai.oenexa.observability.domain.LogAnalyticsSummary;
import ai.oenexa.observability.domain.LogEvent;
import java.time.Instant;
import java.util.List;

/**
 * Service port for ingesting telemetry logs, grouping errors, and diagnosing incidents.
 */
public interface ObservabilityService {

    void ingestLogs(List<LogEvent> events);

    LogAnalyticsSummary getAnalyticsSummary(String serviceFilter, Instant since);

    List<ErrorGroup> getTopErrorGroups(int limit);

    IncidentTimeline correlateIncident(String incidentTitle, Instant startTime, Instant endTime, IncidentSeverity severity);
}
