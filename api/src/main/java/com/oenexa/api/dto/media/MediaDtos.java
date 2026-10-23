package com.oenexa.api.dto.media;

import java.util.List;

/**
 * Standard Data Transfer Objects for Enterprise Media & Vision AI Services.
 */
public final class MediaDtos {

    private MediaDtos() {}

    public record ImageGenerationRequestDto(
            String prompt,
            String resolution,
            String style
    ) {}

    public record ImageProcessingResultDto(
            String mediaId,
            String imageUri,
            String prompt,
            String resolution,
            String description,
            long processingLatencyMs
    ) {}

    public record ImageEditRequestDto(
            String imageBase64,
            String instruction
    ) {}

    public record VideoSummarizationRequestDto(
            String videoBase64,
            String title
    ) {}

    public record VideoProcessingResultDto(
            String mediaId,
            String videoUri,
            String prompt,
            String summary,
            double durationSec,
            long processingLatencyMs
    ) {}
}
