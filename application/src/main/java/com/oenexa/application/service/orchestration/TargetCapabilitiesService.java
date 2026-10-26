package com.oenexa.application.service.orchestration;

import com.oenexa.application.engine.*;
import com.oenexa.domain.model.chat.ChatDomain.MultimodalAttachment;
import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise GoF Facade orchestrating OENEXA's 6 sovereign intelligence engines:
 * 1. OenexaDeepReasoningEngine: Tree-of-Thought (ToT) exploration, Trade-off analysis, Architectural reflection
 * 2. OenexaMultimodalEngine: Speech audio directives, Architecture diagram OCR & vision, Spec fusion
 * 3. OenexaCodeSynthesisEngine: Codebase-wide symbol cross-referencing, atomic multi-file patching, @context curation
 * 4. OenexaAutonomousExecutionEngine: Autonomous goal decomposition, sandbox compilation & zero-mock self-healing loops
 * 5. OenexaIntelligentCompletionEngine: Inline ghost-text prediction, method signature & test assertion synthesis
 * 6. OperatorWorkflowEngine: Multi-step environment workflows (Docker, K8s, Flyway, SAST scans)
 */
@Service
public class TargetCapabilitiesService {

    private final OenexaDeepReasoningEngine deepReasoningEngine;
    private final OenexaMultimodalEngine multimodalEngine;
    private final OenexaCodeSynthesisEngine codeSynthesisEngine;
    private final OenexaAutonomousExecutionEngine autonomousExecutionEngine;
    private final OenexaIntelligentCompletionEngine intelligentCompletionEngine;
    private final OperatorWorkflowEngine operatorWorkflowEngine;

    private final ClaudeReasoningEngine claudeReasoningEngine;
    private final CursorCodingEngine cursorCodingEngine;
    private final DevinExecutionEngine devinExecutionEngine;
    private final CopilotCompletionEngine copilotCompletionEngine;

    public TargetCapabilitiesService(
            OenexaDeepReasoningEngine deepReasoningEngine,
            OenexaMultimodalEngine multimodalEngine,
            OenexaCodeSynthesisEngine codeSynthesisEngine,
            OenexaAutonomousExecutionEngine autonomousExecutionEngine,
            OenexaIntelligentCompletionEngine intelligentCompletionEngine,
            OperatorWorkflowEngine operatorWorkflowEngine
    ) {
        this.deepReasoningEngine = Objects.requireNonNull(deepReasoningEngine, "deepReasoningEngine must not be null");
        this.multimodalEngine = Objects.requireNonNull(multimodalEngine, "multimodalEngine must not be null");
        this.codeSynthesisEngine = Objects.requireNonNull(codeSynthesisEngine, "codeSynthesisEngine must not be null");
        this.autonomousExecutionEngine = Objects.requireNonNull(autonomousExecutionEngine, "autonomousExecutionEngine must not be null");
        this.intelligentCompletionEngine = Objects.requireNonNull(intelligentCompletionEngine, "intelligentCompletionEngine must not be null");
        this.operatorWorkflowEngine = Objects.requireNonNull(operatorWorkflowEngine, "operatorWorkflowEngine must not be null");

        this.claudeReasoningEngine = new ClaudeReasoningEngine(deepReasoningEngine);
        this.cursorCodingEngine = new CursorCodingEngine(codeSynthesisEngine);
        this.devinExecutionEngine = new DevinExecutionEngine(this.cursorCodingEngine);
        this.copilotCompletionEngine = new CopilotCompletionEngine(intelligentCompletionEngine);
    }

