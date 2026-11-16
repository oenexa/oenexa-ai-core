package com.oenexa.api.dto.observability;

import com.oenexa.domain.model.observability.IncidentObservabilityDomain.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Enterprise DTOs for Incident & Observability Intelligence API.
 */
public final class IncidentObservabilityDtos {

    private IncidentObservabilityDtos() {
    }

    public record MetricAnomalyDto(
            String metricName,
            double observedValue,
            double threshold,
            String unit,
            boolean isAnomaly,
            String details
    ) {
        public static MetricAnomalyDto fromDomain(MetricAnomaly domain) {
            return new MetricAnomalyDto(
                    domain.metricName(),
                    domain.observedValue(),
                    domain.threshold(),
                    domain.unit(),
                    domain.isAnomaly(),
                    domain.details()
            );
        }

        public MetricAnomaly toDomain() {
            return new MetricAnomaly(metricName, observedValue, threshold, unit, isAnomaly, details);
        }
    }

    public record SloViolationDto(
            String serviceName,
            String sloName,
            double currentPercentage,
            double targetPercentage,
            boolean isViolated
    ) {
        public static SloViolationDto fromDomain(SloViolation domain) {
            return new SloViolationDto(
                    domain.serviceName(),
                    domain.sloName(),
                    domain.currentPercentage(),
                    domain.targetPercentage(),
                    domain.isViolated()
            );
        }

        public SloViolation toDomain() {
            return new SloViolation(serviceName, sloName, currentPercentage, targetPercentage, isViolated);
        }
    }

    public record RootCauseAnalysisDto(
            String incidentId,
            String failureCategory,
            String rootCauseSummary,
            List<String> affectedServices,
            List<String> recommendedRunbooks,
            List<String> immediateActionItems,
            double confidenceScore
    ) {
        public static RootCauseAnalysisDto fromDomain(RootCauseAnalysis domain) {
            return new RootCauseAnalysisDto(
                    domain.incidentId(),
                    domain.failureCategory(),
                    domain.rootCauseSummary(),
                    domain.affectedServices(),
                    domain.recommendedRunbooks(),
                    domain.immediateActionItems(),
                    domain.confidenceScore()
            );
        }
    }

    public record IncidentTriageRequestDto(
            String title,
            String service,
            String stackTrace,
            List<MetricAnomalyDto> anomalies,
            List<SloViolationDto> sloViolations,
            Map<String, String> metadata
    ) {
        public IncidentTriageRequest toDomain() {
            List<MetricAnomaly> domainAnomalies = (anomalies == null)
                    ? List.of()
                    : anomalies.stream().map(MetricAnomalyDto::toDomain).toList();
            List<SloViolation> domainSlos = (sloViolations == null)
                    ? List.of()
                    : sloViolations.stream().map(SloViolationDto::toDomain).toList();

            return new IncidentTriageRequest(
                    title != null ? title : "Untitled Incident",
                    service != null ? service : "unknown-service",
                    stackTrace != null ? stackTrace : "",
                    domainAnomalies,
                    domainSlos,
                    metadata != null ? metadata : Map.of()
            );
        }
    }

    public record IncidentTriageResponseDto(
            String incidentId,
            IncidentSeverity calculatedSeverity,
            RootCauseAnalysisDto rca,
            Instant timestamp
    ) {
        public static IncidentTriageResponseDto fromDomain(IncidentTriageResponse domain) {
            return new IncidentTriageResponseDto(
                    domain.incidentId(),
                    domain.calculatedSeverity(),
                    RootCauseAnalysisDto.fromDomain(domain.rca()),
                    domain.timestamp()
            );
        }
    }

    public record IncidentDossierDto(
            String incidentId,
            String title,
            String primaryService,
            IncidentSeverity severity,
            IncidentStatus status,
            Instant reportedAt,
            List<MetricAnomalyDto> anomalies,
            List<SloViolationDto> sloViolations,
            RootCauseAnalysisDto rca
    ) {
        public static IncidentDossierDto fromDomain(IncidentDossier domain) {
            return new IncidentDossierDto(
                    domain.incidentId(),
                    domain.title(),
                    domain.primaryService(),
                    domain.severity(),
                    domain.status(),
                    domain.reportedAt(),
                    domain.anomalies().stream().map(MetricAnomalyDto::fromDomain).toList(),
                    domain.sloViolations().stream().map(SloViolationDto::fromDomain).toList(),
                    RootCauseAnalysisDto.fromDomain(domain.rca())
            );
        }
    }
}
