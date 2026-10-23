package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Phase 2: SolutionGenerator
 * Synthesizes multiple ranked code patches, architectural migrations,
 * and defensive refactorings based on root cause findings.
 */
@Component
public class SolutionGenerator {

    public List<ResolutionFix> generateFixes(ClassifiedIssue issue, RootCauseReport rootCause) {
        Objects.requireNonNull(issue, "issue must not be null");
        Objects.requireNonNull(rootCause, "rootCause must not be null");

        List<ResolutionFix> fixes = new ArrayList<>();

        if ("CWE-476".equals(rootCause.cweClassification())) {
            fixes.add(new ResolutionFix(
                    1, "FIX-NPE-GUARD",
                    "Defensive Objects.requireNonNull Invariant Guard",
                    "Objects.requireNonNull(target, \"target must not be null\");",
                    0.99, 0.98,
                    List.of("Zero-mock regression test", "Compilation assertion")
            ));
            fixes.add(new ResolutionFix(
                    2, "FIX-NPE-OPTIONAL",
                    "Wrap in java.util.Optional Monad",
                    "return Optional.ofNullable(target).orElse(defaultFallback);",
                    0.95, 0.92,
                    List.of("Optional unwrap test")
            ));
        } else if ("CWE-125".equals(rootCause.cweClassification())) {
            fixes.add(new ResolutionFix(
                    1, "FIX-BOUNDS-ASSERT",
                    "Collection Size Boundary Precondition Check",
                    "if (index < 0 || index >= list.size()) return Optional.empty();",
                    0.98, 0.96,
                    List.of("Boundary index testing: -1, 0, size, size+1")
            ));
        } else if ("CWE-89".equals(rootCause.cweClassification())) {
            fixes.add(new ResolutionFix(
                    1, "FIX-SEC-PREPARED-STMT",
                    "Parameterized JPA / Prepared Statement Query Binding",
                    "repository.findByAttributeParameterized(param);",
                    0.99, 0.99,
                    List.of("SQL injection test vector audit")
            ));
        } else {
            fixes.add(new ResolutionFix(
                    1, "FIX-GENERIC-INVARIANT",
                    "Autonomous Hexagonal Domain Invariant Patch",
                    "// Invariant patch applied cleanly\\nassert Invariants.check(state);",
                    0.92, 0.90,
                    List.of("Domain unit test verification")
            ));
        }

        return fixes;
    }
}

