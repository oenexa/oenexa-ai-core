package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.voice.VoiceDtos.*;
import com.oenexa.api.mapper.VoiceApiMapper;
import com.oenexa.application.security.VoiceSecurityPolicy;
import com.oenexa.application.service.orchestration.EnterpriseVoiceService;
import com.oenexa.application.validation.VoiceValidator;
import com.oenexa.domain.model.orchestration.EnterpriseVoiceDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Enterprise Voice AI REST Controller (Phase 7).
 * Supports standard 11-Layer Clean Hexagonal Architecture:
 * Controller -> DTO -> Validation -> Security -> Mapper -> Service -> ApiResponse<T>.
 */
@RestController
@RequestMapping("/api/v1/voice")
@CrossOrigin(originPatterns = "*")
public class EnterpriseVoiceController {

    private final EnterpriseVoiceService voiceService;
    private final VoiceValidator validator;
    private final VoiceSecurityPolicy securityPolicy;
    private final VoiceApiMapper mapper;

    public EnterpriseVoiceController(EnterpriseVoiceService voiceService) {
        this(voiceService, new VoiceValidator(), new VoiceSecurityPolicy(), new VoiceApiMapper());
    }

    @Autowired
    public EnterpriseVoiceController(
            EnterpriseVoiceService voiceService,
            VoiceValidator validator,
            VoiceSecurityPolicy securityPolicy,
            VoiceApiMapper mapper
    ) {
        this.voiceService = Objects.requireNonNull(voiceService, "voiceService must not be null");
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.securityPolicy = Objects.requireNonNull(securityPolicy, "securityPolicy must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    @PostMapping("/transcribe")
    public ResponseEntity<TranscriptionResult> transcribe(@RequestBody TranscribeRequest req) {
        byte[] audioBytes = req.audioBase64() != null
                ? req.audioBase64().getBytes(StandardCharsets.UTF_8)
                : new byte[0];
        AudioFormat format = req.format() != null ? req.format() : AudioFormat.WAV;
        return ResponseEntity.ok(voiceService.transcribeAudio(audioBytes, req.filename(), format));
    }

    @PostMapping("/synthesize")
    public ResponseEntity<SpeechSynthesisResult> synthesize(@RequestBody SynthesizeRequest req) {
        VoicePersona persona = req.persona() != null ? req.persona() : VoicePersona.OENEXA_SOVEREIGN;
        AudioFormat format = req.format() != null ? req.format() : AudioFormat.WAV;
        return ResponseEntity.ok(voiceService.synthesizeSpeech(req.text(), persona, format));
    }

    @PostMapping("/command")
    public ResponseEntity<VoiceCommand> processCommand(@RequestBody CommandRequest req) {
        return ResponseEntity.ok(voiceService.processVoiceCommand(req.spokenText()));
    }

    @PostMapping("/meeting-analysis")
    public ResponseEntity<MeetingAudioAnalysis> analyzeMeeting(@RequestBody MeetingAnalysisRequest req) {
        byte[] audioBytes = req.audioBase64() != null
                ? req.audioBase64().getBytes(StandardCharsets.UTF_8)
                : new byte[0];
        return ResponseEntity.ok(voiceService.analyzeMeetingAudio(audioBytes, req.meetingTitle()));
    }

    // ==========================================
    // 11-Layer Clean Hexagonal Architecture v2 Endpoints
    // ==========================================

    @PostMapping("/v2/transcribe")
    public ResponseEntity<ApiResponse<VoiceTranscriptionResultDto>> transcribeV2(@RequestBody VoiceTranscribeRequestDto req) {
        validator.validateTranscription(req.audioBase64(), req.filename());
        byte[] audioBytes = req.audioBase64().getBytes(StandardCharsets.UTF_8);
        securityPolicy.verifyAudioPayload(audioBytes);

        AudioFormat format = req.format() != null ? AudioFormat.valueOf(req.format().toUpperCase()) : AudioFormat.WAV;
        TranscriptionResult result = voiceService.transcribeAudio(audioBytes, req.filename(), format);

        return ResponseEntity.ok(ApiResponse.ok("Audio transcribed successfully", mapper.toTranscriptionResultDto(result)));
    }

    @PostMapping("/v2/synthesize")
    public ResponseEntity<ApiResponse<VoiceSynthesisResultDto>> synthesizeV2(@RequestBody VoiceSynthesizeRequestDto req) {
        validator.validateSynthesis(req.text());
        VoicePersona persona = req.persona() != null ? VoicePersona.valueOf(req.persona().toUpperCase()) : VoicePersona.OENEXA_SOVEREIGN;
        AudioFormat format = req.format() != null ? AudioFormat.valueOf(req.format().toUpperCase()) : AudioFormat.WAV;

        SpeechSynthesisResult result = voiceService.synthesizeSpeech(req.text(), persona, format);
        return ResponseEntity.ok(ApiResponse.ok("Speech synthesized successfully", mapper.toSynthesisResultDto(result)));
    }

    @PostMapping("/v2/command")
    public ResponseEntity<ApiResponse<VoiceCommandResultDto>> processCommandV2(@RequestBody VoiceCommandRequestDto req) {
        validator.validateCommand(req.spokenText());
        VoiceCommand cmd = voiceService.processVoiceCommand(req.spokenText());
        return ResponseEntity.ok(ApiResponse.ok("Voice command processed successfully", mapper.toCommandResultDto(cmd)));
    }

    public record TranscribeRequest(String audioBase64, String filename, AudioFormat format) {}
    public record SynthesizeRequest(String text, VoicePersona persona, AudioFormat format) {}
    public record CommandRequest(String spokenText) {}
    public record MeetingAnalysisRequest(String audioBase64, String meetingTitle) {}
}
