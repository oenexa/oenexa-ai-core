package com.oenexa.application.validation;

import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Validation Layer for Voice AI Operations.
 */
@Component
public class VoiceValidator {

    public void validateTranscription(String audioBase64, String filename) {
        List<String> violations = new ArrayList<>();
        if (audioBase64 == null || audioBase64.trim().isBlank()) {
            violations.add("audioBase64 payload must not be blank");
        }
        if (filename != null && filename.length() > 255) {
            violations.add("filename must not exceed 255 characters");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Voice transcription validation failed", violations);
        }
    }

    public void validateSynthesis(String text) {
        List<String> violations = new ArrayList<>();
        if (text == null || text.trim().isBlank()) {
            violations.add("text to synthesize must not be blank");
        } else if (text.length() > 10_000) {
            violations.add("text exceeds maximum synthesis length of 10,000 characters");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Voice synthesis validation failed", violations);
        }
    }

    public void validateCommand(String spokenText) {
        if (spokenText == null || spokenText.trim().isBlank()) {
            throw new OenexaValidationException("spokenText command must not be blank");
        }
    }
}
