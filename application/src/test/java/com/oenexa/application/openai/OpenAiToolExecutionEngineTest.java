package com.oenexa.application.openai;

import com.oenexa.application.engine.AstAnalysisEngine;
import com.oenexa.application.engine.SecurityAuditEngine;
import com.oenexa.application.engine.TestSynthesizerEngine;
import com.oenexa.domain.OpenAiDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenAiToolExecutionEngine Zero-Mock Unit Tests")
class OpenAiToolExecutionEngineTest {

    private final OpenAiToolExecutionEngine engine = new OpenAiToolExecutionEngine();

    @Test
    @DisplayName("Should return registered tools list")
    void testRegisteredTools() {
        var tools = engine.getRegisteredTools();
        assertEquals(6, tools.size());
        assertTrue(tools.stream().anyMatch(t -> "ast_analyzer".equals(t.function().name())));
        assertTrue(tools.stream().anyMatch(t -> "sast_security_scanner".equals(t.function().name())));
        assertTrue(tools.stream().anyMatch(t -> "zero_mock_test_synthesizer".equals(t.function().name())));
        assertTrue(tools.stream().anyMatch(t -> "workspace_patch_engine".equals(t.function().name())));
        assertTrue(tools.stream().anyMatch(t -> "devops_sandbox_runner".equals(t.function().name())));
        assertTrue(tools.stream().anyMatch(t -> "git_workflow_engine".equals(t.function().name())));
    }

    @Test
    @DisplayName("Should execute ast_analyzer tool successfully")
    void testExecuteAstAnalyzer() {
        var call = new OpenAiToolCall("call-1", "function", new OpenAiFunctionCall("ast_analyzer", "{\"code\":\"public class Sample {}\",\"filePath\":\"src/Sample.java\"}"));
        var res = engine.executeTool(call);
        assertEquals("call-1", res.toolCallId());
        assertEquals("ast_analyzer", res.toolName());
        assertTrue(res.success());
        assertTrue(res.output().contains("AST Analysis Complete"));
    }

    @Test
    @DisplayName("Should execute sast_security_scanner tool successfully")
    void testExecuteSecurityScanner() {
        var call = new OpenAiToolCall("call-2", "function", new OpenAiFunctionCall("sast_security_scanner", "{\"sourceCode\":\"public class Safe {}\"}"));
        var res = engine.executeTool(call);
        assertEquals("sast_security_scanner", res.toolName());
        assertTrue(res.success());
        assertTrue(res.output().contains("SAST Audit Complete"));
    }

    @Test
    @DisplayName("Should execute zero_mock_test_synthesizer tool and generate code artifact")
    void testExecuteTestSynthesizer() {
        var call = new OpenAiToolCall("call-3", "function", new OpenAiFunctionCall("zero_mock_test_synthesizer", "{\"domainClassCode\":\"public record User(String id) {}\",\"targetPackage\":\"com.oenexa.test\"}"));
        var res = engine.executeTool(call);
        assertEquals("zero_mock_test_synthesizer", res.toolName());
        assertTrue(res.success());
        assertTrue(res.generatedArtifact().isPresent());
        assertTrue(res.generatedArtifact().get().codeContent().contains("assertNotNull"));
    }

    @Test
    @DisplayName("Should execute workspace_patch_engine tool successfully")
    void testExecutePatchEngine() {
        var call = new OpenAiToolCall("call-4", "function", new OpenAiFunctionCall("workspace_patch_engine", "{\"filePath\":\"src/Service.java\"}"));
        var res = engine.executeTool(call);
        assertEquals("workspace_patch_engine", res.toolName());
        assertTrue(res.success());
        assertTrue(res.output().contains("Unified Patch applied cleanly"));
    }

    @Test
    @DisplayName("Should execute devops_sandbox_runner tool and generate Dockerfile")
    void testExecuteDevOpsRunner() {
        var call = new OpenAiToolCall("call-5", "function", new OpenAiFunctionCall("devops_sandbox_runner", "{\"serviceName\":\"payment-svc\"}"));
        var res = engine.executeTool(call);
        assertEquals("devops_sandbox_runner", res.toolName());
        assertTrue(res.success());
        assertTrue(res.generatedArtifact().isPresent());
        assertEquals("Dockerfile", res.generatedArtifact().get().title());
    }

    @Test
    @DisplayName("Should execute git_workflow_engine tool successfully")
    void testExecuteGitWorkflow() {
        var call = new OpenAiToolCall("call-6", "function", new OpenAiFunctionCall("git_workflow_engine", "{\"branchName\":\"feature/argon\"}"));
        var res = engine.executeTool(call);
        assertEquals("git_workflow_engine", res.toolName());
        assertTrue(res.success());
        assertTrue(res.output().contains("Git validation passed"));
    }

    @Test
    @DisplayName("Should execute unknown tool via fallback runner")
    void testExecuteUnknownTool() {
        var call = new OpenAiToolCall("call-7", "function", new OpenAiFunctionCall("custom_tool", "{}"));
        var res = engine.executeTool(call);
        assertEquals("custom_tool", res.toolName());
        assertTrue(res.success());
        assertTrue(res.output().contains("fallback runner"));
    }

    @Test
    @DisplayName("Should throw NullPointerException for null arguments")
    void testNullGuards() {
        assertThrows(NullPointerException.class, () -> engine.executeTool(null));
        assertThrows(NullPointerException.class, () -> new OpenAiToolExecutionEngine(null, new SecurityAuditEngine(), new TestSynthesizerEngine()));
        assertThrows(NullPointerException.class, () -> new OpenAiToolExecutionEngine(new AstAnalysisEngine(), null, new TestSynthesizerEngine()));
        assertThrows(NullPointerException.class, () -> new OpenAiToolExecutionEngine(new AstAnalysisEngine(), new SecurityAuditEngine(), null));
    }
}
