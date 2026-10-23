package com.oenexa.application.core;

import com.oenexa.domain.OenexaCoreBrainDomain.ObservabilitySpan;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core Module 12: Observability Engine.
 * Provides distributed tracing spans, runtime latency metrics, error event tracking,
 * and AI performance analytics (tokens/sec, model reliability) for the OENEXA ecosystem.
 */
@Service
public class OenexaObservabilityEngine {

    private final List<ObservabilitySpan> spans = new CopyOnWriteArrayList<>();
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final AtomicLong totalDirectivesProcessed = new AtomicLong(0);

    public ObservabilitySpan startAndRecordSpan(
            String engineName,
            String operation,
            long durationMs,
            String status,
            Map<String, String> tags
    ) {
        String traceId = "tr-" + UUID.randomUUID().toString().substring(0, 8);
        String spanId = "sp-" + UUID.randomUUID().toString().substring(0, 8);

        var span = new ObservabilitySpan(
                traceId,
                spanId,
                engineName != null ? engineName : "OENEXA_CORE",
                operation != null ? operation : "DIRECTIVE_PROCESSING",
                Math.max(1L, durationMs),
                status != null ? status : "OK",
                tags != null ? tags : Map.of()
        );

        spans.add(span);
        totalDirectivesProcessed.incrementAndGet();
        if ("ERROR".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)) {
            errorCounts.computeIfAbsent(operation, k -> new AtomicLong(0)).incrementAndGet();
        }

        // Keep rolling buffer of last 500 spans to prevent memory leaks
        if (spans.size() > 500) {
            spans.removeFirst();
        }

        return span;
    }

    public List<ObservabilitySpan> getRecentSpans() {
        return List.copyOf(spans);
    }

    public long getTotalProcessedCount() {
        return totalDirectivesProcessed.get();
    }

    public Map<String, Long> getErrorMetrics() {
        Map<String, Long> result = new HashMap<>();
        errorCounts.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<String, Object> getSystemTelemetrySummary() {
        long total = totalDirectivesProcessed.get();
        double avgDuration = spans.stream()
                .mapToLong(ObservabilitySpan::durationMs)
                .average()
                .orElse(0.0);

        return Map.of(
                "totalDirectivesProcessed", total,
                "recordedSpansCount", spans.size(),
                "averageDurationMs", Math.round(avgDuration * 100.0) / 100.0,
                "systemStatus", "HEALTHY",
                "uptime", System.currentTimeMillis()
        );
    }
}
