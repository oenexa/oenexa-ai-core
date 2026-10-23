package com.oenexa.application.validation;

import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Validation Layer for Media & Vision AI Operations.
 */
@Component
public class MediaValidator {

    public void validateImageGeneration(String prompt, String resolution) {
        List<String> violations = new ArrayList<>();
        if (prompt == null || prompt.trim().isBlank()) {
            violations.add("prompt for image generation must not be blank");
        }
        if (resolution != null && !isValidResolution(resolution)) {
            violations.add("unsupported image resolution: " + resolution);
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Image generation validation failed", violations);
        }
    }

    public void validateImageInput(String imageBase64) {
        if (imageBase64 == null || imageBase64.trim().isBlank()) {
            throw new OenexaValidationException("imageBase64 payload must not be blank");
        }
    }

    public void validateVideoSummarization(String videoBase64, String title) {
        List<String> violations = new ArrayList<>();
        if (videoBase64 == null || videoBase64.trim().isBlank()) {
            violations.add("videoBase64 payload must not be blank");
        }
        if (title != null && title.length() > 200) {
            violations.add("title must not exceed 200 characters");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Video summarization validation failed", violations);
        }
    }

    private boolean isValidResolution(String resolution) {
        return "1024x1024".equalsIgnoreCase(resolution)
                || "RES_1024x1024".equalsIgnoreCase(resolution)
                || "1920x1080".equalsIgnoreCase(resolution)
                || "512x512".equalsIgnoreCase(resolution);
    }
}
