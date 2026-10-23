package com.oenexa.application.validation;

import com.oenexa.domain.ChatDomain.OenexaAutonomousGoalRequest;
import com.oenexa.domain.OenexaCoreBrainDomain.CoreBrainDirective;
import com.oenexa.domain.OenexaExceptions.OenexaValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AgentGoalValidator Zero-Mock Unit Tests")
class AgentGoalValidatorTest {

    private final AgentGoalValidator validator = new AgentGoalValidator();

    @Test
    @DisplayName("Should accept valid goal and brain directive")
    void shouldAcceptValidGoalAndDirective() {
        OenexaAutonomousGoalRequest goalReq = new OenexaAutonomousGoalRequest("Refactor domain module to zero mocks");
        assertDoesNotThrow(() -> validator.validateGoalRequest(goalReq));

        CoreBrainDirective directive = CoreBrainDirective.of("Automate CI/CD pipeline");
        assertDoesNotThrow(() -> validator.validateBrainDirective(directive));
    }

    @Test
    @DisplayName("Should reject blank or null goal requests")
    void shouldRejectInvalidGoalRequests() {
        assertThrows(OenexaValidationException.class, () -> validator.validateGoalRequest(null));
        assertThrows(OenexaValidationException.class, () -> validator.validateGoalRequest(new OenexaAutonomousGoalRequest("")));
        assertThrows(OenexaValidationException.class, () -> validator.validateGoalRequest(new OenexaAutonomousGoalRequest("ab")));
    }

    @Test
    @DisplayName("Should reject null brain directives")
    void shouldRejectInvalidDirectives() {
        assertThrows(OenexaValidationException.class, () -> validator.validateBrainDirective(null));
    }
}
