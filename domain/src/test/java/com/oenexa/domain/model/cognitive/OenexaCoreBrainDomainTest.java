package com.oenexa.domain.model.cognitive;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaCoreBrainDomain Zero-Mock Unit Tests")
class OenexaCoreBrainDomainTest {

    @Test
    @DisplayName("Should verify all 12 Core Module types exist with non-null missions")
    void testCoreModuleTypes() {
        assertEquals(12, OenexaCoreBrainDomain.CoreModuleType.values().length);
        for (var type : OenexaCoreBrainDomain.CoreModuleType.values()) {
            assertNotNull(type.getDisplayName());
            assertNotNull(type.getMission());
            assertFalse(type.getDisplayName().isBlank());
            assertFalse(type.getMission().isBlank());
        }
    }

    @Test
    @DisplayName("Should validate CoreModuleInfo record creation and bounds")
    void testCoreModuleInfo() {
        var info = OenexaCoreBrainDomain.CoreModuleInfo.healthy(OenexaCoreBrainDomain.CoreModuleType.AI_GATEWAY, 42);
        assertEquals(OenexaCoreBrainDomain.CoreModuleType.AI_GATEWAY, info.type());
        assertEquals(OenexaCoreBrainDomain.CoreModuleStatus.HEALTHY, info.status());
        assertEquals(1.0, info.healthScore());
        assertEquals(42, info.processedRequests());

        var custom = new OenexaCoreBrainDomain.CoreModuleInfo(
                OenexaCoreBrainDomain.CoreModuleType.OBSERVABILITY_ENGINE,
                "Observability Engine",
                "Distributed tracing & telemetry",
                OenexaCoreBrainDomain.CoreModuleStatus.OPTIMIZING,
                1.5, // should clamp to 1.0
                100
        );
        assertEquals(1.0, custom.healthScore());
        assertEquals(OenexaCoreBrainDomain.CoreModuleStatus.OPTIMIZING, custom.status());
    }

    @Test
    @DisplayName("Should validate CoreBrainDirective creation and reject blank objectives")
    void testCoreBrainDirective() {
        var dir = OenexaCoreBrainDomain.CoreBrainDirective.of("Architect distributed state machine");
        assertNotNull(dir.directiveId());
        assertEquals("default", dir.tenantId());
        assertEquals("engineer", dir.userId());
        assertEquals("Architect distributed state machine", dir.objective());
        assertTrue(dir.localFirstRequired());

        assertThrows(NullPointerException.class, () -> new OenexaCoreBrainDomain.CoreBrainDirective(
                "d-1", "t", "u", null, "m", true, "high", Map.of()
        ));
        assertThrows(IllegalArgumentException.class, () -> new OenexaCoreBrainDomain.CoreBrainDirective(
                "d-1", "t", "u", "   ", "m", true, "high", Map.of()
        ));
    }

    @Test
    @DisplayName("Should validate SecurityAssessment and factory methods")
    void testSecurityAssessment() {
        var clean = OenexaCoreBrainDomain.SecurityAssessment.clean();
        assertTrue(clean.allowed());
        assertEquals(0.0, clean.threatScore());
        assertEquals("CLEAN", clean.threatClassification());

        var denied = OenexaCoreBrainDomain.SecurityAssessment.denied("PROMPT_INJECTION", 0.95);
        assertFalse(denied.allowed());
        assertEquals(0.95, denied.threatScore());
        assertEquals("PROMPT_INJECTION", denied.threatClassification());
    }

    @Test
    @DisplayName("Should construct PerformanceSnapshot and ObservabilitySpan safely")
    void testPerformanceAndObservabilityRecords() {
        var perf = new OenexaCoreBrainDomain.PerformanceSnapshot(true, 0.5, 0, 1024, 15);
        assertTrue(perf.cacheHit());
        assertEquals(1.0, perf.compressionRatio()); // min 1.0
        assertEquals(1, perf.activeVirtualThreads()); // min 1

        var span = new OenexaCoreBrainDomain.ObservabilitySpan(
                null, null, null, null, 25, null, Map.of("tag1", "val1")
        );
        assertNotNull(span.traceId());
        assertNotNull(span.spanId());
        assertEquals("OENEXA_BRAIN", span.engineName());
        assertEquals("EXECUTE_DIRECTIVE", span.operation());
        assertEquals("OK", span.status());
        assertEquals("val1", span.tags().get("tag1"));
    }

    @Test
    @DisplayName("Should construct CoreBrainSynthesis record with complete invariants")
    void testCoreBrainSynthesis() {
        var dir = OenexaCoreBrainDomain.CoreBrainDirective.of("Synthesize enterprise schema");
        var route = new OenexaCoreBrainDomain.GatewayRoutingDecision("oenexa-neo-gpt-o3-max", "SOVEREIGN", true, 12.5, true);
        var sec = OenexaCoreBrainDomain.SecurityAssessment.clean();
        var perf = new OenexaCoreBrainDomain.PerformanceSnapshot(false, 2.4, 8, 4096, 45);
        var span = new OenexaCoreBrainDomain.ObservabilitySpan("tr-1", "sp-1", "BRAIN", "SYNTHESIS", 45, "OK", Map.of());
        var artifact = new ChatDomain.CodeArtifact("Schema", "schema.sql", "CREATE TABLE users;");

        var synthesis = new OenexaCoreBrainDomain.CoreBrainSynthesis(
                "dir-100",
                "COMPLETED",
                dir,
                route,
                sec,
                List.of("1. Parsed requirements", "2. Generated DDL"),
                List.of(artifact),
                perf,
                span,
                0.99
        );

        assertEquals("dir-100", synthesis.directiveId());
        assertEquals("COMPLETED", synthesis.status());
        assertEquals(2, synthesis.reasoningSteps().size());
        assertEquals(1, synthesis.synthesizedArtifacts().size());
        assertEquals(0.99, synthesis.confidenceScore());
    }
}

