package com.oenexa.application.security;

import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Enterprise Security Policy for Media and Visual Processing.
 */
@Component
public class MediaSecurityPolicy {

    private static final List<String> BLOCKED_PROMPTS = List.of("nsfw", "deepfake_exploit", "weapon_synthesis");

    public void verifyMediaContentSafety(String prompt) {
        if (prompt == null) {
            return;
        }
        String lower = prompt.toLowerCase();
        for (String blocked : BLOCKED_PROMPTS) {
            if (lower.contains(blocked)) {
                throw new OenexaValidationException("Media request violated safety policy: restricted keyword detected.");
            }
        }
    }
}
