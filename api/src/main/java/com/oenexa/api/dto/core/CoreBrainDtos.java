package com.oenexa.api.dto.core;

import java.util.List;
import java.util.Map;

/**
 * Standard Data Transfer Objects for OENEXA Central Brain & Gateway.
 */
public final class CoreBrainDtos {

    private CoreBrainDtos() {}

    public record CoreBrainDirectiveRequestDto(
            String directiveId,
            String objective,
            String priority,
            Map<String, Object> parameters
    ) {}

    public record CoreBrainDirectiveResultDto(
            String executionId,
            String status,
            String planSummary,
            List<String> executedPhases,
            double confidenceScore
    ) {}

    public record ObservabilityTelemetryDto(
            String spanId,
            String componentName,
            long latencyMs,
            boolean healthy,
            Map<String, Object> metrics
    ) {}
}
