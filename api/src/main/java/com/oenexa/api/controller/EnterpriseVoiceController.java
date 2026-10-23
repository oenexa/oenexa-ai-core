package com.oenexa.api.controller;

import com.oenexa.application.service.orchestration.EnterpriseVoiceService;
import com.oenexa.domain.model.orchestration.EnterpriseVoiceDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/**
 * Enterprise Voice AI REST Controller (Phase 7).
 * Exposes Speech-to-Text (STT), Text-to-Speech (TTS), voice commands, and meeting analysis.
 */
@RestController
@RequestMapping("/api/v1/voice")
@CrossOrigin(origins = "*")
public class EnterpriseVoiceController {

    private final EnterpriseVoiceService voiceService;

    public EnterpriseVoiceController(EnterpriseVoiceService voiceService) {
        this.voiceService = voiceService;
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

    public record TranscribeRequest(String audioBase64, String filename, AudioFormat format) {}
    public record SynthesizeRequest(String text, VoicePersona persona, AudioFormat format) {}
    public record CommandRequest(String spokenText) {}
    public record MeetingAnalysisRequest(String audioBase64, String meetingTitle) {}
}


