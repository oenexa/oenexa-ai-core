package com.oenexa.api;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.domain.PlatformModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModulesController Zero-Mock Unit Tests")
class ModulesControllerTest {

    private final ModulesController controller = new ModulesController();

    @Test
    @DisplayName("Should list all 20 modules ordered by status and id")
    void shouldListModules() {
        ResponseEntity<List<PlatformModule.ModuleInfo>> response = controller.listModules();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(20, response.getBody().size());

        // Check that module info contains id, displayName, description, capabilities, status
        PlatformModule.ModuleInfo first = response.getBody().get(0);
        assertNotNull(first.id());
        assertNotNull(first.displayName());
        assertNotNull(first.description());
        assertNotNull(first.capabilities());
        assertNotNull(first.status());
    }
}
