package com.oenexa.api;

import com.oenexa.application.EnterpriseVoiceService;
import com.oenexa.domain.EnterpriseVoiceDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseVoiceController Zero-Mock Unit Tests")
class EnterpriseVoiceControllerTest {

    private EnterpriseVoiceController controller;
    private EnterpriseVoiceService service;

    @BeforeEach
    void setUp() {
        service = new EnterpriseVoiceService();
        controller = new EnterpriseVoiceController(service);
    }

    @Test
    @DisplayName("Should transcribe audio through controller")
    void shouldTranscribeAudio() {
        var req = new EnterpriseVoiceController.TranscribeRequest("base64audio", "clip.wav", AudioFormat.WAV);
        ResponseEntity<TranscriptionResult> resp = controller.transcribe(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().text());
    }

    @Test
    @DisplayName("Should synthesize speech through controller")
    void shouldSynthesizeSpeech() {
        var req = new EnterpriseVoiceController.SynthesizeRequest("Hello", VoicePersona.SHIMMER, AudioFormat.MP3);
        ResponseEntity<SpeechSynthesisResult> resp = controller.synthesize(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(VoicePersona.SHIMMER, resp.getBody().persona());
    }

    @Test
    @DisplayName("Should process voice commands through controller")
    void shouldProcessCommand() {
        var req = new EnterpriseVoiceController.CommandRequest("Run tests for domain layer");
        ResponseEntity<VoiceCommand> resp = controller.processCommand(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("RUN_TESTS", resp.getBody().commandType());
    }

    @Test
    @DisplayName("Should perform meeting analysis through controller")
    void shouldAnalyzeMeeting() {
        var req = new EnterpriseVoiceController.MeetingAnalysisRequest("audiodata", "Sprint Retrospective");
        ResponseEntity<MeetingAudioAnalysis> resp = controller.analyzeMeeting(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Sprint Retrospective", resp.getBody().meetingTitle());
    }
}
