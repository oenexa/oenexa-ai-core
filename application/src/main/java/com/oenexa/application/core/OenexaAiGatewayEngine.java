package com.oenexa.application.core;

import com.oenexa.domain.OenexaCoreBrainDomain.CoreBrainDirective;
import com.oenexa.domain.OenexaCoreBrainDomain.GatewayRoutingDecision;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Core Module 1: AI Gateway Engine.
 * Serves as the central entry point for all AI requests across the OENEXA ecosystem.
 * Coordinates request routing, tenant isolation, rate limit enforcement, and model selection.
 */
@Service
public class OenexaAiGatewayEngine {

    private final OenexaSecurityEngine securityEngine;

    public OenexaAiGatewayEngine(OenexaSecurityEngine securityEngine) {
        this.securityEngine = Objects.requireNonNull(securityEngine, "securityEngine must not be null");
    }

    public GatewayRoutingDecision evaluateAndRoute(CoreBrainDirective directive) {
        Objects.requireNonNull(directive, "directive must not be null");

        // 1. Pre-flight security assessment
        var securityAssessment = securityEngine.evaluateThreatAndIntegrity(
                directive.objective(),
                directive.userId(),
                directive.tenantId()
        );

        if (!securityAssessment.allowed()) {
            return new GatewayRoutingDecision(
                    "BLOCKED",
                    "SECURITY_FIREWALL",
                    directive.localFirstRequired(),
                    0.0,
                    false
            );
        }

        // 2. Local-first sovereign determination vs Cloud
        String targetModel = directive.preferredModel();
        boolean isLocal = directive.localFirstRequired()
                || targetModel.contains("neo-gpt")
                || targetModel.contains("ollama");

        String provider = isLocal ? "OENEXA_SOVEREIGN_ENGINE" : "CLOUD_FEDERATED_ROUTER";
        double estimatedLatency = isLocal ? 8.5 : 45.0;

        return new GatewayRoutingDecision(
                targetModel,
                provider,
                isLocal,
                estimatedLatency,
                true
        );
    }
}
