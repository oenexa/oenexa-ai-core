package com.oenexa.api.controller;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthControllerTest {

    @Test
    void shouldReturnHealthStatusSuccessfully() {
        HealthController controller = new HealthController();
        ResponseEntity<Map<String, Object>> response = controller.health();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("oenexa-ai-core", response.getBody().get("service"));
        assertEquals("OENEXA AI Core", response.getBody().get("application"));
        assertEquals("OENEXA AI", response.getBody().get("product"));
        assertNotNull(response.getBody().get("javaVersion"));
    }
}

