package com.oenexa.api;

import com.oenexa.application.TargetCapabilitiesService;
import com.oenexa.domain.ChatDomain.MultimodalAttachment;
import com.oenexa.domain.TargetCapabilitiesDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise REST Controller exposing the 6 Target Capabilities:
 * 1. Claude-Level Reasoning (Tree-of-Thought, Trade-offs, Reflection)
 * 2. Gemini-Level Multimodal Intelligence (Diagram Vision, Audio Directives, Context Fusion)
 * 3. Cursor-Level Coding (Codebase Symbol Cross-Referencing, Multi-File Atomic Patches, @context)
 * 4. Devin-Level Task Execution (End-to-End Autonomous Sandbox, Zero-Mock Verification)
 * 5. GitHub Copilot-Level Code Completion (Inline Ghost-Text, Method Signatures, Assertions)
 * 6. OpenAI Operator-Level Automation (Containerization, Deployments, DB Migrations, SAST Scans)
 */
@RestController
@RequestMapping("/api/v1/capabilities")
@CrossOrigin(origins = "*")
public class TargetCapabilitiesController {

    public record ClaudeReasoningRequest(String objective, String codeContext) {}
    public record MultimodalVisionRequest(String diagramName, String mimeType, String imageBase64) {}
    public record MultimodalAudioRequest(String audioFileName, String audioBase64) {}
    public record MultimodalFusionRequest(
            MultimodalAudioAnalysis audio,
            MultimodalVisionAnalysis vision,
            List<MultimodalAttachment> specDocuments,
            String contextualTitle
    ) {}
    public record CursorContextRequest(String promptOrQuery, String repositoryId) {}
    public record CursorPatchRequest(String objective, List<String> targetFilePaths) {}
    public record DevinTaskRequest(String taskGoal, String repositoryId, Boolean enforceZeroMockPolicy) {}
    public record OperatorWorkflowRequest(String workflowObjective, List<OperatorActionType> desiredActions) {}

    private final TargetCapabilitiesService targetCapabilitiesService;

    public TargetCapabilitiesController(TargetCapabilitiesService targetCapabilitiesService) {
        this.targetCapabilitiesService = Objects.requireNonNull(targetCapabilitiesService, "targetCapabilitiesService must not be null");
    }

    @PostMapping("/reasoning/claude")
    public ResponseEntity<ClaudeReasoningTrace> analyzeClaudeReasoning(@RequestBody ClaudeReasoningRequest request) {
        if (request == null || request.objective() == null || request.objective().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(targetCapabilitiesService.analyzeWithClaudeReasoning(request.objective(), request.codeContext()));
    }

    @PostMapping("/multimodal/vision")
    public ResponseEntity<MultimodalVisionAnalysis> analyzeVisionDiagram(@RequestBody MultimodalVisionRequest request) {
        if (request == null || request.diagramName() == null || request.diagramName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(targetCapabilitiesService.analyzeVisualDiagram(request.diagramName(), request.mimeType(), request.imageBase64()));
    }

    @PostMapping("/multimodal/audio")
    public ResponseEntity<MultimodalAudioAnalysis> transcribeAudioDirectives(@RequestBody MultimodalAudioRequest request) {
        if (request == null || request.audioFileName() == null || request.audioFileName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(targetCapabilitiesService.transcribeAndExtractDirectives(request.audioFileName(), request.audioBase64()));
    }

    @PostMapping("/multimodal/fusion")
    public ResponseEntity<MultimodalUnifiedContext> fuseMultimodalContext(@RequestBody MultimodalFusionRequest request) {
        if (request == null || request.audio() == null || request.vision() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(targetCapabilitiesService.fuseMultimodalContext(
                request.audio(),
                request.vision(),
                request.specDocuments(),
                request.contextualTitle()
        ));
    }

    @PostMapping("/coding/context")
    public ResponseEntity<List<CursorContextReference>> curateCursorContext(@RequestBody CursorContextRequest request) {
        if (request == null || request.promptOrQuery() == null || request.promptOrQuery().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(targetCapabilitiesService.curateCursorContext(request.promptOrQuery(), request.repositoryId()));
    }

    @PostMapping("/coding/patch")
    public ResponseEntity<AtomicMultiFilePatch> generateCursorPatch(@RequestBody CursorPatchRequest request) {
        if (request == null || request.objective() == null || request.objective().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(targetCapabilitiesService.generateCursorAtomicPatch(request.objective(), request.targetFilePaths()));
    }

    @PostMapping("/execution/devin")
    public ResponseEntity<DevinTaskExecutionPlan> executeDevinTask(@RequestBody DevinTaskRequest request) {
        if (request == null || request.taskGoal() == null || request.taskGoal().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        boolean enforce = request.enforceZeroMockPolicy() == null || request.enforceZeroMockPolicy();
        return ResponseEntity.ok(targetCapabilitiesService.executeDevinAutonomousTask(request.taskGoal(), request.repositoryId(), enforce));
    }

    @PostMapping("/copilot/complete")
    public ResponseEntity<CopilotCompletionResponse> generateCopilotCompletion(@RequestBody CopilotCompletionRequest request) {
        if (request == null || request.prefixCode() == null || request.prefixCode().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(targetCapabilitiesService.generateCopilotInlineCompletion(request));
    }

    @PostMapping("/operator/automate")
    public ResponseEntity<OperatorWorkflow> executeOperatorWorkflow(@RequestBody OperatorWorkflowRequest request) {
        if (request == null || request.workflowObjective() == null || request.workflowObjective().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(targetCapabilitiesService.executeOperatorWorkflow(request.workflowObjective(), request.desiredActions()));
    }

    @GetMapping("/summary")
    public ResponseEntity<TargetCapabilitiesOverview> getCapabilitiesSummary() {
        return ResponseEntity.ok(targetCapabilitiesService.getCapabilitiesOverview());
    }
}
