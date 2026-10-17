package com.oenexa.application;

import com.oenexa.domain.ChatDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MultiModelRouterService Zero-Mock Unit Tests")
class MultiModelRouterServiceTest {

    private final MultiModelRouterService router = new MultiModelRouterService();

    @Test
    @DisplayName("Should autonomously route tasks to optimal models")
    void shouldRouteAutonomousTasks() {
        ModelRoutingDecision coding = router.routeTask(TaskType.CODING, null);
        assertEquals(ModelProvider.ANTHROPIC_CLAUDE, coding.provider());
        assertEquals("claude-3-5-sonnet", coding.modelName());

        ModelRoutingDecision reasoning = router.routeTask(TaskType.REASONING, null);
        assertEquals(ModelProvider.ANTHROPIC_CLAUDE, reasoning.provider());

        ModelRoutingDecision fast = router.routeTask(TaskType.FAST_CHAT, null);
        assertEquals(ModelProvider.DEEPSEEK, fast.provider());
        assertEquals("deepseek-v3", fast.modelName());

        ModelRoutingDecision repo = router.routeTask(TaskType.REPO_ANALYSIS, null);
        assertEquals(ModelProvider.ANTHROPIC_CLAUDE, repo.provider());

        ModelRoutingDecision bugFix = router.routeTask(TaskType.BUG_FIX, null);
        assertEquals(ModelProvider.OPENAI_GPT, bugFix.provider());
        assertEquals("gpt-4o", bugFix.modelName());

        ModelRoutingDecision sec = router.routeTask(TaskType.SECURITY_AUDIT, null);
        assertEquals(ModelProvider.OENEXA_CUSTOM, sec.provider());

        ModelRoutingDecision arch = router.routeTask(TaskType.ARCHITECTURE, null);
        assertEquals(ModelProvider.GEMINI, arch.provider());
        assertEquals("gemini-2.5-flash", arch.modelName());

        ModelRoutingDecision pm = router.routeTask(TaskType.PRODUCT_MANAGEMENT, null);
        assertEquals(ModelProvider.ANTHROPIC_CLAUDE, pm.provider());
        assertEquals("claude-3-5-sonnet", pm.modelName());

        ModelRoutingDecision mm = router.routeTask(TaskType.MULTIMODAL_ANALYSIS, null);
        assertEquals(ModelProvider.GEMINI, mm.provider());
        assertEquals("gemini-2.5-flash", mm.modelName());
    }

    @ParameterizedTest(name = "Should honor explicit model preference: {0} -> {1}")
    @CsvSource({
            "claude, ANTHROPIC_CLAUDE, claude-3-5-sonnet",
            "gpt, OPENAI_GPT, gpt-4o",
            "deepseek, DEEPSEEK, deepseek-v3",
            "mistral, MISTRAL, mistral-large",
            "llama, LLAMA, llama-3.3-70b",
            "oenexa, OENEXA_CUSTOM, oenexa-expert-code-v1",
            "gemini, GEMINI, gemini-2.5-flash",
            "ollama, LOCAL_OLLAMA, llama3.3:latest",
            "local, LOCAL_OLLAMA, llama3.3:latest",
            "private, LOCAL_PRIVATE, deepseek-r1:14b"
    })
    void shouldHonorExplicitPreferences(String pref, ModelProvider expectedProvider, String expectedModel) {
        ModelRoutingDecision decision = router.routeTask(TaskType.CODING, pref);
        assertEquals(expectedProvider, decision.provider());
        assertEquals(expectedModel, decision.modelName());
    }

    @Test
    @DisplayName("Should classify message intent into TaskType and route directly from message")
    void shouldRouteFromMessage() {
        ModelRoutingDecision d0 = router.routeMessage("Draft PRD with user stories and acceptance criteria", null);
        assertEquals(TaskType.PRODUCT_MANAGEMENT, d0.taskType());
        assertEquals(ModelProvider.ANTHROPIC_CLAUDE, d0.provider());

        ModelRoutingDecision d00 = router.routeMessage("Analyze uploaded architecture image and audio screenshot", null);
        assertEquals(TaskType.MULTIMODAL_ANALYSIS, d00.taskType());
        assertEquals(ModelProvider.GEMINI, d00.provider());

        ModelRoutingDecision d1 = router.routeMessage("Show system context architecture blueprint", "auto");
        assertEquals(TaskType.ARCHITECTURE, d1.taskType());
        assertEquals(ModelProvider.GEMINI, d1.provider());

        ModelRoutingDecision d2 = router.routeMessage("Fix NPE error stack trace", null);
        assertEquals(TaskType.BUG_FIX, d2.taskType());
        assertEquals(ModelProvider.OPENAI_GPT, d2.provider());

        ModelRoutingDecision d3 = router.routeMessage("Scan for SAST OWASP vulnerabilities", null);
        assertEquals(TaskType.SECURITY_AUDIT, d3.taskType());

        ModelRoutingDecision d4 = router.routeMessage("Write and implement code for API endpoint", null);
        assertEquals(TaskType.CODING, d4.taskType());

        ModelRoutingDecision d5 = router.routeMessage("Explain the tradeoff between monolithic and microservice", null);
        assertEquals(TaskType.REASONING, d5.taskType());

        ModelRoutingDecision d6 = router.routeMessage("Index codebase repository for knowledge map", null);
        assertEquals(TaskType.REPO_ANALYSIS, d6.taskType());

        ModelRoutingDecision d7 = router.routeMessage("Hello, good morning", null);
        assertEquals(TaskType.FAST_CHAT, d7.taskType());

        ModelRoutingDecision d8 = router.routeMessage(null, null);
        assertEquals(TaskType.FAST_CHAT, d8.taskType());
    }

    @Test
    @DisplayName("Should throw NPE on null task type")
    void shouldThrowNpeOnNullTaskType() {
        assertThrows(NullPointerException.class, () -> router.routeTask(null, "auto"));
    }
}
