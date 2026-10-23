package com.oenexa.api.mapper;

import com.oenexa.api.dto.core.CoreBrainDtos.*;
import com.oenexa.domain.model.cognitive.OenexaCoreBrainDomain.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Enterprise API Mapper for Central Core Brain & Gateway DTOs.
 */
@Component
public class CoreBrainApiMapper {

    public CoreBrainDirective toDirective(CoreBrainDirectiveRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new CoreBrainDirective(
                dto.directiveId() != null ? dto.directiveId() : "dir-" + System.currentTimeMillis(),
                "default-tenant",
                "default-user",
                dto.objective(),
                "oenexa-neo-gpt-o3-max",
                true,
                "high",
                dto.parameters() != null ? dto.parameters() : java.util.Map.of()
        );
    }

    public CoreBrainDirectiveResultDto toDirectiveResultDto(CoreBrainSynthesis synthesis) {
        if (synthesis == null) {
            return null;
        }
        return new CoreBrainDirectiveResultDto(
                synthesis.directiveId(),
                synthesis.status(),
                synthesis.directive() != null ? synthesis.directive().objective() : "Directive executed",
                synthesis.reasoningSteps() != null ? synthesis.reasoningSteps() : List.of(),
                synthesis.confidenceScore()
        );
    }
}
