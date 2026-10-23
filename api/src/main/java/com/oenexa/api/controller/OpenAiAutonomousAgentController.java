package com.oenexa.api.controller;

import com.oenexa.application.openai.AgentOperatingSystem;
import com.oenexa.domain.OpenAiAgentOsDomain.*;
import com.oenexa.domain.OpenAiDomain.*;
import com.oenexa.domain.OpenAiReasoningDomain.*;
import com.oenexa.domain.OpenAiToolProtocolDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API for OENEXA 10-Persona Autonomous Agent Operating System.
 * Coordinates multi-agent goal decomposition, o3 deliberative reasoning traces,
 * and zero-mock verification.
 */
@RestController
@RequestMapping("/api/v1/openai/agent")
public class OpenAiAutonomousAgentController {

    private final AgentOperatingSystem agentOperatingSystem;

    public OpenAiAutonomousAgentController(AgentOperatingSystem agentOperatingSystem) {
        this.agentOperatingSystem = Objects.requireNonNull(agentOperatingSystem, "agentOperatingSystem must not be null");
    }

    @PostMapping("/execute")
    public ResponseEntity<AgentOsGoalResponse> executeGoal(@RequestBody AgentOsGoalRequest request) {
        if (request == null || request.goal() == null || request.goal().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(agentOperatingSystem.executeGoal(request));
    }

    @GetMapping("/models")
    public ResponseEntity<List<Map<String, String>>> listSupportedModels() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "oenexa-neo-gpt-o1", "specialty", "o3 Deliberative Reasoning & Planning", "mode", "Local-First"),
                Map.of("id", "qwen2.5-coder:32b", "specialty", "Autonomous Code Generation & Refactoring", "mode", "Air-Gapped Ollama"),
                Map.of("id", "deepseek-r1:14b", "specialty", "Deep Architectural Invariants & Root Cause Analysis", "mode", "Air-Gapped Ollama"),
                Map.of("id", "llama3.3:70b", "specialty", "Long-Context Synthesis & Repository Intelligence", "mode", "Air-Gapped Ollama"),
                Map.of("id", "gpt-4o", "specialty", "Cloud Frontier Logic & Fast Responses", "mode", "Cloud Hybrid")
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getEngineHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "engine", "OENEXA Neo-GPT Autonomous Agent OS",
                "version", "3.0.0-AUTONOMOUS-CORE",
                "localInferenceEndpoint", "http://localhost:11434",
                "zeroMockEnforced", true,
                "airGappedActive", true,
                "supportedPersonas", AgentPersona.values().length
        ));
    }
}
