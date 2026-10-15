package ai.oenexa.observability.service;

import ai.oenexa.observability.domain.ErrorGroup;
import ai.oenexa.observability.domain.IncidentSeverity;
import ai.oenexa.observability.domain.IncidentTimeline;
import ai.oenexa.observability.domain.LogAnalyticsSummary;
import ai.oenexa.observability.domain.LogEvent;
import ai.oenexa.observability.domain.LogLevel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class DefaultObservabilityService implements ObservabilityService {

    private final List<LogEvent> logStore = new CopyOnWriteArrayList<>();
    private final Map<String, ErrorGroup> errorGroupStore = new ConcurrentHashMap<>();

    @Override
    public void ingestLogs(List<LogEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (LogEvent event : events) {
            logStore.add(event);

            if (event.level() == LogLevel.ERROR || event.level() == LogLevel.FATAL) {
                String errorType = event.logger().contains(".")
                        ? event.logger().substring(event.logger().lastIndexOf('.') + 1)
                        : event.logger();
                String fingerprint = "err-" + Math.abs((errorType + ":" + event.message()).hashCode());

                errorGroupStore.compute(fingerprint, (fp, existing) -> {
                    if (existing == null) {
                        return new ErrorGroup(
                                fp,
                                errorType,
                                event.message(),
                                1,
                                event.timestamp(),
                                event.timestamp(),
                                List.of(event.service())
                        );
                    } else {
                        List<String> services = new ArrayList<>(existing.affectedServices());
                        if (!services.contains(event.service())) {
                            services.add(event.service());
                        }
                        return new ErrorGroup(
                                fp,
                                existing.errorType(),
                                existing.rootMessage(),
                                existing.occurrenceCount() + 1,
                                existing.firstSeen(),
                                event.timestamp(),
                                services
                        );
                    }
                });
            }
        }
    }

    @Override
    public LogAnalyticsSummary getAnalyticsSummary(String serviceFilter, Instant since) {
        List<LogEvent> filtered = logStore.stream()
                .filter(l -> serviceFilter == null || serviceFilter.isBlank() || serviceFilter.equals(l.service()))
                .filter(l -> since == null || !l.timestamp().isBefore(since))
                .toList();

        long total = filtered.size();
        long errors = filtered.stream().filter(l -> l.level() == LogLevel.ERROR || l.level() == LogLevel.FATAL).count();
        long warns = filtered.stream().filter(l -> l.level() == LogLevel.WARN).count();
        double errorRate = total > 0 ? Math.round(((errors * 100.0) / total) * 100.0) / 100.0 : 0.0;

        List<ErrorGroup> topGroups = getTopErrorGroups(5);
        return new LogAnalyticsSummary(total, errors, warns, errorGroupStore.size(), topGroups, errorRate);
    }

    @Override
    public List<ErrorGroup> getTopErrorGroups(int limit) {
        int effectiveLimit = limit <= 0 ? 10 : limit;
        return errorGroupStore.values().stream()
                .sorted(Comparator.comparingInt(ErrorGroup::occurrenceCount).reversed())
                .limit(effectiveLimit)
                .toList();
    }

    @Override
    public IncidentTimeline correlateIncident(String incidentTitle, Instant startTime, Instant endTime, IncidentSeverity severity) {
        Objects.requireNonNull(incidentTitle, "incidentTitle must not be null");

        Instant start = startTime != null ? startTime : Instant.now().minusSeconds(3600);
        Instant end = endTime != null ? endTime : Instant.now();

        List<LogEvent> incidentLogs = logStore.stream()
                .filter(l -> !l.timestamp().isBefore(start) && !l.timestamp().isAfter(end))
                .filter(l -> l.level() == LogLevel.ERROR || l.level() == LogLevel.FATAL || l.level() == LogLevel.WARN)
                .sorted(Comparator.comparing(LogEvent::timestamp))
                .toList();

        List<String> eventSequence = new ArrayList<>();
        String rootCause = "No critical error signatures detected in the specified timeframe";

        for (LogEvent log : incidentLogs) {
            eventSequence.add(String.format("[%s] %s: %s", log.level(), log.service(), log.message()));
            if (log.level() == LogLevel.ERROR || log.level() == LogLevel.FATAL) {
                rootCause = String.format("Service '%s' failed: %s", log.service(), log.message());
            }
        }

        String incidentId = "inc-" + UUID.randomUUID().toString().substring(0, 8);
        return new IncidentTimeline(
                incidentId,
                incidentTitle,
                severity != null ? severity : IncidentSeverity.P2_HIGH,
                start,
                end,
                eventSequence,
                rootCause
        );
    }
}
