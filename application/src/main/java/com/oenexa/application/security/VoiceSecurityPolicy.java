package com.oenexa.application.security;

import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

/**
 * Enterprise Security Policy for Voice AI and Audio Streams.
 */
@Component
public class VoiceSecurityPolicy {

    private static final int MAX_AUDIO_PAYLOAD_BYTES = 25 * 1024 * 1024; // 25 MB

    public void verifyAudioPayload(byte[] audioBytes) {
        if (audioBytes != null && audioBytes.length > MAX_AUDIO_PAYLOAD_BYTES) {
            throw new OenexaValidationException("Audio payload exceeds maximum permissible size of 25 MB");
        }
    }
}
