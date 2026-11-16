package com.oenexa.domain.model.observability;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Domain records and invariants for Incident & Observability Intelligence.
 */
public final class IncidentObservabilityDomain {

    private IncidentObservabilityDomain() {
    }

    public enum IncidentSeverity {
        P1_CRITICAL,
        P2_HIGH,
        P3_MEDIUM,
        P4_LOW
    }

    public enum IncidentStatus {
        OPEN,
        TRIAGED,
        INVESTIGATING,
        MITIGATING,
        RESOLVED
    }

    public record MetricAnomaly(
            String metricName,
            double observedValue,
            double threshold,
            String unit,
            boolean isAnomaly,
            String details
    ) {
        public MetricAnomaly {
            Objects.requireNonNull(metricName, "metricName must not be null");
            unit = unit == null ? "" : unit;
            details = details == null ? "" : details;
        }
    }

    public record SloViolation(
            String serviceName,
            String sloName,
            double currentPercentage,
            double targetPercentage,
            boolean isViolated
    ) {
        public SloViolation {
            Objects.requireNonNull(serviceName, "serviceName must not be null");
            Objects.requireNonNull(sloName, "sloName must not be null");
        }
    }

    public record RootCauseAnalysis(
            String incidentId,
            String failureCategory,
            String rootCauseSummary,
            List<String> affectedServices,
            List<String> recommendedRunbooks,
            List<String> immediateActionItems,
            double confidenceScore
    ) {
        public RootCauseAnalysis {
            Objects.requireNonNull(incidentId, "incidentId must not be null");
            failureCategory = failureCategory == null ? "UNKNOWN" : failureCategory;
            rootCauseSummary = rootCauseSummary == null ? "" : rootCauseSummary;
            affectedServices = affectedServices == null ? List.of() : List.copyOf(affectedServices);
            recommendedRunbooks = recommendedRunbooks == null ? List.of() : List.copyOf(recommendedRunbooks);
            immediateActionItems = immediateActionItems == null ? List.of() : List.copyOf(immediateActionItems);
            confidenceScore = Math.clamp(confidenceScore, 0.0, 1.0);
        }
    }

    public record IncidentDossier(
            String incidentId,
            String title,
            String primaryService,
            IncidentSeverity severity,
            IncidentStatus status,
            Instant reportedAt,
            List<MetricAnomaly> anomalies,
            List<SloViolation> sloViolations,
            RootCauseAnalysis rca
    ) {
        public IncidentDossier {
            Objects.requireNonNull(incidentId, "incidentId must not be null");
            Objects.requireNonNull(title, "title must not be null");
            Objects.requireNonNull(primaryService, "primaryService must not be null");
            Objects.requireNonNull(severity, "severity must not be null");
            Objects.requireNonNull(status, "status must not be null");
            reportedAt = reportedAt == null ? Instant.now() : reportedAt;
            anomalies = anomalies == null ? List.of() : List.copyOf(anomalies);
            sloViolations = sloViolations == null ? List.of() : List.copyOf(sloViolations);
        }
    }

    public record IncidentTriageRequest(
            String title,
            String service,
            String stackTrace,
            List<MetricAnomaly> anomalies,
            List<SloViolation> sloViolations,
            Map<String, String> metadata
    ) {
        public IncidentTriageRequest {
            Objects.requireNonNull(title, "title must not be null");
            Objects.requireNonNull(service, "service must not be null");
            stackTrace = stackTrace == null ? "" : stackTrace;
            anomalies = anomalies == null ? List.of() : List.copyOf(anomalies);
            sloViolations = sloViolations == null ? List.of() : List.copyOf(sloViolations);
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    public record IncidentTriageResponse(
            String incidentId,
            IncidentSeverity calculatedSeverity,
            RootCauseAnalysis rca,
            Instant timestamp
    ) {
        public IncidentTriageResponse {
            Objects.requireNonNull(incidentId, "incidentId must not be null");
            Objects.requireNonNull(calculatedSeverity, "calculatedSeverity must not be null");
            Objects.requireNonNull(rca, "rca must not be null");
            timestamp = timestamp == null ? Instant.now() : timestamp;
        }
    }
}
