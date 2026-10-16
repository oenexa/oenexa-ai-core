package ai.oenexa.chat.application;

import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.ChatMessage.Role;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GeminiReasoningEngine Zero-Mock Unit Tests")
class GeminiReasoningEngineTest {

    private final GeminiReasoningEngine engine = new GeminiReasoningEngine();

    @ParameterizedTest(name = "Should determine intent {1} for input: {0}")
    @CsvSource({
            "Please explain system architecture and design, ARCHITECTURE_DESIGN",
            "Perform security scan for OWASP vulnerabilities, SECURITY_AUDIT",
            "There is a bug in the code causing NullPointerException, BUG_FIX_ANALYSIS",
            "Refactor code to clean smells, CODE_REFACTORING",
            "Setup devops docker pipeline for k8s, DEVOPS_PIPELINE",
            "Generate test suite with junit and bdd coverage, TESTING_SYNTHESIS",
            "Write and create code for new service, CODE_GENERATION",
            "Hello there assistant, GENERAL_ASSISTANT",
            "'', GENERAL_ASSISTANT"
    })
    void shouldDetermineIntentAccurately(String input, String expectedIntent) {
        assertThat(engine.determineIntent(input)).isEqualTo(expectedIntent);
    }

    @Test
    void shouldHandleNullIntentGracefully() {
        assertThat(engine.determineIntent(null)).isEqualTo("GENERAL_ASSISTANT");
    }

    @Test
    void shouldGenerateThoughtForAllIntents() {
        String[] intents = {
                "ARCHITECTURE_DESIGN", "SECURITY_AUDIT", "BUG_FIX_ANALYSIS",
                "CODE_REFACTORING", "DEVOPS_PIPELINE", "TESTING_SYNTHESIS",
                "CODE_GENERATION", "GENERAL_ASSISTANT"
        };

        for (String intent : intents) {
            String thought = engine.generateThought(intent, "Sample query", "gemini-2.5-pro");
            assertThat(thought).startsWith("<thought>").endsWith("</thought>\n\n");
            assertThat(thought).contains("gemini-2.5-pro");

            // Test with null / blank model fallback
            String defaultThought = engine.generateThought(intent, "Sample query", null);
            assertThat(defaultThought).contains("gemini-2.5-flash");
        }
    }

    @Test
    void shouldGenerateResponseForAllIntents() {
        String[] intents = {
                "ARCHITECTURE_DESIGN", "SECURITY_AUDIT", "BUG_FIX_ANALYSIS",
                "CODE_REFACTORING", "DEVOPS_PIPELINE", "TESTING_SYNTHESIS",
                "CODE_GENERATION", "GENERAL_ASSISTANT"
        };

        for (String intent : intents) {
            String resp = engine.generateResponse(intent, "Sample query");
            assertThat(resp).isNotBlank();
        }
    }

    @Test
    void shouldSuggestFollowUpsForAllIntents() {
        String[] intents = {
                "ARCHITECTURE_DESIGN", "SECURITY_AUDIT", "BUG_FIX_ANALYSIS",
                "CODE_REFACTORING", "DEVOPS_PIPELINE", "TESTING_SYNTHESIS",
                "CODE_GENERATION", "GENERAL_ASSISTANT"
        };

        for (String intent : intents) {
            List<String> followUps = engine.suggestFollowUps(intent);
            assertThat(followUps).isNotEmpty();
        }
    }

    @Test
    void shouldProcessTurnResultWithAndWithoutThinking() {
        var withThinking = engine.process("Check architecture", "gemini-2.5-flash", true);
        assertThat(withThinking.intent()).isEqualTo("ARCHITECTURE_DESIGN");
        assertThat(withThinking.thought()).isNotBlank();
        assertThat(withThinking.response()).contains("Polyrepo / Hexagonal");
        assertThat(withThinking.followUps()).isNotEmpty();

        var withoutThinking = engine.process("Check architecture", "gemini-2.5-flash", false);
        assertThat(withoutThinking.thought()).isEmpty();
    }

    @Test
    void shouldStreamTokensForPrompt() {
        var messages = List.of(
                ChatMessage.of(Role.SYSTEM, "You are an assistant."),
                ChatMessage.of(Role.USER, "How to fix this bug?")
        );

        StepVerifier.create(engine.stream(messages, "gemini-2.5-pro"))
                .thenConsumeWhile(chunk -> !chunk.isEmpty())
                .verifyComplete();

        // Overloaded default stream
        StepVerifier.create(engine.stream(messages))
                .thenConsumeWhile(chunk -> !chunk.isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldHandleEmptyOrNullPromptInStream() {
        StepVerifier.create(engine.stream(null))
                .assertNext(chunk -> assertThat(chunk).contains("OENEXA AI Engineer: Ready."))
                .verifyComplete();

        StepVerifier.create(engine.stream(List.of()))
                .assertNext(chunk -> assertThat(chunk).contains("OENEXA AI Engineer: Ready."))
                .verifyComplete();
    }
}
