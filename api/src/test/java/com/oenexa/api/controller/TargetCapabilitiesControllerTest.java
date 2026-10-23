package com.oenexa.api.controller;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.api.controller.TargetCapabilitiesController.*;
import com.oenexa.application.service.orchestration.TargetCapabilitiesService;
import com.oenexa.domain.model.chat.ChatDomain.MultimodalAttachment;
import com.oenexa.domain.model.chat.ChatDomain.MultimodalType;
import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TargetCapabilitiesController Zero-Mock Unit Tests")
class TargetCapabilitiesControllerTest {

    private TargetCapabilitiesService service;
    private TargetCapabilitiesController controller;

    @BeforeEach
    void setUp() {
        service = new TargetCapabilitiesService();
        controller = new TargetCapabilitiesController(service);
    }

    @Test
    @DisplayName("Should reject null service in constructor")
    void shouldRejectNullService() {
        assertThrows(NullPointerException.class, () -> new TargetCapabilitiesController(null));
    }

    @Test
    @DisplayName("Should analyze with Claude Reasoning and return 200 OK")
    void shouldAnalyzeWithClaudeReasoning() {
        ClaudeReasoningRequest request = new ClaudeReasoningRequest("Design Hexagonal Core", "public record DomainEntity() {}");
        ResponseEntity<ClaudeReasoningTrace> response = controller.analyzeClaudeReasoning(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Design Hexagonal Core", response.getBody().objective());
        assertFalse(response.getBody().hypotheses().isEmpty());
        assertNotNull(response.getBody().selectedBranch());
        assertNotNull(response.getBody().reflectionVerdict());
    }

    @Test
    @DisplayName("Should reject invalid Claude reasoning request with 400 Bad Request")
    void shouldRejectInvalidClaudeReasoning() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.analyzeClaudeReasoning(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.analyzeClaudeReasoning(new ClaudeReasoningRequest(null, "ctx")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.analyzeClaudeReasoning(new ClaudeReasoningRequest("   ", "ctx")).getStatusCode());
    }

    @Test
    @DisplayName("Should analyze visual diagram with Gemini Multimodal Vision and return 200 OK")
    void shouldAnalyzeVisionDiagram() {
        MultimodalVisionRequest request = new MultimodalVisionRequest("arch_diagram.png", "image/png", "base64-image-bytes");
        ResponseEntity<MultimodalVisionAnalysis> response = controller.analyzeVisionDiagram(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("HEXAGONAL_ARCHITECTURE_DIAGRAM", response.getBody().diagramType());
        assertFalse(response.getBody().components().isEmpty());

        MultimodalVisionRequest requestC4 = new MultimodalVisionRequest("c4_architecture.png", "image/png", "b64");
        ResponseEntity<MultimodalVisionAnalysis> responseC4 = controller.analyzeVisionDiagram(requestC4);
        assertEquals(HttpStatus.OK, responseC4.getStatusCode());
        assertEquals("C4_COMPONENT_DIAGRAM", responseC4.getBody().diagramType());
    }

    @Test
    @DisplayName("Should reject invalid vision request with 400 Bad Request")
    void shouldRejectInvalidVisionRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.analyzeVisionDiagram(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.analyzeVisionDiagram(new MultimodalVisionRequest(null, "img/png", "b64")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.analyzeVisionDiagram(new MultimodalVisionRequest("   ", "img/png", "b64")).getStatusCode());
    }

    @Test
    @DisplayName("Should transcribe audio directives with Gemini Multimodal Audio and return 200 OK")
    void shouldTranscribeAudioDirectives() {
        MultimodalAudioRequest request = new MultimodalAudioRequest("developer_audio.wav", "base64-audio-bytes");
        ResponseEntity<MultimodalAudioAnalysis> response = controller.transcribeAudioDirectives(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().transcript().contains("Developer Speech Directive"));
        assertFalse(response.getBody().extractedDirectives().isEmpty());
    }

    @Test
    @DisplayName("Should reject invalid audio request with 400 Bad Request")
    void shouldRejectInvalidAudioRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.transcribeAudioDirectives(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.transcribeAudioDirectives(new MultimodalAudioRequest(null, "b64")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.transcribeAudioDirectives(new MultimodalAudioRequest("   ", "b64")).getStatusCode());
    }

    @Test
    @DisplayName("Should fuse multimodal context and return 200 OK")
    void shouldFuseMultimodalContext() {
        MultimodalAudioAnalysis audio = service.transcribeAndExtractDirectives("dev_voice.mp3", "b64");
        MultimodalVisionAnalysis vision = service.analyzeVisualDiagram("arch.png", "image/png", "b64");
        MultimodalAttachment doc = new MultimodalAttachment(MultimodalType.FILE_DOCUMENT, "SPEC.md", "text/markdown", "b64", "Spec content");
        MultimodalFusionRequest request = new MultimodalFusionRequest(audio, vision, List.of(doc), "Unified Context");

        ResponseEntity<MultimodalUnifiedContext> response = controller.fuseMultimodalContext(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().unifiedEngineeringBrief().contains("Unified Context"));
        assertNotNull(response.getBody().visualContext());
        assertNotNull(response.getBody().audioContext());
    }

    @Test
    @DisplayName("Should reject null multimodal fusion request with 400 Bad Request")
    void shouldRejectNullFusionRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.fuseMultimodalContext(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.fuseMultimodalContext(new MultimodalFusionRequest(null, null, List.of(), "title")).getStatusCode());
        MultimodalAudioAnalysis audio = service.transcribeAndExtractDirectives("dev_voice.mp3", "b64");
        assertEquals(HttpStatus.BAD_REQUEST, controller.fuseMultimodalContext(new MultimodalFusionRequest(audio, null, List.of(), "title")).getStatusCode());
        MultimodalVisionAnalysis vision = service.analyzeVisualDiagram("arch.png", "image/png", "b64");
        assertEquals(HttpStatus.BAD_REQUEST, controller.fuseMultimodalContext(new MultimodalFusionRequest(null, vision, List.of(), "title")).getStatusCode());
    }

    @Test
    @DisplayName("Should curate Cursor coding context and return 200 OK")
    void shouldCurateCursorContext() {
        CursorContextRequest request = new CursorContextRequest("@symbol TargetCapabilitiesService", "repo-1");
        ResponseEntity<List<CursorContextReference>> response = controller.curateCursorContext(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should reject invalid cursor context request with 400 Bad Request")
    void shouldRejectInvalidCursorContextRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.curateCursorContext(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.curateCursorContext(new CursorContextRequest(null, "r1")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.curateCursorContext(new CursorContextRequest("   ", "r1")).getStatusCode());
    }

    @Test
    @DisplayName("Should generate Cursor atomic multi-file patch and return 200 OK")
    void shouldGenerateCursorPatch() {
        CursorPatchRequest request = new CursorPatchRequest(
                "Refactor authentication to Oenexa token claims",
                List.of("src/AuthService.java", "src/SecurityConfig.java")
        );
        ResponseEntity<AtomicMultiFilePatch> response = controller.generateCursorPatch(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().fileDiffs().size());
        assertTrue(response.getBody().rollbackSafe());
    }

    @Test
    @DisplayName("Should reject invalid cursor patch request with 400 Bad Request")
    void shouldRejectInvalidCursorPatchRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.generateCursorPatch(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.generateCursorPatch(new CursorPatchRequest(null, List.of("file.java"))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.generateCursorPatch(new CursorPatchRequest("   ", List.of("file.java"))).getStatusCode());
    }

    @Test
    @DisplayName("Should execute Devin autonomous task and return 200 OK")
    void shouldExecuteDevinTask() {
        DevinTaskRequest request1 = new DevinTaskRequest("Fix race condition in OrderService", "repo-oenexa", true);
        ResponseEntity<DevinTaskExecutionPlan> response1 = controller.executeDevinTask(request1);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertNotNull(response1.getBody());
        assertTrue(response1.getBody().zeroMockVerificationPassed());
        assertTrue(response1.getBody().buildPassed());
        assertTrue(response1.getBody().finalSummary().contains("Zero-Mock Policy Enforced"));
        assertTrue(response1.getBody().finalSummary().contains("true"));

        DevinTaskRequest request2 = new DevinTaskRequest("Fix race condition in OrderService", "repo-oenexa", null);
        ResponseEntity<DevinTaskExecutionPlan> response2 = controller.executeDevinTask(request2);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertTrue(response2.getBody().zeroMockVerificationPassed());
        assertTrue(response2.getBody().finalSummary().contains("true"));

        DevinTaskRequest request3 = new DevinTaskRequest("Fix race condition in OrderService", "repo-oenexa", false);
        ResponseEntity<DevinTaskExecutionPlan> response3 = controller.executeDevinTask(request3);
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        assertTrue(response3.getBody().zeroMockVerificationPassed());
        assertTrue(response3.getBody().finalSummary().contains("false"));
    }

    @Test
    @DisplayName("Should reject invalid Devin task request with 400 Bad Request")
    void shouldRejectInvalidDevinTaskRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeDevinTask(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeDevinTask(new DevinTaskRequest(null, "r1", true)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeDevinTask(new DevinTaskRequest("   ", "r1", true)).getStatusCode());
    }

    @Test
    @DisplayName("Should generate GitHub Copilot inline completions and return 200 OK")
    void shouldGenerateCopilotCompletion() {
        CopilotCompletionRequest request = new CopilotCompletionRequest(
                "public record OrderStatus(",
                "OrderService.java"
        );
        ResponseEntity<CopilotCompletionResponse> response = controller.generateCopilotCompletion(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().suggestions().isEmpty());
    }

    @Test
    @DisplayName("Should reject invalid copilot completion request with 400 Bad Request")
    void shouldRejectInvalidCopilotRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.generateCopilotCompletion(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.generateCopilotCompletion(new CopilotCompletionRequest(null, "Test.java")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.generateCopilotCompletion(new CopilotCompletionRequest("   ", "Test.java")).getStatusCode());
    }

    @Test
    @DisplayName("Should execute OpenAI Operator workflow and return 200 OK")
    void shouldExecuteOperatorWorkflow() {
        OperatorWorkflowRequest request = new OperatorWorkflowRequest(
                "Deploy release candidate v2.4.0",
                List.of(OperatorActionType.DOCKER_BUILD, OperatorActionType.DB_MIGRATION, OperatorActionType.SAST_SECURITY_SCAN)
        );
        ResponseEntity<OperatorWorkflow> response = controller.executeOperatorWorkflow(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Deploy release candidate v2.4.0", response.getBody().objective());
        assertEquals(3, response.getBody().actions().size());
        assertEquals("COMPLETED", response.getBody().status());
    }

    @Test
    @DisplayName("Should reject invalid operator workflow request with 400 Bad Request")
    void shouldRejectInvalidOperatorRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeOperatorWorkflow(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeOperatorWorkflow(new OperatorWorkflowRequest(null, List.of(OperatorActionType.DOCKER_BUILD))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeOperatorWorkflow(new OperatorWorkflowRequest("   ", List.of(OperatorActionType.DOCKER_BUILD))).getStatusCode());
    }

    @Test
    @DisplayName("Should return capabilities overview with 200 OK")
    void shouldReturnCapabilitiesOverview() {
        ResponseEntity<TargetCapabilitiesOverview> response = controller.getCapabilitiesSummary();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().claudeReasoningActive());
        assertTrue(response.getBody().geminiMultimodalActive());
        assertTrue(response.getBody().cursorCodingActive());
        assertTrue(response.getBody().devinExecutionActive());
        assertTrue(response.getBody().copilotCompletionActive());
        assertTrue(response.getBody().operatorAutomationActive());
        assertEquals("2.6.0-OENEXA-MAX", response.getBody().platformVersion());
    }
}



