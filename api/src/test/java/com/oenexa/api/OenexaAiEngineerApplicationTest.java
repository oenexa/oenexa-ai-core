package com.oenexa.api;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAiEngineerApplication Zero-Mock Unit Tests")
class OenexaAiEngineerApplicationTest {

    @Test
    @DisplayName("Should instantiate application class cleanly")
    void shouldInstantiateApplication() {
        OenexaAiEngineerApplication app = new OenexaAiEngineerApplication();
        assertNotNull(app);
    }
}
