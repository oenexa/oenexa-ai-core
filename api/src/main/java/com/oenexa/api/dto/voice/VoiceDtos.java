package com.oenexa.api.dto.voice;

import java.util.Map;

/**
 * Standard Data Transfer Objects for Enterprise Voice AI Services.
 */
public final class VoiceDtos {

    private VoiceDtos() {}

    public record VoiceTranscribeRequestDto(
            String audioBase64,
            String filename,
            String format
    ) {}

    public record VoiceTranscriptionResultDto(
            String transcript,
            double confidence,
            long durationMs,
            String language
    ) {}

    public record VoiceSynthesizeRequestDto(
            String text,
            String persona,
            String format
    ) {}

    public record VoiceSynthesisResultDto(
            String audioBase64,
            String persona,
            String format,
            long sizeBytes,
            long generationLatencyMs
    ) {}

    public record VoiceCommandRequestDto(
            String spokenText
    ) {}

    public record VoiceCommandResultDto(
            String intent,
            String action,
            Map<String, String> parameters,
            double confidence
    ) {}
}
