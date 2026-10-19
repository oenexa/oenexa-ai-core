package com.oenexa.domain;

import java.util.*;

/**
 * Enterprise Media AI Domain Model (Phase 8).
 * Encapsulates Image Generation, Editing, Background Removal, OCR, Visual Inspection,
 * Video Generation, Summarization, Subtitle Generation, and Scene Detection.
 */
public final class EnterpriseMediaAiDomain {

    private EnterpriseMediaAiDomain() {}

    public enum MediaTaskType {
        IMAGE_GENERATION,
        IMAGE_EDIT,
        BACKGROUND_REMOVAL,
        OCR_TEXT_EXTRACTION,
        IMAGE_ANALYSIS,
        VIDEO_SUMMARIZATION,
        AI_VIDEO_GENERATION,
        SUBTITLE_GENERATION,
        SCENE_DETECTION
    }

    public enum ImageResolution {
        RES_512x512,
        RES_1024x1024,
        RES_1792x1024,
        RES_1024x1792
    }

    public record BoundingBox(double x, double y, double width, double height) {
        public BoundingBox {
            if (x < 0 || y < 0 || width < 0 || height < 0) {
                throw new IllegalArgumentException("Bounding box coordinates must be non-negative");
            }
        }
    }

    public record DetectedObject(String label, double confidence, BoundingBox boundingBox) {
        public DetectedObject {
            Objects.requireNonNull(label, "label must not be null");
            boundingBox = (boundingBox != null) ? boundingBox : new BoundingBox(0, 0, 1, 1);
        }
    }

    public record ImageProcessingResult(
            String mediaId,
            String imageUri,
            String prompt,
            ImageResolution resolution,
            String description,
            List<DetectedObject> detectedObjects,
            String extractedOcrText,
            long processingLatencyMs
    ) {
        public ImageProcessingResult {
            Objects.requireNonNull(mediaId, "mediaId must not be null");
            Objects.requireNonNull(imageUri, "imageUri must not be null");
            detectedObjects = (detectedObjects != null) ? List.copyOf(detectedObjects) : List.of();
            extractedOcrText = (extractedOcrText != null) ? extractedOcrText : "";
            description = (description != null) ? description : "";
        }
    }

    public record VideoScene(int sceneIndex, double startSec, double endSec, String description, String keyframeUri) {
        public VideoScene {
            if (sceneIndex < 0 || startSec < 0 || endSec < startSec) {
                throw new IllegalArgumentException("Invalid scene bounds");
            }
            description = (description != null) ? description : "";
            keyframeUri = (keyframeUri != null) ? keyframeUri : "";
        }
    }

    public record SubtitleCue(int cueIndex, double startSec, double endSec, String text) {
        public SubtitleCue {
            if (cueIndex < 0 || startSec < 0 || endSec < startSec) {
                throw new IllegalArgumentException("Invalid subtitle cue bounds");
            }
            Objects.requireNonNull(text, "text must not be null");
        }
    }

    public record VideoProcessingResult(
            String mediaId,
            String videoUri,
            String prompt,
            String summary,
            List<VideoScene> scenes,
            List<SubtitleCue> subtitles,
            double durationSec,
            long processingLatencyMs
    ) {
        public VideoProcessingResult {
            Objects.requireNonNull(mediaId, "mediaId must not be null");
            Objects.requireNonNull(videoUri, "videoUri must not be null");
            summary = (summary != null) ? summary : "";
            scenes = (scenes != null) ? List.copyOf(scenes) : List.of();
            subtitles = (subtitles != null) ? List.copyOf(subtitles) : List.of();
        }
    }
}
