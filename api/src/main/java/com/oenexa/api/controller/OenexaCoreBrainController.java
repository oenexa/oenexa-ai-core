package com.oenexa.api.controller;

import com.oenexa.application.core.OenexaAiGatewayEngine;
import com.oenexa.application.core.OenexaCoreBrain;
import com.oenexa.application.core.OenexaObservabilityEngine;
import com.oenexa.application.core.OenexaSecurityEngine;
import com.oenexa.domain.OenexaCoreBrainDomain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST API Controller for OENEXA-AI-CORE Central Brain.
 * Serves as the central API gateway entry point for the 12 Core Engines,
 * real-time observability telemetry, module diagnostics, and security audits.
 */
@RestController
@RequestMapping("/api/v1/core")
@CrossOrigin(origins = "*")
public class OenexaCoreBrainController {

    private final OenexaCoreBrain coreBrain;
    private final OenexaAiGatewayEngine gatewayEngine;
    private final OenexaSecurityEngine securityEngine;
    private final OenexaObservabilityEngine observabilityEngine;

    public OenexaCoreBrainController(
            OenexaCoreBrain coreBrain,
            OenexaAiGatewayEngine gatewayEngine,
            OenexaSecurityEngine securityEngine,
            OenexaObservabilityEngine observabilityEngine
    ) {
        this.coreBrain = Objects.requireNonNull(coreBrain, "coreBrain must not be null");
        this.gatewayEngine = Objects.requireNonNull(gatewayEngine, "gatewayEngine must not be null");
        this.securityEngine = Objects.requireNonNull(securityEngine, "securityEngine must not be null");
        this.observabilityEngine = Objects.requireNonNull(observabilityEngine, "observabilityEngine must not be null");
    }

    @PostMapping("/brain/execute")
    public ResponseEntity<CoreBrainSynthesis> executeDirective(@RequestBody(required = false) CoreBrainDirective directive) {
        if (directive == null || directive.objective() == null || directive.objective().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        CoreBrainSynthesis synthesis = coreBrain.executeDirective(directive);
        return ResponseEntity.ok(synthesis);
    }

    @GetMapping("/brain/modules")
    public ResponseEntity<List<CoreModuleInfo>> getModulesStatus() {
        return ResponseEntity.ok(coreBrain.getCoreModulesStatus());
    }

    @PostMapping("/gateway/route")
    public ResponseEntity<GatewayRoutingDecision> evaluateRoute(@RequestBody(required = false) CoreBrainDirective directive) {
        if (directive == null || directive.objective() == null || directive.objective().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(gatewayEngine.evaluateAndRoute(directive));
    }

    @GetMapping("/observability/telemetry")
    public ResponseEntity<Map<String, Object>> getTelemetrySummary() {
        return ResponseEntity.ok(observabilityEngine.getSystemTelemetrySummary());
    }

    @GetMapping("/security/audit")
    public ResponseEntity<List<String>> getSecurityAuditTrail(@RequestParam(defaultValue = "default") String tenantId) {
        return ResponseEntity.ok(securityEngine.getAuditTrail(tenantId));
    }
}
