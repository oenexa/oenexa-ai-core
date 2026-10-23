package com.oenexa.api;

import com.oenexa.api.controller.*;
import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.autonomous.AutonomousIssueDtos.*;
import com.oenexa.api.dto.billing.BillingDtos.*;
import com.oenexa.api.dto.core.CoreBrainDtos.*;
import com.oenexa.api.dto.mcp.McpDtos.*;
import com.oenexa.api.dto.media.MediaDtos.*;
import com.oenexa.api.dto.multimodel.MultiModelDtos.*;
import com.oenexa.api.dto.voice.VoiceDtos.*;
import com.oenexa.api.mapper.*;
import com.oenexa.application.core.*;
import com.oenexa.application.cognitive.*;
import com.oenexa.application.security.*;
import com.oenexa.application.service.agent.CollaborativeAgentService;
import com.oenexa.application.service.code.CodeEngineeringService;
import com.oenexa.application.service.knowledge.EnterpriseKnowledgeService;
import com.oenexa.application.service.memory.EnterpriseMemoryHierarchyService;
import com.oenexa.application.service.orchestration.*;
import com.oenexa.application.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 11-Layer Clean Hexagonal Architecture Integration Tests for Extended Ecosystem Modules.
 * Validates request flow: Controller -> DTO -> Validation -> Security -> Mapper -> Service -> ApiResponse<T>.
 * 100% Zero-Mock Standard compliant.
 */
@DisplayName("11-Layer Clean Architecture Extended Modules Integration Tests (Zero-Mock)")
class CleanArchitectureModulesOrchestrationIntegrationTest {

    private MultiModelController multiModelController;
    private EnterpriseBillingController billingController;
    private EnterpriseVoiceController voiceController;
    private EnterpriseMediaAiController mediaController;
    private ModelContextProtocolController mcpController;
    private OenexaCoreBrainController coreBrainController;
    private OenexaAutonomousIssueHandlingController autonomousIssueController;

    @BeforeEach
    void setUp() {
        multiModelController = new MultiModelController(
                new EnterpriseMultiModelOrchestrator(),
                new MultiModelValidator(),
                new MultiModelSecurityPolicy(),
                new MultiModelApiMapper()
        );

        billingController = new EnterpriseBillingController(
                new EnterpriseBillingService(),
                new BillingValidator(),
                new BillingSecurityPolicy(),
                new BillingApiMapper()
        );

        voiceController = new EnterpriseVoiceController(
                new EnterpriseVoiceService(),
                new VoiceValidator(),
                new VoiceSecurityPolicy(),
                new VoiceApiMapper()
        );

        mediaController = new EnterpriseMediaAiController(
                new EnterpriseMediaAiService(),
                new MediaValidator(),
                new MediaSecurityPolicy(),
                new MediaApiMapper()
        );

        mcpController = new ModelContextProtocolController(
                new ModelContextProtocolService(),
                new McpValidator(),
                new McpSecurityPolicy(),
                new McpApiMapper()
        );

        OenexaSecurityEngine securityEngine = new OenexaSecurityEngine();
        OenexaAiGatewayEngine gatewayEngine = new OenexaAiGatewayEngine(securityEngine);
        OenexaObservabilityEngine obsEngine = new OenexaObservabilityEngine();
        OenexaCoreBrain brain = new OenexaCoreBrain();

        coreBrainController = new OenexaCoreBrainController(
                brain,
                gatewayEngine,
                securityEngine,
                obsEngine,
                new CoreBrainValidator(),
                new CoreBrainSecurityPolicy(),
                new CoreBrainApiMapper()
        );

        autonomousIssueController = new OenexaAutonomousIssueHandlingController(
                new OenexaAutonomousLifecycleOrchestrator(),
                new OenexaAutonomousProblemSolverEngine(),
                new AutonomousIssueValidator(),
                new AutonomousIssueSecurityPolicy(),
                new AutonomousIssueApiMapper()
        );
    }

