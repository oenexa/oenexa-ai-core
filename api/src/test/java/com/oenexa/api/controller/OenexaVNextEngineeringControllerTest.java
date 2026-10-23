package com.oenexa.api.controller;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.application.vnext.OenexaAutonomousEngineeringSystem;
import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaVNextEngineeringController Zero-Mock Unit Tests")
class OenexaVNextEngineeringControllerTest {

    private OenexaVNextEngineeringController controller;
    private OenexaAutonomousEngineeringSystem system;

    @BeforeEach
    void setUp() {
        system = new OenexaAutonomousEngineeringSystem();
        controller = new OenexaVNextEngineeringController(system);
    }

    @Test
    @DisplayName("Should test constructors and parameter null verification")
    void testConstructors() {
        OenexaVNextEngineeringController defaultCtrl = new OenexaVNextEngineeringController();
        assertNotNull(defaultCtrl);
        assertThrows(NullPointerException.class, () -> new OenexaVNextEngineeringController(null));
    }

    @Test
    @DisplayName("Should execute VNext loop via POST /api/v4/vnext/execute")
    void testExecuteEndpoint() {
        var respNull = controller.executeVNextLoop(null);
        assertEquals(HttpStatus.OK, respNull.getStatusCode());
        assertNotNull(respNull.getBody());
        assertTrue(respNull.getBody().deliveredSuccessfully());

        var req = new OenexaVNextEngineeringController.ExecutionRequest("Fix OutOfMemoryError in caching layer", Map.of("tier", "L2"));
        var respCustom = controller.executeVNextLoop(req);
        assertEquals(HttpStatus.OK, respCustom.getStatusCode());
        assertNotNull(respCustom.getBody());
        assertEquals(VNextDetectionType.MEMORY_LEAK, respCustom.getBody().issue().type());
    }

    @Test
    @DisplayName("Should return 7 memory tiers and execute consolidation")
    void testMemoryEndpoints() {
        ResponseEntity<List<VNextMemoryTier>> tiersResp = controller.getMemoryTiers();
        assertEquals(HttpStatus.OK, tiersResp.getStatusCode());
        assertNotNull(tiersResp.getBody());
        assertEquals(7, tiersResp.getBody().size());

        var req = new OenexaVNextEngineeringController.ConsolidateMemoryRequest(List.of("alpha", "beta", "alpha"));
        ResponseEntity<MemoryConsolidationReport> consResp = controller.consolidateMemory(req);
        assertEquals(HttpStatus.OK, consResp.getStatusCode());
        assertNotNull(consResp.getBody());
        assertEquals(2, consResp.getBody().entriesConsolidated());
        assertEquals(1, consResp.getBody().duplicatesEliminated());
    }

    @Test
    @DisplayName("Should evaluate architecture layer evolution")
    void testArchitectureEndpoint() {
        ResponseEntity<ArchitectureEvolutionPlan> resp = controller.evaluateArchitecture("DOMAIN");
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("DOMAIN", resp.getBody().targetLayer());
        assertTrue(resp.getBody().backwardCompatible());
    }

    @Test
    @DisplayName("Should trigger autonomous self-upgrade")
    void testUpgradeEndpoint() {
        ResponseEntity<CapabilityGenerationArtifact> resp = controller.triggerSelfUpgrade("Add Blockchain Analyzer");
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("BlockchainAnalyzer", resp.getBody().capabilityName());

        ResponseEntity<CapabilityGenerationArtifact> respNoMatch = controller.triggerSelfUpgrade("Standard simple question");
        assertEquals(HttpStatus.NO_CONTENT, respNoMatch.getStatusCode());
    }
}


