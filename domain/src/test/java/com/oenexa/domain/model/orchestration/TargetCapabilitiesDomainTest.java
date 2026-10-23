package com.oenexa.domain.model.orchestration;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TargetCapabilitiesDomainTest {

    @Test
    void testClaudeReasoningRecords() {
        assertNotNull(new TargetCapabilitiesDomain());

        // HypothesisBranch
        TargetCapabilitiesDomain.HypothesisBranch branchDefault = new TargetCapabilitiesDomain.HypothesisBranch(
                "Hexagonal Modular Monolith", "Ensures strict boundary isolation", 0.98, 0.95
        );
        assertNotNull(branchDefault.branchId());
        assertEquals("Hexagonal Modular Monolith", branchDefault.hypothesis());
        assertEquals("Ensures strict boundary isolation", branchDefault.rationale());
        assertEquals(3, branchDefault.pros().size());
        assertEquals(1, branchDefault.cons().size());
        assertEquals(0.98, branchDefault.architecturalScore());
        assertEquals(0.95, branchDefault.feasibilityScore());
        assertEquals(0.965, branchDefault.confidenceScore(), 0.001);

        TargetCapabilitiesDomain.HypothesisBranch branchCustom = new TargetCapabilitiesDomain.HypothesisBranch(
                "b-1", "Reactive Microservices", "Independent scaling", List.of("Scalable"), List.of("Complex"), 0.85, 0.80, 0.825
        );
        assertEquals("b-1", branchCustom.branchId());
        assertEquals("Reactive Microservices", branchCustom.hypothesis());

        TargetCapabilitiesDomain.HypothesisBranch branch7Arg = new TargetCapabilitiesDomain.HypothesisBranch(
                "Event-Driven", "Kafka topics", List.of("Async"), List.of("Lag"), 0.90, 0.85, 0.875
        );
        assertNotNull(branch7Arg.branchId());
        assertEquals("Event-Driven", branch7Arg.hypothesis());

        // ClaudeReasoningTrace
        TargetCapabilitiesDomain.ClaudeReasoningTrace traceDefault = new TargetCapabilitiesDomain.ClaudeReasoningTrace(
                "Design auth system", List.of(branchDefault, branchCustom), branchDefault,
                "Hexagonal monolith scores highest on simplicity and strict zero-mock testability.",
                "Selected Hexagonal Monolith: optimal balance."
        );
        assertNotNull(traceDefault.traceId());
        assertEquals("Design auth system", traceDefault.objective());
        assertEquals(2, traceDefault.hypotheses().size());
        assertEquals(branchDefault, traceDefault.selectedBranch());
        assertEquals(0.99, traceDefault.calibratedConfidence());
        assertNotNull(traceDefault.timestamp());

        Instant now = Instant.now();
        TargetCapabilitiesDomain.ClaudeReasoningTrace traceCustom = new TargetCapabilitiesDomain.ClaudeReasoningTrace(
                "t-1", "Design cache", List.of(branchDefault), branchDefault, "Tradeoff", "Verdict", 0.95, now
        );
        assertEquals("t-1", traceCustom.traceId());
        assertEquals(now, traceCustom.timestamp());
    }

    @Test
    void testGeminiMultimodalRecords() {
        // DetectedComponent
        TargetCapabilitiesDomain.DetectedComponent compDefault = new TargetCapabilitiesDomain.DetectedComponent(
                "AuthFilter", "SECURITY_GATEWAY"
        );
        assertNotNull(compDefault.id());
        assertEquals("AuthFilter", compDefault.name());
        assertEquals("SECURITY_GATEWAY", compDefault.layerType());
        assertEquals("x:100,y:200,w:300,h:150", compDefault.boundingBox());
        assertTrue(compDefault.downstreamTargets().isEmpty());

        TargetCapabilitiesDomain.DetectedComponent compCustom = new TargetCapabilitiesDomain.DetectedComponent(
                "c-1", "DatabaseAdapter", "PERSISTENCE", "x:0,y:0", List.of("postgres")
        );
        assertEquals("c-1", compCustom.id());
        assertEquals(1, compCustom.downstreamTargets().size());

        // MultimodalVisionAnalysis
        TargetCapabilitiesDomain.MultimodalVisionAnalysis visionDefault = new TargetCapabilitiesDomain.MultimodalVisionAnalysis(
                "C4_COMPONENT_DIAGRAM", List.of("API", "DOMAIN", "INFRASTRUCTURE"), List.of(compDefault), "graph TD; A-->B;"
        );
        assertNotNull(visionDefault.analysisId());
        assertEquals("C4_COMPONENT_DIAGRAM", visionDefault.diagramType());
        assertEquals(3, visionDefault.detectedLayers().size());
        assertEquals(1, visionDefault.components().size());
        assertEquals("graph TD; A-->B;", visionDefault.synthesizedMermaidOrC4());
        assertEquals("", visionDefault.synthesizedCodeOrDdl());
        assertEquals(0.98, visionDefault.confidence());

        TargetCapabilitiesDomain.MultimodalVisionAnalysis visionCustom = new TargetCapabilitiesDomain.MultimodalVisionAnalysis(
                "v-1", "UML", List.of(), List.of(), "mermaid", "code", 0.99
        );
        assertEquals("v-1", visionCustom.analysisId());
        assertEquals("code", visionCustom.synthesizedCodeOrDdl());

        // MultimodalAudioAnalysis
        TargetCapabilitiesDomain.MultimodalAudioAnalysis audioDefault = new TargetCapabilitiesDomain.MultimodalAudioAnalysis(
                12.5, "Refactor database entities to reactive WebFlux", "REFACTOR", List.of("use WebFlux", "zero mocks")
        );
        assertNotNull(audioDefault.analysisId());
        assertEquals(12.5, audioDefault.audioLengthSeconds());
        assertEquals("Refactor database entities to reactive WebFlux", audioDefault.transcript());
        assertEquals("REFACTOR", audioDefault.developerIntent());
        assertEquals(2, audioDefault.extractedDirectives().size());
        assertEquals(0.99, audioDefault.confidence());

        TargetCapabilitiesDomain.MultimodalAudioAnalysis audioCustom = new TargetCapabilitiesDomain.MultimodalAudioAnalysis(
                "a-1", 5.0, "text", "INTENT", List.of(), 0.95
        );
        assertEquals("a-1", audioCustom.analysisId());

        // MultimodalUnifiedContext
        TargetCapabilitiesDomain.MultimodalUnifiedContext ctxDefault = new TargetCapabilitiesDomain.MultimodalUnifiedContext(
                audioDefault, visionDefault, "Unified Engineering Brief for reactive auth architecture."
        );
        assertNotNull(ctxDefault.contextId());
        assertEquals(audioDefault, ctxDefault.audioContext());
        assertEquals(visionDefault, ctxDefault.visualContext());
        assertTrue(ctxDefault.documentContexts().isEmpty());
        assertEquals("Unified Engineering Brief for reactive auth architecture.", ctxDefault.unifiedEngineeringBrief());
        assertNotNull(ctxDefault.createdAt());

        Instant now = Instant.now();
        TargetCapabilitiesDomain.MultimodalUnifiedContext ctxCustom = new TargetCapabilitiesDomain.MultimodalUnifiedContext(
                "ctx-1", audioDefault, visionDefault, List.of(), "brief", now
        );
        assertEquals("ctx-1", ctxCustom.contextId());
        assertEquals(now, ctxCustom.createdAt());
    }

    @Test
    void testCursorCodingRecords() {
        assertEquals(5, TargetCapabilitiesDomain.CursorContextType.values().length);
        assertEquals(TargetCapabilitiesDomain.CursorContextType.FILE, TargetCapabilitiesDomain.CursorContextType.valueOf("FILE"));
        assertEquals(TargetCapabilitiesDomain.CursorContextType.CODEBASE_INDEX, TargetCapabilitiesDomain.CursorContextType.valueOf("CODEBASE_INDEX"));

        // CursorContextReference
        TargetCapabilitiesDomain.CursorContextReference refDefault = new TargetCapabilitiesDomain.CursorContextReference(
                TargetCapabilitiesDomain.CursorContextType.SYMBOL, "AuthFilter", "public class AuthFilter {}"
        );
        assertNotNull(refDefault.refId());
        assertEquals(TargetCapabilitiesDomain.CursorContextType.SYMBOL, refDefault.type());
        assertEquals("AuthFilter", refDefault.targetPathOrSymbol());
        assertEquals(0.95, refDefault.relevanceScore());

        TargetCapabilitiesDomain.CursorContextReference refCustom = new TargetCapabilitiesDomain.CursorContextReference(
                "r-1", TargetCapabilitiesDomain.CursorContextType.FILE, "src/Auth.java", "code", 0.99
        );
        assertEquals("r-1", refCustom.refId());

        // FileDiff
        TargetCapabilitiesDomain.FileDiff diffDefault = new TargetCapabilitiesDomain.FileDiff(
                "src/Auth.java", "+ Objects.requireNonNull(token);", "PATCH"
        );
        assertEquals("src/Auth.java", diffDefault.filePath());
        assertEquals("sha256-pre-patch", diffDefault.originalHash());
        assertEquals("+ Objects.requireNonNull(token);", diffDefault.patchContent());
        assertEquals("PATCH", diffDefault.action());

        TargetCapabilitiesDomain.FileDiff diffCustom = new TargetCapabilitiesDomain.FileDiff(
                "src/User.java", "hash123", "content", "CREATE"
        );
        assertEquals("hash123", diffCustom.originalHash());

        // AtomicMultiFilePatch
        TargetCapabilitiesDomain.AtomicMultiFilePatch patchDefault = new TargetCapabilitiesDomain.AtomicMultiFilePatch(
                "Add defensive null guards across controllers", List.of(diffDefault), List.of("AuthFilter")
        );
        assertNotNull(patchDefault.patchId());
        assertEquals("Add defensive null guards across controllers", patchDefault.description());
        assertEquals(1, patchDefault.fileDiffs().size());
        assertEquals(1, patchDefault.affectedSymbols().size());
        assertTrue(patchDefault.rollbackSafe());
        assertNotNull(patchDefault.createdAt());

        Instant now = Instant.now();
        TargetCapabilitiesDomain.AtomicMultiFilePatch patchCustom = new TargetCapabilitiesDomain.AtomicMultiFilePatch(
                "p-1", "desc", List.of(), List.of(), false, now
        );
        assertEquals("p-1", patchCustom.patchId());
        assertFalse(patchCustom.rollbackSafe());
    }

    @Test
    void testDevinTaskExecutionRecords() {
        // DevinExecutionStage
        TargetCapabilitiesDomain.DevinExecutionStage stageDefault = new TargetCapabilitiesDomain.DevinExecutionStage(
                "Compile Codebase", "./gradlew compileJava", "BUILD SUCCESSFUL in 2s", "SUCCESS"
        );
        assertNotNull(stageDefault.stageId());
        assertEquals("Compile Codebase", stageDefault.name());
        assertEquals("./gradlew compileJava", stageDefault.commandExecuted());
        assertEquals("BUILD SUCCESSFUL in 2s", stageDefault.outputLog());
        assertEquals("SUCCESS", stageDefault.status());
        assertEquals(0, stageDefault.retryAttempt());
        assertNotNull(stageDefault.timestamp());

        Instant now = Instant.now();
        TargetCapabilitiesDomain.DevinExecutionStage stageCustom = new TargetCapabilitiesDomain.DevinExecutionStage(
                "s-1", "Test", "./gradlew test", "100% passed", "SUCCESS", 1, now
        );
        assertEquals("s-1", stageCustom.stageId());
        assertEquals(1, stageCustom.retryAttempt());
        assertEquals(now, stageCustom.timestamp());

        // DevinTaskExecutionPlan
        TargetCapabilitiesDomain.DevinTaskExecutionPlan planDefault = new TargetCapabilitiesDomain.DevinTaskExecutionPlan(
                "Refactor and test reactive auth", "oenexa-monolith", List.of(stageDefault), true, true
        );
        assertNotNull(planDefault.taskId());
        assertEquals("Refactor and test reactive auth", planDefault.goal());
        assertEquals("oenexa-monolith", planDefault.repositoryId());
        assertEquals(1, planDefault.stages().size());
        assertTrue(planDefault.buildPassed());
        assertTrue(planDefault.zeroMockVerificationPassed());
        assertNull(planDefault.appliedPatch());
        assertNotNull(planDefault.finalSummary());
        assertNotNull(planDefault.completedAt());

        TargetCapabilitiesDomain.DevinTaskExecutionPlan planCustom = new TargetCapabilitiesDomain.DevinTaskExecutionPlan(
                "task-1", "Goal", "repo", List.of(), false, false, null, "summary", now
        );
        assertEquals("task-1", planCustom.taskId());
        assertFalse(planCustom.buildPassed());
    }

    @Test
    void testCopilotCompletionRecords() {
        // CopilotCompletionItem
        TargetCapabilitiesDomain.CopilotCompletionItem item = new TargetCapabilitiesDomain.CopilotCompletionItem(
                "public record UserProfile(UUID id, String email) {}", "UserProfile record", "RECORD", "Java 25 Record definition", 0.99
        );
        assertEquals("public record UserProfile(UUID id, String email) {}", item.text());
        assertEquals("UserProfile record", item.label());
        assertEquals("RECORD", item.kind());
        assertEquals("Java 25 Record definition", item.documentation());
        assertEquals(0.99, item.confidence());

        // CopilotCompletionRequest
        TargetCapabilitiesDomain.CopilotCompletionRequest reqDefault = new TargetCapabilitiesDomain.CopilotCompletionRequest(
                "public class Solution {", "src/Solution.java"
        );
        assertEquals("public class Solution {", reqDefault.prefixCode());
        assertEquals("", reqDefault.suffixCode());
        assertEquals("src/Solution.java", reqDefault.filePath());
        assertEquals("java", reqDefault.language());
        assertEquals(1, reqDefault.cursorLine());
        assertEquals(23, reqDefault.cursorColumn());

        TargetCapabilitiesDomain.CopilotCompletionRequest reqNullPrefix = new TargetCapabilitiesDomain.CopilotCompletionRequest(
                null, "src/Empty.java"
        );
        assertEquals(0, reqNullPrefix.cursorColumn());

        TargetCapabilitiesDomain.CopilotCompletionRequest reqCustom = new TargetCapabilitiesDomain.CopilotCompletionRequest(
                "prefix", "suffix", "file.ts", "typescript", 5, 10
        );
        assertEquals("typescript", reqCustom.language());
        assertEquals(5, reqCustom.cursorLine());
        assertEquals(10, reqCustom.cursorColumn());

        // CopilotCompletionResponse
        TargetCapabilitiesDomain.CopilotCompletionResponse respDefault = new TargetCapabilitiesDomain.CopilotCompletionResponse(
                List.of(item), "public record UserProfile(UUID id, String email) {}"
        );
        assertNotNull(respDefault.completionId());
        assertEquals(1, respDefault.suggestions().size());
        assertEquals("public record UserProfile(UUID id, String email) {}", respDefault.ghostText());
        assertEquals(15L, respDefault.generationLatencyMs());
        assertEquals(0.98, respDefault.confidenceScore());

        TargetCapabilitiesDomain.CopilotCompletionResponse respCustom = new TargetCapabilitiesDomain.CopilotCompletionResponse(
                "c-1", List.of(), "ghost", 8L, 0.99
        );
        assertEquals("c-1", respCustom.completionId());
        assertEquals(8L, respCustom.generationLatencyMs());
    }

    @Test
    void testOperatorAutomationRecords() {
        assertEquals(7, TargetCapabilitiesDomain.OperatorActionType.values().length);
        assertEquals(TargetCapabilitiesDomain.OperatorActionType.DOCKER_BUILD, TargetCapabilitiesDomain.OperatorActionType.valueOf("DOCKER_BUILD"));
        assertEquals(TargetCapabilitiesDomain.OperatorActionType.K8S_DEPLOY, TargetCapabilitiesDomain.OperatorActionType.valueOf("K8S_DEPLOY"));

        // OperatorAction
        TargetCapabilitiesDomain.OperatorAction actionDefault = new TargetCapabilitiesDomain.OperatorAction(
                TargetCapabilitiesDomain.OperatorActionType.DOCKER_BUILD, "Dockerfile", "Built image oenexa:latest successfully"
        );
        assertNotNull(actionDefault.actionId());
        assertEquals(TargetCapabilitiesDomain.OperatorActionType.DOCKER_BUILD, actionDefault.actionType());
        assertEquals("Dockerfile", actionDefault.targetResource());
        assertTrue(actionDefault.payload().isEmpty());
        assertEquals("SUCCESS", actionDefault.resultStatus());
        assertEquals("Built image oenexa:latest successfully", actionDefault.executionOutput());
        assertNotNull(actionDefault.timestamp());

        Instant now = Instant.now();
        TargetCapabilitiesDomain.OperatorAction actionCustom = new TargetCapabilitiesDomain.OperatorAction(
                "a-1", TargetCapabilitiesDomain.OperatorActionType.CLI_COMMAND, "bash", Map.of("cmd", "ls"), "DONE", "output", now
        );
        assertEquals("a-1", actionCustom.actionId());
        assertEquals(1, actionCustom.payload().size());
        assertEquals(now, actionCustom.timestamp());

        // OperatorWorkflow
        TargetCapabilitiesDomain.OperatorWorkflow workflowDefault = new TargetCapabilitiesDomain.OperatorWorkflow(
                "Deploy Canary Release", "Build image, run migrations, deploy k8s manifests", List.of(actionDefault)
        );
        assertNotNull(workflowDefault.workflowId());
        assertEquals("Deploy Canary Release", workflowDefault.title());
        assertEquals(1, workflowDefault.actions().size());
        assertEquals("COMPLETED", workflowDefault.status());
        assertNotNull(workflowDefault.startedAt());
        assertNotNull(workflowDefault.completedAt());

        TargetCapabilitiesDomain.OperatorWorkflow workflowCustom = new TargetCapabilitiesDomain.OperatorWorkflow(
                "w-1", "title", "obj", List.of(), "IN_PROGRESS", now, now
        );
        assertEquals("w-1", workflowCustom.workflowId());
        assertEquals("IN_PROGRESS", workflowCustom.status());

        // TargetCapabilitiesOverview
        TargetCapabilitiesDomain.TargetCapabilitiesOverview overviewDefault = new TargetCapabilitiesDomain.TargetCapabilitiesOverview();
        assertTrue(overviewDefault.claudeReasoningActive());
        assertTrue(overviewDefault.geminiMultimodalActive());
        assertTrue(overviewDefault.cursorCodingActive());
        assertTrue(overviewDefault.devinExecutionActive());
        assertTrue(overviewDefault.copilotCompletionActive());
        assertTrue(overviewDefault.operatorAutomationActive());
        assertEquals("2.6.0-OENEXA-MAX", overviewDefault.platformVersion());
        assertEquals(0.99, overviewDefault.overallConfidenceScore());

        TargetCapabilitiesDomain.TargetCapabilitiesOverview overviewCustom = new TargetCapabilitiesDomain.TargetCapabilitiesOverview(
                true, false, true, false, true, false, "v1", 0.90
        );
        assertFalse(overviewCustom.geminiMultimodalActive());
        assertEquals("v1", overviewCustom.platformVersion());
    }
}

