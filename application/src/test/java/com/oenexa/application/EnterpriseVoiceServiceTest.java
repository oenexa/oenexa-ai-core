package com.oenexa.application;

import com.oenexa.domain.EnterpriseVoiceDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseVoiceService Zero-Mock Unit Tests")
class EnterpriseVoiceServiceTest {

    private EnterpriseVoiceService service;

    @BeforeEach
    void setUp() {
        service = new EnterpriseVoiceService();
    }

    @Test
    @DisplayName("Should transcribe audio into text and word timestamps")
    void shouldTranscribeAudio() {
        byte[] sampleAudio = "audio header data".getBytes(StandardCharsets.UTF_8);
        TranscriptionResult result = service.transcribeAudio(sampleAudio, "voice_sample.wav", AudioFormat.WAV);

        assertNotNull(result);
        assertNotNull(result.text());
        assertFalse(result.words().isEmpty());
        assertTrue(result.durationMs() > 0);
        assertTrue(result.confidence() > 0.9);
    }

    @Test
    @DisplayName("Should synthesize text into speech audio data URI")
    void shouldSynthesizeSpeech() {
        SpeechSynthesisResult tts = service.synthesizeSpeech(
                "Welcome to OENEXA AI",
                VoicePersona.OENEXA_SOVEREIGN,
                AudioFormat.WAV
        );

        assertNotNull(tts);
        assertTrue(tts.audioDataUri().startsWith("data:audio/wav;base64,"));
        assertEquals(VoicePersona.OENEXA_SOVEREIGN, tts.persona());
        assertTrue(tts.sizeBytes() > 0);
        assertTrue(tts.generationLatencyMs() > 0);
    }

    @Test
    @DisplayName("Should parse and route voice commands accurately")
    void shouldProcessVoiceCommands() {
        VoiceCommand cmdTest = service.processVoiceCommand("Run zero-mock gradle tests");
        assertEquals("RUN_TESTS", cmdTest.commandType());
        assertTrue(cmdTest.executed());

        VoiceCommand cmdDeploy = service.processVoiceCommand("Deploy docker containers to k8s");
        assertEquals("DEPLOY_SERVICE", cmdDeploy.commandType());

        VoiceCommand cmdSearch = service.processVoiceCommand("Search knowledge for authentication");
        assertEquals("KNOWLEDGE_SEARCH", cmdSearch.commandType());

        VoiceCommand cmdAgent = service.processVoiceCommand("Plan autonomous agent goal for backend");
        assertEquals("PLAN_AGENT_GOAL", cmdAgent.commandType());
    }

    @Test
    @DisplayName("Should analyze meeting audio with speaker diarization and action items")
    void shouldAnalyzeMeetingAudio() {
        MeetingAudioAnalysis analysis = service.analyzeMeetingAudio(new byte[100], "Quarterly AI Alignment");

        assertNotNull(analysis);
        assertEquals("Quarterly AI Alignment", analysis.meetingTitle());
        assertEquals(4, analysis.segments().size());
        assertFalse(analysis.actionItems().isEmpty());
        assertFalse(analysis.keyDecisions().isEmpty());
        assertTrue(analysis.summary().contains("OENEXA"));
    }
}
