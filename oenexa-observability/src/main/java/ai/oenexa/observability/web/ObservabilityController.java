package ai.oenexa.observability.web;

import ai.oenexa.observability.domain.ErrorGroup;
import ai.oenexa.observability.domain.IncidentSeverity;
import ai.oenexa.observability.domain.IncidentTimeline;
import ai.oenexa.observability.domain.LogAnalyticsSummary;
import ai.oenexa.observability.domain.LogEvent;
import ai.oenexa.observability.service.ObservabilityService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/observability")
public class ObservabilityController {

    private final ObservabilityService observabilityService;

    public ObservabilityController(ObservabilityService observabilityService) {
        this.observabilityService = Objects.requireNonNull(observabilityService, "observabilityService must not be null");
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Integer>> ingest(@RequestBody List<LogEvent> events) {
        observabilityService.ingestLogs(events);
        return ResponseEntity.ok(Map.of("ingested", events != null ? events.size() : 0));
    }

    @GetMapping("/analytics")
    public ResponseEntity<LogAnalyticsSummary> analytics(@RequestParam(required = false) String service,
                                                        @RequestParam(required = false) String since) {
        Instant sinceTime = since != null ? Instant.parse(since) : null;
        return ResponseEntity.ok(observabilityService.getAnalyticsSummary(service, sinceTime));
    }

    @GetMapping("/errors")
    public ResponseEntity<List<ErrorGroup>> getErrors(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(observabilityService.getTopErrorGroups(limit));
    }

    @PostMapping("/incident")
    public ResponseEntity<IncidentTimeline> correlateIncident(@RequestBody Map<String, String> payload) {
        String title = payload.getOrDefault("title", "Degradation Incident");
        String sevStr = payload.getOrDefault("severity", "P2_HIGH");
        IncidentSeverity severity = IncidentSeverity.valueOf(sevStr);
        String startStr = payload.get("startTime");
        String endStr = payload.get("endTime");

        Instant start = startStr != null ? Instant.parse(startStr) : null;
        Instant end = endStr != null ? Instant.parse(endStr) : null;

        return ResponseEntity.ok(observabilityService.correlateIncident(title, start, end, severity));
    }
}