    @Test
    @DisplayName("Layer 1-11 Flow: MultiModel dynamic routing and smart fallback execution")
    void shouldExecuteMultiModelThroughAll11Layers() {
        ModelRouteRequestDto routeReq = new ModelRouteRequestDto("Optimize quantum graph search algorithm", "REASONING", "QUALITY", "gemini-2.5-pro");
        ResponseEntity<ApiResponse<ModelRoutingResultDto>> routeResp = multiModelController.routeModelV2(routeReq);

        assertEquals(HttpStatus.OK, routeResp.getStatusCode());
        assertNotNull(routeResp.getBody());
        assertTrue(routeResp.getBody().success());
        assertNotNull(routeResp.getBody().data().selectedModel());

        FallbackExecutionRequestDto fallbackReq = new FallbackExecutionRequestDto("conv-mm-1", "Generate high-throughput ingestion pipeline", "AUTO", null, true, false);
        ResponseEntity<ApiResponse<FallbackExecutionResultDto>> fallbackResp = multiModelController.executeWithFallbackV2(fallbackReq);

        assertEquals(HttpStatus.OK, fallbackResp.getStatusCode());
        assertTrue(fallbackResp.getBody().data().fallbackTriggered());
    }

    @Test
    @DisplayName("Layer 1-11 Flow: Billing subscription upgrade and quota management")
    void shouldExecuteBillingThroughAll11Layers() {
        SubscriptionUpgradeRequestDto upReq = new SubscriptionUpgradeRequestDto("enterprise-user-99", "ENTERPRISE", "ANNUAL");
        ResponseEntity<ApiResponse<SubscriptionStatusDto>> upResp = billingController.upgradeSubscriptionV2("enterprise-user-99", "ROLE_USER", upReq);

        assertEquals(HttpStatus.OK, upResp.getStatusCode());
        assertNotNull(upResp.getBody());
        assertTrue(upResp.getBody().success());
        assertEquals("ENTERPRISE", upResp.getBody().data().tier());

        ResponseEntity<ApiResponse<QuotaUsageDto>> quotaResp = billingController.getQuotaV2("enterprise-user-99", "enterprise-user-99", "ROLE_USER");
        assertEquals(HttpStatus.OK, quotaResp.getStatusCode());
        assertTrue(quotaResp.getBody().data().tokenLimit() > 0);
    }

    @Test
    @DisplayName("Layer 1-11 Flow: Voice STT transcription, TTS synthesis, and commands")
    void shouldExecuteVoiceThroughAll11Layers() {
        VoiceTranscribeRequestDto transReq = new VoiceTranscribeRequestDto("UklGRi4AAABXQVZFZmt0", "meeting-recording.wav", "WAV");
        ResponseEntity<ApiResponse<VoiceTranscriptionResultDto>> transResp = voiceController.transcribeV2(transReq);

        assertEquals(HttpStatus.OK, transResp.getStatusCode());
        assertTrue(transResp.getBody().success());

        VoiceSynthesizeRequestDto synthReq = new VoiceSynthesizeRequestDto("OENEXA-AI Sovereign Operating System is ready.", "OENEXA_SOVEREIGN", "WAV");
        ResponseEntity<ApiResponse<VoiceSynthesisResultDto>> synthResp = voiceController.synthesizeV2(synthReq);

        assertEquals(HttpStatus.OK, synthResp.getStatusCode());
        assertNotNull(synthResp.getBody().data().audioBase64());

        VoiceCommandRequestDto cmdReq = new VoiceCommandRequestDto("deploy current sprint release to production");
        ResponseEntity<ApiResponse<VoiceCommandResultDto>> cmdResp = voiceController.processCommandV2(cmdReq);

        assertEquals(HttpStatus.OK, cmdResp.getStatusCode());
        assertNotNull(cmdResp.getBody().data().intent());
    }

