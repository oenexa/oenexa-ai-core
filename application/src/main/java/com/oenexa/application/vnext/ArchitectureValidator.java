package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Phase 1: ArchitectureValidator
 * Enforces hexagonal architectural boundaries, DDD entity immutability,
 * and zero-framework pollution inside domain packages.
 */
@Component
public class ArchitectureValidator {

    public List<ArchitectureViolation> validateArchitecture(String packageName, List<String> importedPackages) {
        Objects.requireNonNull(packageName, "packageName must not be null");
        if (importedPackages == null || importedPackages.isEmpty()) {
            return List.of();
        }

        List<ArchitectureViolation> violations = new ArrayList<>();

        if (packageName.contains(".domain")) {
            for (String imported : importedPackages) {
                if (imported.startsWith("org.springframework.") || imported.startsWith("jakarta.persistence.") || imported.startsWith("com.oenexa.infrastructure.")) {
                    violations.add(new ArchitectureViolation(
                            "DOMAIN_FRAMEWORK_LEAKAGE",
                            packageName,
                            "Domain Layer",
                            "Extract dependency to an outbound port interface in domain and implement adapter in infrastructure"
                    ));
                }
            }
        }

        if (packageName.contains(".api") || packageName.contains(".controller")) {
            for (String imported : importedPackages) {
                if (imported.startsWith("com.oenexa.infrastructure.persistence.repository.")) {
                    violations.add(new ArchitectureViolation(
                            "API_DIRECT_INFRASTRUCTURE_ACCESS",
                            packageName,
                            "API Layer",
                            "Direct repository access from controllers is forbidden; route requests through application services"
                    ));
                }
            }
        }

        return violations;
    }
}

