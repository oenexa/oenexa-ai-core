package com.oenexa.application.service.orchestration;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.application.engine.*;
import com.oenexa.domain.model.chat.ChatDomain.MultimodalAttachment;
import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise GoF Facade orchestrating the 6 Target Capabilities engines:
 * 1. ClaudeReasoningEngine: Deep Tree-of-Thought (ToT) exploration, Trade-off analysis, Reflection
 * 2. OenexaMultimodalEngine: Speech audio directives, Architecture diagram OCR & vision, Spec fusion
 * 3. CursorCodingEngine: Codebase-wide symbol cross-referencing, atomic multi-file patching, @context curation
 * 4. DevinExecutionEngine: Autonomous goal decomposition, sandbox compilation & zero-mock self-healing loops
 * 5. CopilotCompletionEngine: Inline ghost-text prediction, method signature & test assertion synthesis
 * 6. OperatorWorkflowEngine: Multi-step environment workflows (Docker, K8s, Flyway, SAST scans)
 */
@Service
public class TargetCapabilitiesService {

    private final ClaudeReasoningEngine claudeReasoningEngine;
    private final OenexaMultimodalEngine oenexaMultimodalEngine;
    private final CursorCodingEngine cursorCodingEngine;
    private final DevinExecutionEngine devinExecutionEngine;
    private final CopilotCompletionEngine copilotCompletionEngine;
    private final OperatorWorkflowEngine operatorWorkflowEngine;

    public TargetCapabilitiesService(
            ClaudeReasoningEngine claudeReasoningEngine,
            OenexaMultimodalEngine oenexaMultimodalEngine,
            CursorCodingEngine cursorCodingEngine,
            DevinExecutionEngine devinExecutionEngine,
            CopilotCompletionEngine copilotCompletionEngine,
            OperatorWorkflowEngine operatorWorkflowEngine
    ) {
        this.claudeReasoningEngine = Objects.requireNonNull(claudeReasoningEngine, "claudeReasoningEngine must not be null");
        this.oenexaMultimodalEngine = Objects.requireNonNull(oenexaMultimodalEngine, "oenexaMultimodalEngine must not be null");
        this.cursorCodingEngine = Objects.requireNonNull(cursorCodingEngine, "cursorCodingEngine must not be null");
        this.devinExecutionEngine = Objects.requireNonNull(devinExecutionEngine, "devinExecutionEngine must not be null");
        this.copilotCompletionEngine = Objects.requireNonNull(copilotCompletionEngine, "copilotCompletionEngine must not be null");
        this.operatorWorkflowEngine = Objects.requireNonNull(operatorWorkflowEngine, "operatorWorkflowEngine must not be null");
    }

    public TargetCapabilitiesService() {
        this(
                new ClaudeReasoningEngine(),
                new OenexaMultimodalEngine(),
                new CursorCodingEngine(),
                new DevinExecutionEngine(),
                new CopilotCompletionEngine(),
                new OperatorWorkflowEngine()
        );
    }

    public ClaudeReasoningTrace analyzeWithClaudeReasoning(String objective, String codeContext) {
        return claudeReasoningEngine.analyzeWithClaudeReasoning(objective, codeContext);
    }

    public MultimodalVisionAnalysis analyzeVisualDiagram(String diagramName, String mimeType, String imageBase64) {
        return oenexaMultimodalEngine.analyzeVisualDiagram(diagramName, mimeType, imageBase64);
    }

    public MultimodalAudioAnalysis transcribeAndExtractDirectives(String audioFileName, String audioBase64) {
        return oenexaMultimodalEngine.transcribeAndExtractDirectives(audioFileName, audioBase64);
    }

    public MultimodalUnifiedContext fuseMultimodalContext(
            MultimodalAudioAnalysis audio,
            MultimodalVisionAnalysis vision,
            List<MultimodalAttachment> docs,
            String title
    ) {
        return oenexaMultimodalEngine.fuseMultimodalContext(audio, vision, docs, title);
    }

    public List<CursorContextReference> curateCursorContext(String query, String repositoryId) {
        return cursorCodingEngine.curateCursorContext(query, repositoryId);
    }

    public AtomicMultiFilePatch generateCursorAtomicPatch(String objective, List<String> filePaths) {
        return cursorCodingEngine.generateCursorAtomicPatch(objective, filePaths);
    }

    public DevinTaskExecutionPlan executeDevinAutonomousTask(String goal, String repositoryId, boolean enforceZeroMock) {
        return devinExecutionEngine.executeDevinAutonomousTask(goal, repositoryId, enforceZeroMock);
    }

    public CopilotCompletionResponse generateCopilotInlineCompletion(CopilotCompletionRequest request) {
        return copilotCompletionEngine.generateCopilotInlineCompletion(request);
    }

    public OperatorWorkflow executeOperatorWorkflow(String objective, List<OperatorActionType> desiredActions) {
        return operatorWorkflowEngine.executeOperatorWorkflow(objective, desiredActions);
    }

    public TargetCapabilitiesOverview getCapabilitiesOverview() {
        return new TargetCapabilitiesOverview();
    }

    public ClaudeReasoningEngine getClaudeReasoningEngine() {
        return claudeReasoningEngine;
    }

    public OenexaMultimodalEngine getOenexaMultimodalEngine() {
        return oenexaMultimodalEngine;
    }

    public CursorCodingEngine getCursorCodingEngine() {
        return cursorCodingEngine;
    }

    public DevinExecutionEngine getDevinExecutionEngine() {
        return devinExecutionEngine;
    }

    public CopilotCompletionEngine getCopilotCompletionEngine() {
        return copilotCompletionEngine;
    }

    public OperatorWorkflowEngine getOperatorWorkflowEngine() {
        return operatorWorkflowEngine;
    }
}