    @Test
    @DisplayName("Layer 1-11 Flow: Media AI generative images and video summarization")
    void shouldExecuteMediaThroughAll11Layers() {
        ImageGenerationRequestDto imgReq = new ImageGenerationRequestDto("Cyberpunk sovereign AI core architecture diagram", "1024x1024", "CINEMATIC");
        ResponseEntity<ApiResponse<ImageProcessingResultDto>> imgResp = mediaController.generateImageV2(imgReq);

        assertEquals(HttpStatus.OK, imgResp.getStatusCode());
        assertTrue(imgResp.getBody().success());

        VideoSummarizationRequestDto vidReq = new VideoSummarizationRequestDto("AAAAHGZ0eXBtcDQy", "Architecture Walkthrough");
        ResponseEntity<ApiResponse<VideoProcessingResultDto>> vidResp = mediaController.summarizeVideoV2(vidReq);

        assertEquals(HttpStatus.OK, vidResp.getStatusCode());
        assertNotNull(vidResp.getBody().data().summary());
    }

    @Test
    @DisplayName("Layer 1-11 Flow: Model Context Protocol (MCP) tool execution and JSON-RPC")
    void shouldExecuteMcpThroughAll11Layers() {
        McpToolExecutionRequestDto toolReq = new McpToolExecutionRequestDto("oenexa_ast_refactor", Map.of("targetFile", "JpaUserStorageAdapter.java"));
        ResponseEntity<ApiResponse<McpToolExecutionResultDto>> toolResp = mcpController.executeToolV2("oenexa_ast_refactor", toolReq, "ROLE_DEVELOPER");

        assertEquals(HttpStatus.OK, toolResp.getStatusCode());
        assertTrue(toolResp.getBody().data().success());

        McpRpcRequestDto rpcReq = new McpRpcRequestDto("2.0", "tools/list", Map.of(), "req-42");
        ResponseEntity<ApiResponse<McpRpcResponseDto>> rpcResp = mcpController.handleRpcV2(rpcReq);

        assertEquals(HttpStatus.OK, rpcResp.getStatusCode());
        assertEquals("2.0", rpcResp.getBody().data().jsonrpc());
    }

    @Test
    @DisplayName("Layer 1-11 Flow: OENEXA Core Brain directive dispatch and synthesis")
    void shouldExecuteCoreBrainThroughAll11Layers() {
        CoreBrainDirectiveRequestDto brainReq = new CoreBrainDirectiveRequestDto("dir-101", "Execute architectural self-audit across microservices", "NORMAL", Map.of("scope", "core"));
        ResponseEntity<ApiResponse<CoreBrainDirectiveResultDto>> brainResp = coreBrainController.executeDirectiveV2(brainReq, "ROLE_ADMIN");

        assertEquals(HttpStatus.OK, brainResp.getStatusCode());
        assertTrue(brainResp.getBody().success());
        assertEquals("dir-101", brainResp.getBody().data().executionId());
    }

    @Test
    @DisplayName("Layer 1-11 Flow: Autonomous Issue detection, resolution, and self-healing")
    void shouldExecuteAutonomousIssueThroughAll11Layers() {
        IssueDetectionRequestDto issueReq = new IssueDetectionRequestDto("java.lang.NullPointerException in PaymentGateway", "BillingService", "STAGING", "HIGH");
        ResponseEntity<ApiResponse<IssueResolutionResultDto>> issueResp = autonomousIssueController.detectAndResolveV2(issueReq);

        assertEquals(HttpStatus.OK, issueResp.getStatusCode());
        assertTrue(issueResp.getBody().success());
        assertNotNull(issueResp.getBody().data().resolutionPlan());

        SelfHealingTriggerRequestDto healReq = new SelfHealingTriggerRequestDto("cycle-900", "DEADLOCK_RECOVERY", "ConnectionPool");
        ResponseEntity<ApiResponse<SelfHealingStatusDto>> healResp = autonomousIssueController.triggerSelfHealingV2(healReq, "DEVELOPMENT", "ROLE_USER");

        assertEquals(HttpStatus.OK, healResp.getStatusCode());
        assertTrue(healResp.getBody().data().restored());
    }
}
