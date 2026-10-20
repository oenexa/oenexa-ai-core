package com.oenexa.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaRepositoryMemoryGraphService Zero-Mock Unit Tests")
class OenexaRepositoryMemoryGraphServiceTest {

    private final OenexaRepositoryMemoryGraphService service = new OenexaRepositoryMemoryGraphService();

    @Test
    @DisplayName("Should retrieve default workspace modular graph")
    void shouldRetrieveDefaultGraph() {
        var modules = service.getModules("default-ws");
        assertNotNull(modules);
        assertFalse(modules.isEmpty());
        assertTrue(modules.stream().anyMatch(m -> "mod-domain".equals(m.moduleId())));
        assertTrue(modules.stream().anyMatch(m -> "mod-app".equals(m.moduleId())));
    }

    @Test
    @DisplayName("Should register, load into RAM, and evict modules to SSD")
    void shouldManageModuleState() {
        service.registerModule("custom-ws", "mod-billing", "Billing Module", 85, 102400L, List.of("mod-domain"));

        var modules = service.getModules("custom-ws");
        var billing = modules.stream().filter(m -> "mod-billing".equals(m.moduleId())).findFirst().orElseThrow();
        assertFalse(billing.isLoadedInRam());

        assertTrue(service.loadModuleIntoRam("custom-ws", "mod-billing"));
        var reloaded = service.getModules("custom-ws").stream().filter(m -> "mod-billing".equals(m.moduleId())).findFirst().orElseThrow();
        assertTrue(reloaded.isLoadedInRam());

        assertTrue(service.evictModuleToSsd("custom-ws", "mod-billing"));
        var evicted = service.getModules("custom-ws").stream().filter(m -> "mod-billing".equals(m.moduleId())).findFirst().orElseThrow();
        assertFalse(evicted.isLoadedInRam());
    }
}
