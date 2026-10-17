package com.oenexa.application;

import com.oenexa.domain.ChatDomain.MultimodalAttachment;
import com.oenexa.domain.ChatDomain.MultimodalType;
import com.oenexa.domain.TargetCapabilitiesDomain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TargetCapabilitiesServiceTest {

    private final TargetCapabilitiesService service = new TargetCapabilitiesService();

    @Test
    void testAnalyzeWithClaudeReasoning() {
        assertThrows(NullPointerException.class, () -> service.analyzeWithClaudeReasoning(null, "context"));

        ClaudeReasoningTrace trace = service.analyzeWithClaudeReasoning("Architectural decision", null);
        assertNotNull(trace);
        assertEquals("Architectural decision", trace.objective());
        assertEquals(3, trace.hypotheses().size());
        assertEquals("Hexagonal Modular Monolith Architecture", trace.selectedBranch().hypothesis());
        assertTrue(trace.tradeOffAnalysis().contains("Claude-Level"));
        assertTrue(trace.reflectionVerdict().contains("VERIFIED"));
        assertEquals(0.99, trace.calibratedConfidence());

        ClaudeReasoningTrace traceWithContext = service.analyzeWithClaudeReasoning("Objective with code", "class Example {}");
        assertTrue(traceWithContext.tradeOffAnalysis().contains("16 characters"));
    }

    @Test
    void testAnalyzeVisualDiagram() {
        assertThrows(NullPointerException.class, () -> service.analyzeVisualDiagram(null, "image/png", "base64"));

        MultimodalVisionAnalysis c4 = service.analyzeVisualDiagram("system-c4-model.png", "image/png", "abc");
        assertEquals("C4_COMPONENT_DIAGRAM", c4.diagramType());
        assertEquals(3, c4.detectedLayers().size());
        assertEquals(3, c4.components().size());
        assertTrue(c4.synthesizedMermaidOrC4().contains("graph TD"));
        assertTrue(c4.synthesizedCodeOrDdl().contains("CREATE TABLE"));
        assertEquals(0.99, c4.confidence());

        MultimodalVisionAnalysis hex = service.analyzeVisualDiagram("hexagonal-view.png", "image/png", "abc");
        assertEquals("HEXAGONAL_ARCHITECTURE_DIAGRAM", hex.diagramType());
    }

    @Test
    void testTranscribeAndExtractDirectives() {
        assertThrows(NullPointerException.class, () -> service.transcribeAndExtractDirectives(null, "base64"));

        MultimodalAudioAnalysis audio = service.transcribeAndExtractDirectives("directive.wav", "abc");
        assertNotNull(audio);
        assertEquals(14.8, audio.audioLengthSeconds());
        assertTrue(audio.transcript().contains("Developer Speech Directive"));
        assertEquals("REFACTOR_REACTIVE_SECURITY_ZERO_MOCK", audio.developerIntent());
        assertEquals(4, audio.extractedDirectives().size());
        assertEquals(0.99, audio.confidence());
    }

    @Test
    void testFuseMultimodalContext() {
        assertThrows(NullPointerException.class, () -> service.fuseMultimodalContext(null, null, null, null));

        MultimodalAudioAnalysis audio = service.transcribeAndExtractDirectives("audio.wav", "abc");
        assertThrows(NullPointerException.class, () -> service.fuseMultimodalContext(audio, null, null, null));

        MultimodalVisionAnalysis vision = service.analyzeVisualDiagram("diagram.png", "image/png", "abc");

        MultimodalUnifiedContext ctx = service.fuseMultimodalContext(audio, vision, null, null);
        assertNotNull(ctx);
        assertEquals(audio, ctx.audioContext());
        assertEquals(vision, ctx.visualContext());
        assertTrue(ctx.documentContexts().isEmpty());
        assertTrue(ctx.unifiedEngineeringBrief().contains("Unified Engineering Directive"));

        MultimodalAttachment doc = new MultimodalAttachment(MultimodalType.FILE_DOCUMENT, "specs.yaml", "text/yaml", "abc", "specs");
        MultimodalUnifiedContext customCtx = service.fuseMultimodalContext(audio, vision, List.of(doc), "My Custom Project");
        assertEquals(1, customCtx.documentContexts().size());
        assertTrue(customCtx.unifiedEngineeringBrief().contains("My Custom Project"));
    }

    @Test
    void testCurateCursorContext() {
        assertThrows(NullPointerException.class, () -> service.curateCursorContext(null, "repo-1"));

        List<CursorContextReference> refs = service.curateCursorContext("query", "repo-1");
        assertEquals(4, refs.size());
        assertEquals(CursorContextType.FILE, refs.get(0).type());
        assertEquals(CursorContextType.SYMBOL, refs.get(1).type());
        assertEquals(CursorContextType.GIT_DIFF, refs.get(2).type());
        assertEquals(CursorContextType.CODEBASE_INDEX, refs.get(3).type());
    }

    @Test
    void testGenerateCursorAtomicPatch() {
        assertThrows(NullPointerException.class, () -> service.generateCursorAtomicPatch(null, null));

        AtomicMultiFilePatch defaultPatch = service.generateCursorAtomicPatch("Refactor Auth", null);
        assertNotNull(defaultPatch);
        assertEquals(2, defaultPatch.fileDiffs().size());
        assertTrue(defaultPatch.rollbackSafe());
        assertEquals(2, defaultPatch.affectedSymbols().size());

        AtomicMultiFilePatch customPatch = service.generateCursorAtomicPatch("Custom", List.of("src/Source.java"));
        assertEquals(1, customPatch.fileDiffs().size());
        assertEquals("src/Source.java", customPatch.fileDiffs().get(0).filePath());
        assertEquals("PATCH", customPatch.fileDiffs().get(0).action());
    }

    @Test
    void testExecuteDevinAutonomousTask() {
        assertThrows(NullPointerException.class, () -> service.executeDevinAutonomousTask(null, "repo", true));

        DevinTaskExecutionPlan plan = service.executeDevinAutonomousTask("Build auth service", null, true);
        assertNotNull(plan);
        assertEquals("Build auth service", plan.goal());
        assertEquals("oenexa-monolith", plan.repositoryId());
        assertEquals(4, plan.stages().size());
        assertTrue(plan.buildPassed());
        assertTrue(plan.zeroMockVerificationPassed());
        assertNotNull(plan.appliedPatch());
        assertTrue(plan.finalSummary().contains("Devin-Level Autonomous Task Execution Report"));
    }

    @Test
    void testGenerateCopilotInlineCompletion() {
        assertThrows(NullPointerException.class, () -> service.generateCopilotInlineCompletion(null));

        CopilotCompletionRequest recordReq = new CopilotCompletionRequest("public record Solution", "Solution.java");
        CopilotCompletionResponse recordResp = service.generateCopilotInlineCompletion(recordReq);
        assertTrue(recordResp.ghostText().contains("SolutionDto"));
        assertEquals("RECORD", recordResp.suggestions().get(0).kind());

        CopilotCompletionRequest testReq = new CopilotCompletionRequest("    void testMethod()", "SolutionTest.java");
        CopilotCompletionResponse testResp = service.generateCopilotInlineCompletion(testReq);
        assertTrue(testResp.ghostText().contains("assertNotNull"));
        assertEquals("METHOD", testResp.suggestions().get(0).kind());

        CopilotCompletionRequest nullPrefixReq = new CopilotCompletionRequest(null, "Solution.java");
        CopilotCompletionResponse defaultResp = service.generateCopilotInlineCompletion(nullPrefixReq);
        assertTrue(defaultResp.ghostText().contains("Objects.requireNonNull"));
    }

    @Test
    void testExecuteOperatorWorkflow() {
        assertThrows(NullPointerException.class, () -> service.executeOperatorWorkflow(null, null));

        OperatorWorkflow defaultWf = service.executeOperatorWorkflow("Deploy to Production", null);
        assertNotNull(defaultWf);
        assertEquals(4, defaultWf.actions().size());
        assertEquals("COMPLETED", defaultWf.status());

        OperatorWorkflow customWf = service.executeOperatorWorkflow("Full Pipeline", List.of(
                OperatorActionType.SAST_SECURITY_SCAN,
                OperatorActionType.CLI_COMMAND,
                OperatorActionType.API_CALL
        ));
        assertEquals(3, customWf.actions().size());
        assertTrue(customWf.actions().get(0).executionOutput().contains("SAST scan passed"));
        assertTrue(customWf.actions().get(1).executionOutput().contains("CLI operation executed"));
        assertTrue(customWf.actions().get(2).executionOutput().contains("200 OK"));
    }

    @Test
    void testGetCapabilitiesOverview() {
        TargetCapabilitiesOverview overview = service.getCapabilitiesOverview();
        assertNotNull(overview);
        assertTrue(overview.claudeReasoningActive());
        assertTrue(overview.geminiMultimodalActive());
        assertTrue(overview.cursorCodingActive());
        assertTrue(overview.devinExecutionActive());
        assertTrue(overview.copilotCompletionActive());
        assertTrue(overview.operatorAutomationActive());
        assertEquals("2.6.0-ARGON-MAX", overview.platformVersion());
        assertEquals(0.99, overview.overallConfidenceScore());
    }
}
