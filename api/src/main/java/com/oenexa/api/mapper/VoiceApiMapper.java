package com.oenexa.api.mapper;

import com.oenexa.api.dto.voice.VoiceDtos.*;
import com.oenexa.domain.model.orchestration.EnterpriseVoiceDomain.*;
import org.springframework.stereotype.Component;

/**
 * Enterprise API Mapper for Voice AI DTOs.
 */
@Component
public class VoiceApiMapper {

    public VoiceTranscriptionResultDto toTranscriptionResultDto(TranscriptionResult result) {
        if (result == null) {
            return null;
        }
        return new VoiceTranscriptionResultDto(
                result.text(),
                result.confidence(),
                result.durationMs(),
                result.language()
        );
    }

    public VoiceSynthesisResultDto toSynthesisResultDto(SpeechSynthesisResult result) {
        if (result == null) {
            return null;
        }
        return new VoiceSynthesisResultDto(
                result.audioDataUri(),
                result.persona() != null ? result.persona().name() : "OENEXA_SOVEREIGN",
                result.format() != null ? result.format().name() : "WAV",
                result.sizeBytes(),
                result.generationLatencyMs()
        );
    }

    public VoiceCommandResultDto toCommandResultDto(VoiceCommand command) {
        if (command == null) {
            return null;
        }
        return new VoiceCommandResultDto(
                command.commandType(),
                command.commandType(),
                command.parameters(),
                1.0
        );
    }
}
