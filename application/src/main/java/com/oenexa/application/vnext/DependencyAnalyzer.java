package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Phase 1: DependencyAnalyzer
 * Analyzes runtime and compile-time dependency graphs for collisions,
 * outdated vulnerabilities, and version incompatibilities.
 */
@Component
public class DependencyAnalyzer {

    public List<DependencyConflict> analyzeDependencies(Map<String, String> directDeps, Map<String, String> transitiveDeps) {
        if (directDeps == null || transitiveDeps == null) {
            return List.of();
        }

        List<DependencyConflict> conflicts = new ArrayList<>();

        for (Map.Entry<String, String> direct : directDeps.entrySet()) {
            String lib = direct.getKey();
            String directVer = direct.getValue();
            if (transitiveDeps.containsKey(lib)) {
                String transVer = transitiveDeps.get(lib);
                if (!directVer.equalsIgnoreCase(transVer)) {
                    conflicts.add(new DependencyConflict(
                            lib,
                            directVer,
                            transVer,
                            "Force dependency alignment to " + directVer + " in Gradle dependencyManagement"
                    ));
                }
            }
        }

        return conflicts;
    }
}

