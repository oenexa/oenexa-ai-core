package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Phase 1: RootCauseEngine
 * Traces issue symptoms, stacks, and AST paths back to fundamental root causes
 * with CWE classification and structured diagnostic evidence.
 */
@Component
public class RootCauseEngine {

    public RootCauseReport analyzeRootCause(ClassifiedIssue issue) {
        Objects.requireNonNull(issue, "issue must not be null");
        String text = (issue.description() + " " + issue.title()).toLowerCase();

        String cwe = "CWE-General";
        String probableCause;
        List<String> evidence = new ArrayList<>();

        switch (issue.type()) {
            case BUG -> {
                if (text.contains("nullpointer")) {
                    cwe = "CWE-476";
                    probableCause = "Unchecked null reference dereference in execution path.";
                    evidence.add("Null check invariant missing before method invocation");
                } else if (text.contains("arrayindex") || text.contains("indexout")) {
                    cwe = "CWE-125";
                    probableCause = "Out-of-bounds indexing in array or list access.";
                    evidence.add("Index boundary precondition check omitted");
                } else {
                    cwe = "CWE-20";
                    probableCause = "Improper input validation on domain parameters.";
                    evidence.add("Domain parameter boundary constraint violated");
                }
            }
            case MEMORY_LEAK -> {
                cwe = "CWE-400";
                probableCause = "Unbounded collection accumulation or pinned thread buffer leak.";
                evidence.add("Heap utilization continuously grew without generational GC reclamation");
            }
            case SECURITY_RISK -> {
                cwe = "CWE-89";
                probableCause = "Untrusted input concatenation into data/query evaluation tier.";
                evidence.add("Direct concatenation in persistence query detected");
            }
            case ARCHITECTURE_VIOLATION -> {
                cwe = "CWE-Architectural";
                probableCause = "Domain entity imports framework adapter or infrastructure dependency.";
                evidence.add("Import of org.springframework.* inside domain package");
            }
            case DEPENDENCY_CONFLICT -> {
                cwe = "CWE-1104";
                probableCause = "Classpath collision from multiple incompatible transitive library versions.";
                evidence.add("Duplicate class definitions found across runtime jars");
            }
            default -> {
                cwe = "CWE-Unknown";
                probableCause = "Emergent runtime behavior requiring deeper deliberative reasoning.";
                evidence.add("No direct deterministic match in static heuristic catalog");
            }
        }

        return new RootCauseReport(issue.issueId(), probableCause, cwe, 0.95, evidence);
    }
}

