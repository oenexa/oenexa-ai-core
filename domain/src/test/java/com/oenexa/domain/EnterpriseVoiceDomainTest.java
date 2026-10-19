package com.oenexa.domain;

import com.oenexa.domain.EnterpriseVoiceDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseVoiceDomain Invariant Tests (Zero-Mock)")
class EnterpriseVoiceDomainTest {

    @Test
    @DisplayName("Should create TranscriptionResult with word timestamps")
    void shouldCreateTranscriptionResult() {
        WordTimestamp w1 = new WordTimestamp("Hello", 0.0, 0.4);
        WordTimestamp w2 = new WordTimestamp("Oenexa", 0.5, 1.1);

        TranscriptionResult result = new TranscriptionResult(
                "Hello Oenexa",
                "en",
                0.98,
                List.of(w1, w2),
                1100L
        );

        assertNotNull(result);
        assertEquals("Hello Oenexa", result.text());
        assertEquals(2, result.words().size());
        assertEquals(0.98, result.confidence());
    }

    @Test
    @DisplayName("Should validate timestamp bounds in WordTimestamp")
    void shouldEnforceWordTimestampBounds() {
        assertThrows(NullPointerException.class, () -> new WordTimestamp(null, 0.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new WordTimestamp("test", -1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new WordTimestamp("test", 2.0, 1.0));
    }

    @Test
    @DisplayName("Should create SpeechSynthesisResult, VoiceCommand, and MeetingAudioAnalysis")
    void shouldCreateSynthesisAndMeetingAnalysis() {
        SpeechSynthesisResult tts = new SpeechSynthesisResult(
                "data:audio/wav;base64,UklGRi...",
                VoicePersona.OENEXA_SOVEREIGN,
                AudioFormat.WAV,
                20480L,
                45L
        );
        assertEquals(VoicePersona.OENEXA_SOVEREIGN, tts.persona());

        VoiceCommand cmd = new VoiceCommand(
                "CODE_SEARCH",
                Map.of("query", "AuthService"),
                "Search code for AuthService",
                true
        );
        assertEquals("CODE_SEARCH", cmd.commandType());
        assertTrue(cmd.executed());

        MeetingSpeakerSegment seg1 = new MeetingSpeakerSegment("spk-1", "Alice", 0.0, 15.0, "Architecture review started.");
        MeetingSpeakerSegment seg2 = new MeetingSpeakerSegment("spk-2", "Bob", 15.2, 30.0, "Agreed on Zero-Mock testing policy.");

        MeetingAudioAnalysis meeting = new MeetingAudioAnalysis(
                "Sprint Architecture Sync",
                List.of(seg1, seg2),
                "Team agreed on Zero-Mock and Hexagonal isolation.",
                List.of("Alice to implement Domain", "Bob to configure CI/CD"),
                List.of("Zero-Mock is mandatory across all 23 backend modules")
        );

        assertEquals("Sprint Architecture Sync", meeting.meetingTitle());
        assertEquals(2, meeting.segments().size());
        assertEquals(1, meeting.keyDecisions().size());
    }
}
