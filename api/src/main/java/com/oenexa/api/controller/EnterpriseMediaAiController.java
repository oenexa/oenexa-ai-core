package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.media.MediaDtos.*;
import com.oenexa.api.mapper.MediaApiMapper;
import com.oenexa.application.security.MediaSecurityPolicy;
import com.oenexa.application.service.orchestration.EnterpriseMediaAiService;
import com.oenexa.application.validation.MediaValidator;
import com.oenexa.domain.model.orchestration.EnterpriseMediaAiDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Enterprise Media AI REST Controller (Phase 8).
 * Supports standard 11-Layer Clean Hexagonal Architecture:
 * Controller -> DTO -> Validation -> Security -> Mapper -> Service -> ApiResponse<T>.
 */
@RestController
@RequestMapping("/api/v1/media")
@CrossOrigin(originPatterns = "*")
public class EnterpriseMediaAiController {

    private final EnterpriseMediaAiService mediaService;
    private final MediaValidator validator;
    private final MediaSecurityPolicy securityPolicy;
    private final MediaApiMapper mapper;

    public EnterpriseMediaAiController(EnterpriseMediaAiService mediaService) {
        this(mediaService, new MediaValidator(), new MediaSecurityPolicy(), new MediaApiMapper());
    }

    @Autowired
    public EnterpriseMediaAiController(
            EnterpriseMediaAiService mediaService,
            MediaValidator validator,
            MediaSecurityPolicy securityPolicy,
            MediaApiMapper mapper
    ) {
        this.mediaService = Objects.requireNonNull(mediaService, "mediaService must not be null");
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.securityPolicy = Objects.requireNonNull(securityPolicy, "securityPolicy must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
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

    // ==========================================
    // 11-Layer Clean Hexagonal Architecture v2 Endpoints
    // ==========================================

    @PostMapping("/v2/image/generate")
    public ResponseEntity<ApiResponse<ImageProcessingResultDto>> generateImageV2(@RequestBody ImageGenerationRequestDto req) {
        validator.validateImageGeneration(req.prompt(), req.resolution());
        securityPolicy.verifyMediaContentSafety(req.prompt());

        ImageResolution res = ImageResolution.RES_1024x1024;
        if (req.resolution() != null) {
            String r = req.resolution().toUpperCase();
            if (!r.startsWith("RES_")) {
                r = "RES_" + r;
            }
            try {
                res = ImageResolution.valueOf(r);
            } catch (IllegalArgumentException ignored) {
                res = ImageResolution.RES_1024x1024;
            }
        }
        ImageProcessingResult result = mediaService.generateImage(req.prompt(), res, req.style());

        return ResponseEntity.ok(ApiResponse.ok("Image generated successfully", mapper.toImageProcessingResultDto(result)));
    }

    @PostMapping("/v2/video/summarize")
    public ResponseEntity<ApiResponse<VideoProcessingResultDto>> summarizeVideoV2(@RequestBody VideoSummarizationRequestDto req) {
        validator.validateVideoSummarization(req.videoBase64(), req.title());
        byte[] bytes = req.videoBase64().getBytes(StandardCharsets.UTF_8);

        VideoProcessingResult result = mediaService.summarizeVideo(bytes, req.title());
        return ResponseEntity.ok(ApiResponse.ok("Video summarized successfully", mapper.toVideoProcessingResultDto(result)));
    }

    public record GenerateImageRequest(String prompt, ImageResolution resolution, String style) {}
    public record EditImageRequest(String imageBase64, String instruction) {}
    public record ImageRequest(String imageBase64) {}
    public record VideoRequest(String videoBase64, String title) {}
    public record GenerateVideoRequest(String prompt, Double durationSec) {}
}
