package com.oenexa.application.vnext;

import com.oenexa.domain.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Phase 1: IssueClassifier
 * Autonomously categorizes incoming runtime failures, anomalies, logs, code smells,
 * and architecture reports into the 8 canonical VNext detection types.
 */
@Component
public class IssueClassifier {

    public ClassifiedIssue classify(String input, Map<String, String> context) {
        Objects.requireNonNull(input, "input must not be null");
        String safeInput = input.trim();
        String lower = safeInput.toLowerCase();
        String issueId = "ISSUE-" + UUID.randomUUID().toString().substring(0, 8);

        VNextDetectionType type;
        String severity;
        String title;

        if (lower.contains("layer leak") || lower.contains("framework leak") || lower.contains("domain depends on") || lower.contains("architecture violation") || lower.contains("circular")) {
            type = VNextDetectionType.ARCHITECTURE_VIOLATION;
            severity = "MEDIUM";
            title = "Clean Architecture / Hexagonal Boundary Violation";
        } else if (lower.contains("nullpointer") || lower.contains("arrayindex") || lower.contains("classcast")) {
            type = VNextDetectionType.BUG;
            severity = "HIGH";
            title = "Algorithmic Bug Dereference Detected";
        } else if (lower.contains("outofmemory") || lower.contains("memory leak") || lower.contains("heap space") || lower.contains("buffer leak")) {
            type = VNextDetectionType.MEMORY_LEAK;
            severity = "CRITICAL";
            title = "Resource Exhaustion / Memory Leak Detected";
        } else if (lower.contains("sql injection") || lower.contains("cwe-") || lower.contains("vulnerability") || lower.contains("xss")) {
            type = VNextDetectionType.SECURITY_RISK;
            severity = "CRITICAL";
            title = "Security Vulnerability Detected";
        } else if (lower.contains("timeout") || lower.contains("connection refused") || lower.contains("500") || lower.contains("crash")) {
            type = VNextDetectionType.RUNTIME_FAILURE;
            severity = "HIGH";
            title = "System Runtime Execution Failure";
        } else if (lower.contains("layer leak") || lower.contains("domain depends on") || lower.contains("framework leak") || lower.contains("circular")) {
            type = VNextDetectionType.ARCHITECTURE_VIOLATION;
            severity = "MEDIUM";
            title = "Clean Architecture / Hexagonal Boundary Violation";
        } else if (lower.contains("latency") || lower.contains("slow query") || lower.contains("thread starvation") || lower.contains("bottleneck")) {
            type = VNextDetectionType.PERFORMANCE_BOTTLENECK;
            severity = "MEDIUM";
            title = "Performance Degradation Bottleneck";
        } else if (lower.contains("conflict") || lower.contains("incompatible version") || lower.contains("nosuchmethoderror")) {
            type = VNextDetectionType.DEPENDENCY_CONFLICT;
            severity = "HIGH";
            title = "Transitive Dependency Version Collision";
        } else {
            type = VNextDetectionType.UNKNOWN_SCENARIO;
            severity = "LOW";
            title = "Unknown Exploratory Scenario";
        }

        return new ClassifiedIssue(
                issueId,
                type,
                title,
                safeInput,
                severity,
                Instant.now(),
                context != null ? context : Map.of()
        );
    }
}
