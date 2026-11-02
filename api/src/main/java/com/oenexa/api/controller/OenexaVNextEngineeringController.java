package com.oenexa.api.controller;

import com.oenexa.application.vnext.OenexaAutonomousEngineeringSystem;
import com.oenexa.application.vnext.OenexaAutonomousEngineeringSystem.MasterVNextExecutionResult;
import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Enterprise REST API Controller exposing the OENEXA-AI VNext Autonomous AI Engineering Platform.
 * Supports continuous improvement loops, pattern retrieval, memory consolidation,
 * architecture evolution evaluation, and autonomous capability self-upgrades.
 */
@RestController
@RequestMapping("/api/v4/vnext")
@CrossOrigin(originPatterns = "*")
public class OenexaVNextEngineeringController {

    public record ExecutionRequest(String input, Map<String, String> context) {}
    public record ConsolidateMemoryRequest(List<String> rawEntries) {}

    private final OenexaAutonomousEngineeringSystem engineeringSystem;

    public OenexaVNextEngineeringController() {
        this(new OenexaAutonomousEngineeringSystem());
    }

    @Autowired
    public OenexaVNextEngineeringController(OenexaAutonomousEngineeringSystem engineeringSystem) {
        this.engineeringSystem = Objects.requireNonNull(engineeringSystem, "engineeringSystem must not be null");
    }

    @PostMapping("/execute")
    public ResponseEntity<MasterVNextExecutionResult> executeVNextLoop(@RequestBody(required = false) ExecutionRequest request) {
        String input = (request != null && request.input() != null && !request.input().isBlank())
                ? request.input()
                : "Continuous self-optimization and architecture invariant scan";
        Map<String, String> context = (request != null && request.context() != null)
                ? request.context()
                : Map.of();

        MasterVNextExecutionResult result = engineeringSystem.executeContinuousImprovementLoop(input, context);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/memory/tiers")
    public ResponseEntity<List<VNextMemoryTier>> getMemoryTiers() {
        return ResponseEntity.ok(engineeringSystem.getMemoryManager().getActiveTiers());
    }

    @PostMapping("/memory/consolidate")
    public ResponseEntity<MemoryConsolidationReport> consolidateMemory(@RequestBody(required = false) ConsolidateMemoryRequest request) {
        List<String> entries = (request != null && request.rawEntries() != null) ? request.rawEntries() : List.of();
        return ResponseEntity.ok(engineeringSystem.getMemoryManager().consolidateMemory(entries));
    }

    @GetMapping("/architecture/evaluate")
    public ResponseEntity<ArchitectureEvolutionPlan> evaluateArchitecture(@RequestParam(defaultValue = "DOMAIN") String layer) {
        return ResponseEntity.ok(engineeringSystem.getArchitectureEvolutionManager().evaluateLayerEvolution(layer));
    }

    @PostMapping("/upgrade")
    public ResponseEntity<CapabilityGenerationArtifact> triggerSelfUpgrade(@RequestParam String directive) {
        Optional<CapabilityGenerationArtifact> artifact = engineeringSystem.getSelfUpgradeEngine().evaluateAndUpgrade(directive);
        return artifact.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}

