package com.oenexa.application.vnext;

import com.oenexa.domain.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Phase 5: CapabilityAnalyzer
 * Detects missing system capabilities from incoming user directives or execution failures.
 */
@Component
public class CapabilityAnalyzer {

    private final Set<String> knownCapabilities = Set.of(
            "REST_API_GENERATOR", "BUG_FIXER", "TEST_SYNTHESIZER", "PR_REVIEWER",
            "SECURITY_AUDITOR", "INFRASTRUCTURE_AUTOMATOR", "DELIBERATIVE_REASONER"
    );

    public Optional<MissingCapabilityDescriptor> detectMissingCapability(String userDirective) {
        Objects.requireNonNull(userDirective, "userDirective must not be null");
        String lower = userDirective.toLowerCase();

        if (lower.contains("blockchain") || lower.contains("smart contract")) {
            return Optional.of(new MissingCapabilityDescriptor(
                    "BlockchainAnalyzer",
                    "Autonomous verification and audit of smart contracts and distributed ledgers",
                    "com.oenexa.application.blockchain",
                    List.of("BlockchainAuditPort", "ContractVerifier")
            ));
        }

        if (lower.contains("quantum") || lower.contains("qiskit")) {
            return Optional.of(new MissingCapabilityDescriptor(
                    "QuantumCircuitOptimizer",
                    "Optimization of quantum state gates and QASM representation",
                    "com.oenexa.application.quantum",
                    List.of("QuantumCircuitPort", "StateOptimizer")
            ));
        }

        return Optional.empty();
    }
}
