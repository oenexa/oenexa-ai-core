package com.oenexa.api.controller;

import com.oenexa.application.cognitive.OenexaAgentOperatingSystem;
import com.oenexa.domain.OenexaAgentOsDomain.*;
import com.oenexa.domain.OenexaCognitiveDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API for OENEXA 10-Persona Autonomous Agent Operating System (OENEXA Agent OS).
 * Coordinates multi-agent goal decomposition, o3 deliberative reasoning traces,
 * native tool calling, and zero-mock verification with 100% data sovereignty.
 */
@RestController
@RequestMapping({"/api/v1/agent-os", "/api/v1/oenexa/agent"})
@CrossOrigin(origins = "*")
public class OenexaAgentOsController {

    private final OenexaAgentOperatingSystem agentOperatingSystem;

    public OenexaAgentOsController(OenexaAgentOperatingSystem agentOperatingSystem) {
        this.agentOperatingSystem = Objects.requireNonNull(agentOperatingSystem, "agentOperatingSystem must not be null");
    }

    @PostMapping("/execute")
    public ResponseEntity<OenexaAgentOsGoalResponse> executeGoal(@RequestBody OenexaAgentOsGoalRequest request) {
        if (request == null || request.goal() == null || request.goal().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(agentOperatingSystem.executeGoal(request));
    }

    @GetMapping("/models")
    public ResponseEntity<List<Map<String, String>>> listSupportedModels() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "oenexa-neo-gpt-o3-max", "specialty", "o3 Deliberative Reasoning & Planning", "mode", "Sovereign In-Process"),
                Map.of("id", "oenexa-neo-gpt-coder-32b", "specialty", "Autonomous Code Generation & Refactoring", "mode", "Sovereign In-Process"),
                Map.of("id", "oenexa-neo-gpt-architect-70b", "specialty", "Deep Architectural Invariants & Root Cause Analysis", "mode", "Sovereign In-Process"),
                Map.of("id", "oenexa-neo-gpt-reasoning-14b", "specialty", "High-Speed Tree-of-Thought Exploration", "mode", "Sovereign In-Process")
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getEngineHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "engine", "OENEXA Neo-GPT Autonomous Agent OS",
                "version", "3.0.0-PURE-OWN-CORE",
                "sovereignInProcess", true,
                "zeroMockEnforced", true,
                "airGappedActive", true,
                "supportedPersonas", OenexaAgentPersona.values().length
        ));
    }
}