    public TargetCapabilitiesService(
            ClaudeReasoningEngine claudeReasoningEngine,
            OenexaMultimodalEngine oenexaMultimodalEngine,
            CursorCodingEngine cursorCodingEngine,
            DevinExecutionEngine devinExecutionEngine,
            CopilotCompletionEngine copilotCompletionEngine,
            OperatorWorkflowEngine operatorWorkflowEngine
    ) {
        this.claudeReasoningEngine = Objects.requireNonNull(claudeReasoningEngine, "claudeReasoningEngine must not be null");
        this.multimodalEngine = Objects.requireNonNull(oenexaMultimodalEngine, "oenexaMultimodalEngine must not be null");
        this.cursorCodingEngine = Objects.requireNonNull(cursorCodingEngine, "cursorCodingEngine must not be null");
        this.devinExecutionEngine = Objects.requireNonNull(devinExecutionEngine, "devinExecutionEngine must not be null");
        this.copilotCompletionEngine = Objects.requireNonNull(copilotCompletionEngine, "copilotCompletionEngine must not be null");
        this.operatorWorkflowEngine = Objects.requireNonNull(operatorWorkflowEngine, "operatorWorkflowEngine must not be null");

        this.deepReasoningEngine = new OenexaDeepReasoningEngine();
        this.codeSynthesisEngine = new OenexaCodeSynthesisEngine();
        this.autonomousExecutionEngine = new OenexaAutonomousExecutionEngine(this.codeSynthesisEngine);
        this.intelligentCompletionEngine = new OenexaIntelligentCompletionEngine();
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

    // --- 1. Deep Reasoning ---
    public DeepReasoningTrace analyzeWithDeepReasoning(String objective, String codeContext) {
        return deepReasoningEngine.analyzeWithDeepReasoning(objective, codeContext);
    }

    public ClaudeReasoningTrace analyzeWithClaudeReasoning(String objective, String codeContext) {
        return deepReasoningEngine.analyzeWithClaudeReasoning(objective, codeContext);
    }

    // --- 2. Multimodal Vision & Audio ---
    public MultimodalVisionAnalysis analyzeVisualDiagram(String diagramName, String mimeType, String imageBase64) {
        return multimodalEngine.analyzeVisualDiagram(diagramName, mimeType, imageBase64);
    }

    public MultimodalAudioAnalysis transcribeAndExtractDirectives(String audioFileName, String audioBase64) {
        return multimodalEngine.transcribeAndExtractDirectives(audioFileName, audioBase64);
    }

    public MultimodalUnifiedContext fuseMultimodalContext(
            MultimodalAudioAnalysis audio,
            MultimodalVisionAnalysis vision,
            List<MultimodalAttachment> docs,
            String title
    ) {
        return multimodalEngine.fuseMultimodalContext(audio, vision, docs, title);
    }

    // --- 3. Code Synthesis ---
    public List<CodeSynthesisContextReference> curateCodeSynthesisContext(String query, String repositoryId) {
        return codeSynthesisEngine.curateContext(query, repositoryId);
    }

    public List<CursorContextReference> curateCursorContext(String query, String repositoryId) {
        return codeSynthesisEngine.curateCursorContext(query, repositoryId);
    }

    public AtomicMultiFilePatch generateAtomicPatch(String objective, List<String> filePaths) {
        return codeSynthesisEngine.generateAtomicPatch(objective, filePaths);
    }

    public AtomicMultiFilePatch generateCursorAtomicPatch(String objective, List<String> filePaths) {
        return codeSynthesisEngine.generateCursorAtomicPatch(objective, filePaths);
    }

    // --- 4. Autonomous Task Execution ---
    public AutonomousTaskExecutionPlan executeAutonomousTask(String goal, String repositoryId, boolean enforceZeroMock) {
        return autonomousExecutionEngine.executeAutonomousTask(goal, repositoryId, enforceZeroMock);
    }

    public DevinTaskExecutionPlan executeDevinAutonomousTask(String goal, String repositoryId, boolean enforceZeroMock) {
        return autonomousExecutionEngine.executeDevinAutonomousTask(goal, repositoryId, enforceZeroMock);
    }

    // --- 5. Intelligent Completion ---
    public IntelligentCompletionResponse generateInlineCompletion(IntelligentCompletionRequest request) {
        return intelligentCompletionEngine.generateInlineCompletion(request);
    }

    public CopilotCompletionResponse generateCopilotInlineCompletion(CopilotCompletionRequest request) {
        return intelligentCompletionEngine.generateCopilotInlineCompletion(request);
    }

    // --- 6. Operator Workflows ---
    public OperatorWorkflow executeOperatorWorkflow(String objective, List<OperatorActionType> desiredActions) {
        return operatorWorkflowEngine.executeOperatorWorkflow(objective, desiredActions);
    }

    public TargetCapabilitiesOverview getCapabilitiesOverview() {
        return new TargetCapabilitiesOverview();
    }

    // --- Engine Accessors ---
    public OenexaDeepReasoningEngine getDeepReasoningEngine() {
        return deepReasoningEngine;
    }

    public ClaudeReasoningEngine getClaudeReasoningEngine() {
        return claudeReasoningEngine;
    }

    public OenexaMultimodalEngine getOenexaMultimodalEngine() {
        return multimodalEngine;
    }

    public OenexaCodeSynthesisEngine getCodeSynthesisEngine() {
        return codeSynthesisEngine;
    }

    public CursorCodingEngine getCursorCodingEngine() {
        return cursorCodingEngine;
    }

    public OenexaAutonomousExecutionEngine getAutonomousExecutionEngine() {
        return autonomousExecutionEngine;
    }

    public DevinExecutionEngine getDevinExecutionEngine() {
        return devinExecutionEngine;
    }

    public OenexaIntelligentCompletionEngine getIntelligentCompletionEngine() {
        return intelligentCompletionEngine;
    }

    public CopilotCompletionEngine getCopilotCompletionEngine() {
        return copilotCompletionEngine;
    }

    public OperatorWorkflowEngine getOperatorWorkflowEngine() {
        return operatorWorkflowEngine;
    }
}
