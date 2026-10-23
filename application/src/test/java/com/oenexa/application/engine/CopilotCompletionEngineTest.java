package com.oenexa.application.engine;

import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.CopilotCompletionRequest;
import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.CopilotCompletionResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CopilotCompletionEngineTest {

    private final CopilotCompletionEngine engine = new CopilotCompletionEngine();

    @Test
    void shouldThrowExceptionWhenRequestIsNull() {
        assertThrows(NullPointerException.class, () -> engine.generateCopilotInlineCompletion(null));
    }

    @Test
    void shouldGenerateRecordOrClassCompletion() {
        CopilotCompletionRequest reqRecord = new CopilotCompletionRequest("public record UserDto", "User.java");
        CopilotCompletionResponse respRecord = engine.generateCopilotInlineCompletion(reqRecord);

        assertNotNull(respRecord);
        assertTrue(respRecord.ghostText().contains("SolutionDto"));
        assertEquals("RECORD", respRecord.suggestions().get(0).kind());

        CopilotCompletionRequest reqClass = new CopilotCompletionRequest("public class AuthService", "AuthService.java");
        CopilotCompletionResponse respClass = engine.generateCopilotInlineCompletion(reqClass);

        assertTrue(respClass.ghostText().contains("SolutionDto"));
    }

    @Test
    void shouldGenerateTestMethodCompletion() {
        CopilotCompletionRequest reqTest = new CopilotCompletionRequest("@Test\nvoid should", "AuthTest.java");
        CopilotCompletionResponse respTest = engine.generateCopilotInlineCompletion(reqTest);

        assertNotNull(respTest);
        assertTrue(respTest.ghostText().contains("shouldVerifyZeroMockState"));
        assertEquals("METHOD", respTest.suggestions().get(0).kind());

        CopilotCompletionRequest reqVoid = new CopilotCompletionRequest("void execute()", "ServiceTest.java");
        CopilotCompletionResponse respVoid = engine.generateCopilotInlineCompletion(reqVoid);

        assertTrue(respVoid.ghostText().contains("assertNotNull"));
    }

    @Test
    void shouldGenerateDefensiveGuardCompletionForOtherOrNullPrefix() {
        CopilotCompletionRequest reqNull = new CopilotCompletionRequest(null, "Utility.java");
        CopilotCompletionResponse respNull = engine.generateCopilotInlineCompletion(reqNull);

        assertNotNull(respNull);
        assertTrue(respNull.ghostText().contains("Objects.requireNonNull"));
        assertEquals("STATEMENT", respNull.suggestions().get(0).kind());

        CopilotCompletionRequest reqOther = new CopilotCompletionRequest("int count = 0;", "Count.java");
        CopilotCompletionResponse respOther = engine.generateCopilotInlineCompletion(reqOther);

        assertTrue(respOther.ghostText().contains("Objects.requireNonNull"));
    }
}

