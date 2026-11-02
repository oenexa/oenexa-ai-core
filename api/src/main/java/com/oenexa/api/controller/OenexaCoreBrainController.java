package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.core.CoreBrainDtos.*;
import com.oenexa.api.mapper.CoreBrainApiMapper;
import com.oenexa.application.core.OenexaAiGatewayEngine;
import com.oenexa.application.core.OenexaCoreBrain;
import com.oenexa.application.core.OenexaObservabilityEngine;
import com.oenexa.application.core.OenexaSecurityEngine;
import com.oenexa.application.security.CoreBrainSecurityPolicy;
import com.oenexa.application.validation.CoreBrainValidator;
import com.oenexa.domain.model.cognitive.OenexaCoreBrainDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST API Controller for OENEXA-AI-CORE Central Brain.
 * Supports standard 11-Layer Clean Hexagonal Architecture:
 * Controller -> DTO -> Validation -> Security -> Mapper -> Service -> ApiResponse<T>.
 */
@RestController
@RequestMapping("/api/v1/core")
@CrossOrigin(originPatterns = "*")
public class OenexaCoreBrainController {

    private final OenexaCoreBrain coreBrain;
    private final OenexaAiGatewayEngine gatewayEngine;
    private final OenexaSecurityEngine securityEngine;
    private final OenexaObservabilityEngine observabilityEngine;
    private final CoreBrainValidator validator;
    private final CoreBrainSecurityPolicy securityPolicy;
    private final CoreBrainApiMapper mapper;

    public OenexaCoreBrainController(
            OenexaCoreBrain coreBrain,
            OenexaAiGatewayEngine gatewayEngine,
            OenexaSecurityEngine securityEngine,
            OenexaObservabilityEngine observabilityEngine
    ) {
        this(coreBrain, gatewayEngine, securityEngine, observabilityEngine, new CoreBrainValidator(), new CoreBrainSecurityPolicy(), new CoreBrainApiMapper());
    }

    @Autowired
    public OenexaCoreBrainController(
            OenexaCoreBrain coreBrain,
            OenexaAiGatewayEngine gatewayEngine,
            OenexaSecurityEngine securityEngine,
            OenexaObservabilityEngine observabilityEngine,
            CoreBrainValidator validator,
            CoreBrainSecurityPolicy securityPolicy,
            CoreBrainApiMapper mapper
    ) {
        this.coreBrain = Objects.requireNonNull(coreBrain, "coreBrain must not be null");
        this.gatewayEngine = Objects.requireNonNull(gatewayEngine, "gatewayEngine must not be null");
        this.securityEngine = Objects.requireNonNull(securityEngine, "securityEngine must not be null");
        this.observabilityEngine = Objects.requireNonNull(observabilityEngine, "observabilityEngine must not be null");
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.securityPolicy = Objects.requireNonNull(securityPolicy, "securityPolicy must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
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

    // ==========================================
    // 11-Layer Clean Hexagonal Architecture v2 Endpoints
    // ==========================================

    @PostMapping("/v2/brain/execute")
    public ResponseEntity<ApiResponse<CoreBrainDirectiveResultDto>> executeDirectiveV2(
            @RequestBody CoreBrainDirectiveRequestDto req,
            @RequestHeader(value = "X-User-Role", defaultValue = "ROLE_USER") String role
    ) {
        validator.validateDirective(req.directiveId(), req.objective());
        securityPolicy.verifyDirectiveIntegrity(req.objective(), req.priority(), role);

        CoreBrainDirective directive = mapper.toDirective(req);
        CoreBrainSynthesis synthesis = coreBrain.executeDirective(directive);

        return ResponseEntity.ok(ApiResponse.ok("Core brain directive executed successfully", mapper.toDirectiveResultDto(synthesis)));
    }
}
