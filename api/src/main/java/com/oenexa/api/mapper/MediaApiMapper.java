package com.oenexa.api.mapper;

import com.oenexa.api.dto.media.MediaDtos.*;
import com.oenexa.domain.model.orchestration.EnterpriseMediaAiDomain.*;
import org.springframework.stereotype.Component;

/**
 * Enterprise API Mapper for Media & Vision AI DTOs.
 */
@Component
public class MediaApiMapper {

    public ImageProcessingResultDto toImageProcessingResultDto(ImageProcessingResult result) {
        if (result == null) {
            return null;
        }
        return new ImageProcessingResultDto(
                result.mediaId(),
                result.imageUri(),
                result.prompt(),
                result.resolution() != null ? result.resolution().name() : "RES_1024x1024",
                result.description(),
                result.processingLatencyMs()
        );
    }

    public VideoProcessingResultDto toVideoProcessingResultDto(VideoProcessingResult result) {
        if (result == null) {
            return null;
        }
        return new VideoProcessingResultDto(
                result.mediaId(),
                result.videoUri(),
                result.prompt(),
                result.summary(),
                result.durationSec(),
                result.processingLatencyMs()
        );
    }
}
