package com.oenexa.api.controller;

import com.oenexa.application.service.orchestration.EnterpriseMediaAiService;
import com.oenexa.domain.model.orchestration.EnterpriseMediaAiDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Enterprise Media AI REST Controller (Phase 8).
 * Exposes Image Generation, Editing, Background Removal, OCR, Visual Analysis,
 * Video Generation, Summarization, Subtitles, and Scene Detection.
 */
@RestController
@RequestMapping("/api/v1/media")
@CrossOrigin(origins = "*")
public class EnterpriseMediaAiController {

    private final EnterpriseMediaAiService mediaService;

    public EnterpriseMediaAiController(EnterpriseMediaAiService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/image/generate")
    public ResponseEntity<ImageProcessingResult> generateImage(@RequestBody GenerateImageRequest req) {
        ImageResolution res = req.resolution() != null ? req.resolution() : ImageResolution.RES_1024x1024;
        return ResponseEntity.ok(mediaService.generateImage(req.prompt(), res, req.style()));
    }

    @PostMapping("/image/edit")
    public ResponseEntity<ImageProcessingResult> editImage(@RequestBody EditImageRequest req) {
        return ResponseEntity.ok(mediaService.editImage(req.imageBase64(), req.instruction()));
    }

    @PostMapping("/image/remove-bg")
    public ResponseEntity<ImageProcessingResult> removeBackground(@RequestBody ImageRequest req) {
        return ResponseEntity.ok(mediaService.removeBackground(req.imageBase64()));
    }

    @PostMapping("/image/ocr")
    public ResponseEntity<ImageProcessingResult> extractOcr(@RequestBody ImageRequest req) {
        return ResponseEntity.ok(mediaService.extractOcr(req.imageBase64()));
    }

    @PostMapping("/image/analyze")
    public ResponseEntity<ImageProcessingResult> analyzeImage(@RequestBody ImageRequest req) {
        return ResponseEntity.ok(mediaService.analyzeImage(req.imageBase64()));
    }

    @PostMapping("/video/summarize")
    public ResponseEntity<VideoProcessingResult> summarizeVideo(@RequestBody VideoRequest req) {
        byte[] bytes = req.videoBase64() != null ? req.videoBase64().getBytes(StandardCharsets.UTF_8) : new byte[0];
        return ResponseEntity.ok(mediaService.summarizeVideo(bytes, req.title()));
    }

    @PostMapping("/video/generate")
    public ResponseEntity<VideoProcessingResult> generateVideo(@RequestBody GenerateVideoRequest req) {
        double dur = req.durationSec() != null ? req.durationSec() : 15.0;
        return ResponseEntity.ok(mediaService.generateVideo(req.prompt(), dur));
    }

    @PostMapping("/video/subtitles")
    public ResponseEntity<List<SubtitleCue>> generateSubtitles(@RequestBody VideoRequest req) {
        byte[] bytes = req.videoBase64() != null ? req.videoBase64().getBytes(StandardCharsets.UTF_8) : new byte[0];
        return ResponseEntity.ok(mediaService.generateSubtitles(bytes));
    }

    @PostMapping("/video/scenes")
    public ResponseEntity<List<VideoScene>> detectScenes(@RequestBody VideoRequest req) {
        byte[] bytes = req.videoBase64() != null ? req.videoBase64().getBytes(StandardCharsets.UTF_8) : new byte[0];
        return ResponseEntity.ok(mediaService.detectScenes(bytes));
    }

    public record GenerateImageRequest(String prompt, ImageResolution resolution, String style) {}
    public record EditImageRequest(String imageBase64, String instruction) {}
    public record ImageRequest(String imageBase64) {}
    public record VideoRequest(String videoBase64, String title) {}
    public record GenerateVideoRequest(String prompt, Double durationSec) {}
}


