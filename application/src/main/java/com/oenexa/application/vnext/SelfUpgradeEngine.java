package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 5: SelfUpgradeEngine
 * Autonomously expands system capabilities when missing modules or requirements are identified:
 * Detect Missing Module -> Design Module -> Generate Code Structure -> Generate Tests -> Register Capability.
 */
@Service
public class SelfUpgradeEngine {

    private final CapabilityAnalyzer analyzer;
    private final CapabilityGenerator generator;
    private final ArchitectureEvolutionManager evolutionManager;
    private final Map<String, CapabilityGenerationArtifact> registeredCapabilities = new ConcurrentHashMap<>();

    public SelfUpgradeEngine() {
        this(new CapabilityAnalyzer(), new CapabilityGenerator(), new ArchitectureEvolutionManager());
    }

    @Autowired
    public SelfUpgradeEngine(
            CapabilityAnalyzer analyzer,
            CapabilityGenerator generator,
            ArchitectureEvolutionManager evolutionManager
    ) {
        this.analyzer = Objects.requireNonNull(analyzer, "analyzer must not be null");
        this.generator = Objects.requireNonNull(generator, "generator must not be null");
        this.evolutionManager = Objects.requireNonNull(evolutionManager, "evolutionManager must not be null");
    }

    public Optional<CapabilityGenerationArtifact> evaluateAndUpgrade(String userDirective) {
        Objects.requireNonNull(userDirective, "userDirective must not be null");

        Optional<MissingCapabilityDescriptor> missingOpt = analyzer.detectMissingCapability(userDirective);
        if (missingOpt.isEmpty()) {
            return Optional.empty();
        }

        MissingCapabilityDescriptor missing = missingOpt.get();
        CapabilityGenerationArtifact artifact = generator.generateCapability(missing);

        // Register new capability
        registeredCapabilities.put(artifact.capabilityName(), artifact);

        return Optional.of(artifact);
    }

    public boolean isCapabilityRegistered(String name) {
        return registeredCapabilities.containsKey(name);
    }

    public int getRegisteredCount() {
        return registeredCapabilities.size();
    }
}

