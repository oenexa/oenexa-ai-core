package com.oenexa.application.observability;

import com.oenexa.domain.model.observability.IncidentObservabilityDomain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enterprise Incident & Observability Intelligence Service.
 * Provides automated incident triage, root cause analysis (RCA),
 * SLO violation correlation, and runbook matching.
 */
@Service
public class IncidentObservabilityService {

    private final Map<String, IncidentDossier> dossierRegistry = new ConcurrentHashMap<>();
    private final AtomicLong incidentCounter = new AtomicLong(1000);

    public IncidentTriageResponse triageIncident(IncidentTriageRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String incidentId = "INC-" + incidentCounter.incrementAndGet();
        IncidentSeverity severity = calculateSeverity(request);
        String category = classifyFailureCategory(request);
        List<String> affected = determineAffectedServices(request);
        List<String> runbooks = resolveRunbooks(category);
        List<String> actionItems = generateActionItems(severity, category);
        double confidence = calculateConfidence(request, category);

        String summary = generateRcaSummary(request, category);

        RootCauseAnalysis rca = new RootCauseAnalysis(
                incidentId,
                category,
                summary,
                affected,
                runbooks,
                actionItems,
                confidence
        );

        IncidentDossier dossier = new IncidentDossier(
                incidentId,
                request.title(),
                request.service(),
                severity,
                IncidentStatus.TRIAGED,
                Instant.now(),
                request.anomalies(),
                request.sloViolations(),
                rca
        );

        dossierRegistry.put(incidentId, dossier);

        return new IncidentTriageResponse(
                incidentId,
                severity,
                rca,
                Instant.now()
        );
    }

    public Optional<IncidentDossier> getDossier(String incidentId) {
        return Optional.ofNullable(dossierRegistry.get(incidentId));
    }

    public List<IncidentDossier> listDossiers() {
        return List.copyOf(dossierRegistry.values());
    }

    private IncidentSeverity calculateSeverity(IncidentTriageRequest request) {
        boolean hasCriticalSlo = request.sloViolations().stream()
                .anyMatch(s -> s.isViolated() && (s.currentPercentage() < 95.0 || s.currentPercentage() > s.targetPercentage() * 2));
        boolean hasOom = request.stackTrace().contains("OutOfMemoryError")
                || request.stackTrace().contains("StackOverflowError");
        long severeAnomalies = request.anomalies().stream()
                .filter(a -> a.isAnomaly() && a.observedValue() >= a.threshold() * 1.5)
                .count();

        if (hasCriticalSlo || hasOom || severeAnomalies >= 2) {
            return IncidentSeverity.P1_CRITICAL;
        }

        boolean hasSloViolation = request.sloViolations().stream().anyMatch(SloViolation::isViolated);
        boolean hasException = request.stackTrace().contains("TimeoutException")
                || request.stackTrace().contains("NullPointerException")
                || request.stackTrace().contains("ConnectionRefused");

        if (hasSloViolation || hasException || severeAnomalies >= 1) {
            return IncidentSeverity.P2_HIGH;
        }

        if (!request.anomalies().isEmpty()) {
            return IncidentSeverity.P3_MEDIUM;
        }

        return IncidentSeverity.P4_LOW;
    }

    private String classifyFailureCategory(IncidentTriageRequest request) {
        String trace = request.stackTrace().toLowerCase();
        if (trace.contains("outofmemory") || trace.contains("heap") || trace.contains("gc overhead")) {
            return "JVM_RESOURCE_EXHAUSTION";
        }
        if (trace.contains("hikaricp") || trace.contains("connection refused") || trace.contains("timeout") || trace.contains("sql")) {
            return "DATABASE_INFRASTRUCTURE";
        }
        if (trace.contains("jwt") || trace.contains("unauthorized") || trace.contains("forbidden") || trace.contains("security")) {
            return "AUTHENTICATION_AUTHORIZATION";
        }
        if (trace.contains("nullpointer") || trace.contains("illegalargument") || trace.contains("classnotfound")) {
            return "APPLICATION_LOGIC";
        }
        if (request.anomalies().stream().anyMatch(a -> a.metricName().contains("latency") || a.metricName().contains("cpu"))) {
            return "COMPUTE_SATURATION";
        }
        return "UNCLASSIFIED_INCIDENT";
    }

    private List<String> determineAffectedServices(IncidentTriageRequest request) {
        Set<String> services = new LinkedHashSet<>();
        services.add(request.service());
        for (SloViolation slo : request.sloViolations()) {
            services.add(slo.serviceName());
        }
        return List.copyOf(services);
    }

    private List<String> resolveRunbooks(String category) {
        return switch (category) {
            case "JVM_RESOURCE_EXHAUSTION" -> List.of(
                    "RUNBOOK-JVM-001: Kubernetes Pod Memory Heap Profiling & Auto-restart",
                    "RUNBOOK-K8S-004: Horizontal Pod Autoscaler Trigger"
            );
            case "DATABASE_INFRASTRUCTURE" -> List.of(
                    "RUNBOOK-DB-002: PostgreSQL HikariCP Pool Drain & Read Replica Failover",
                    "RUNBOOK-DB-005: Slow Query Kill & Index Optimization"
            );
            case "AUTHENTICATION_AUTHORIZATION" -> List.of(
                    "RUNBOOK-SEC-005: IdP Federation Health & Key Rotation",
                    "RUNBOOK-SEC-008: Edge Rate-Limiting & IP Throttling"
            );
            case "APPLICATION_LOGIC" -> List.of(
                    "RUNBOOK-APP-003: Canary Rollback & Feature Flag Isolation",
                    "RUNBOOK-APP-007: Exception Stack Trace Diagnostics"
            );
            default -> List.of(
                    "RUNBOOK-OPS-000: Standard SRE Escalation & Log Correlator"
            );
        };
    }

    private List<String> generateActionItems(IncidentSeverity severity, String category) {
        List<String> items = new ArrayList<>();
        if (severity == IncidentSeverity.P1_CRITICAL) {
            items.add("Notify Tier-1 Incident Commander on #incident-critical");
            items.add("Engage Service Owner & Platform SRE on-call rotation");
        }
        items.add("Execute primary runbook for category: " + category);
        items.add("Capture container heap/thread dump and prometheus metrics snapshot");
        return List.copyOf(items);
    }

    private double calculateConfidence(IncidentTriageRequest request, String category) {
        if ("UNCLASSIFIED_INCIDENT".equals(category)) {
            return 0.65;
        }
        boolean hasTrace = !request.stackTrace().isBlank();
        boolean hasMetrics = !request.anomalies().isEmpty();
        if (hasTrace && hasMetrics) {
            return 0.96;
        }
        return 0.88;
    }

    private String generateRcaSummary(IncidentTriageRequest request, String category) {
        return String.format(
                "Automated RCA identified category '%s' affecting primary service '%s'. Correlated with %d anomalies and %d SLO violations.",
                category,
                request.service(),
                request.anomalies().size(),
                request.sloViolations().size()
        );
    }
}
