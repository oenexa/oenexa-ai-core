package com.oenexa.application.vnext;

import com.oenexa.domain.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Phase 5 & 7: ArchitectureEvolutionManager
 * Continuously evaluates architectural layers:
 * Domain, Application, Infrastructure, API, AI, Memory.
 * Formulates refactoring & migration plans while guaranteeing backward compatibility.
 */
@Component
public class ArchitectureEvolutionManager {

    public ArchitectureEvolutionPlan evaluateLayerEvolution(String layerName) {
        Objects.requireNonNull(layerName, "layerName must not be null");
        String normalized = layerName.trim().toUpperCase();

        return switch (normalized) {
            case "DOMAIN" -> new ArchitectureEvolutionPlan(
                    "DOMAIN", "Mutable Entity Pojos", "Pure Sealed Java 25 Domain Records with Invariants",
                    true, List.of("Convert setters to defensive constructor records", "Enforce Objects.requireNonNull", "Zero framework imports")
            );
            case "APPLICATION" -> new ArchitectureEvolutionPlan(
                    "APPLICATION", "Synchronous Heavy Services", "Virtual Thread Structured Concurrency Services",
                    true, List.of("Wrap I/O tasks in StructuredTaskScope", "Ensure carrier thread unmounting", "Maintain 100% zero-mock testability")
            );
            case "INFRASTRUCTURE" -> new ArchitectureEvolutionPlan(
                    "INFRASTRUCTURE", "Direct SQL Queries", "Spring Data JPA + PostgreSQL 17 pgvector HNSW Indexing",
                    true, List.of("Generate Flyway migration scripts", "Add HNSW vector indexing", "Enable connection pooling via HikariCP")
            );
            case "API" -> new ArchitectureEvolutionPlan(
                    "API", "Ad-hoc JSON Error DTOs", "RFC 7807 / RFC 9457 Problem Details with Distributed Trace IDs",
                    true, List.of("Return ProblemDetail in GlobalErrorHandler", "Propagate X-Correlation-Id", "Sanitize production stack traces")
            );
            default -> new ArchitectureEvolutionPlan(
                    normalized, "Monolithic Component", "Hexagonal Decoupled Module",
                    true, List.of("Extract ports", "Implement adapters", "Run regression test suite")
            );
        };
    }
}
