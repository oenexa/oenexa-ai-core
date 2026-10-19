package com.oenexa.domain;

import java.util.*;

/**
 * Enterprise Voice AI Domain Model (Phase 7).
 * Encapsulates Speech-to-Text (STT), Text-to-Speech (TTS), Real-time voice commands,
 * and meeting diarization & summarization.
 */
public final class EnterpriseVoiceDomain {

    private EnterpriseVoiceDomain() {}

    public enum AudioFormat {
        WAV,
        MP3,
        WEBM,
        OGG,
        AAC
    }

    public enum VoicePersona {
        ALLOY,
        ECHO,
        FABLE,
        ONYX,
        NOVA,
        SHIMMER,
        OENEXA_SOVEREIGN
    }

    public record WordTimestamp(String word, double startSec, double endSec) {
        public WordTimestamp {
            Objects.requireNonNull(word, "word must not be null");
            if (startSec < 0 || endSec < startSec) {
                throw new IllegalArgumentException("Invalid timestamp range");
            }
        }
    }

    public record TranscriptionResult(
            String text,
            String language,
            double confidence,
            List<WordTimestamp> words,
            long durationMs
    ) {
        public TranscriptionResult {
            Objects.requireNonNull(text, "text must not be null");
            language = (language != null) ? language : "en";
            words = (words != null) ? List.copyOf(words) : List.of();
        }
    }

    public record SpeechSynthesisResult(
            String audioDataUri,
            VoicePersona persona,
            AudioFormat format,
            long sizeBytes,
            long generationLatencyMs
    ) {
        public SpeechSynthesisResult {
            Objects.requireNonNull(audioDataUri, "audioDataUri must not be null");
            Objects.requireNonNull(persona, "persona must not be null");
            Objects.requireNonNull(format, "format must not be null");
        }
    }

    public record VoiceCommand(
            String commandType,
            Map<String, String> parameters,
            String transcribedText,
            boolean executed
    ) {
        public VoiceCommand {
            Objects.requireNonNull(commandType, "commandType must not be null");
            parameters = (parameters != null) ? Map.copyOf(parameters) : Map.of();
            transcribedText = (transcribedText != null) ? transcribedText : "";
        }
    }

    public record MeetingSpeakerSegment(
            String speakerId,
            String speakerName,
            double startSec,
            double endSec,
            String text
    ) {
        public MeetingSpeakerSegment {
            Objects.requireNonNull(speakerId, "speakerId must not be null");
            Objects.requireNonNull(speakerName, "speakerName must not be null");
            Objects.requireNonNull(text, "text must not be null");
        }
    }

    public record MeetingAudioAnalysis(
            String meetingTitle,
            List<MeetingSpeakerSegment> segments,
            String summary,
            List<String> actionItems,
            List<String> keyDecisions
    ) {
        public MeetingAudioAnalysis {
            Objects.requireNonNull(meetingTitle, "meetingTitle must not be null");
            summary = (summary != null) ? summary : "";
            segments = (segments != null) ? List.copyOf(segments) : List.of();
            actionItems = (actionItems != null) ? List.copyOf(actionItems) : List.of();
            keyDecisions = (keyDecisions != null) ? List.copyOf(keyDecisions) : List.of();
        }
    }
}
