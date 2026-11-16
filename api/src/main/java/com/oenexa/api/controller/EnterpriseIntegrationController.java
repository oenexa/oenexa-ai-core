package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.integration.IntegrationDtos.*;
import com.oenexa.application.integration.EnterpriseIntegrationService;
import com.oenexa.domain.model.integration.IntegrationDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Enterprise Integration REST API Controller.
 * Manages GitHub, GitLab, and Local Workspace integration endpoints.
 */
@RestController
@RequestMapping("/api/v4/integrations")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class EnterpriseIntegrationController {

    private final EnterpriseIntegrationService integrationService;

    public EnterpriseIntegrationController(EnterpriseIntegrationService integrationService) {
        this.integrationService = Objects.requireNonNull(integrationService, "integrationService must not be null");
    }

    @PostMapping("/webhooks/{provider}")
    public ResponseEntity<ApiResponse<WebhookProcessingResultDto>> processWebhook(
            @PathVariable("provider") String providerStr,
            @RequestBody WebhookPayloadDto payloadDto
    ) {
        IntegrationProvider provider;
        try {
            provider = IntegrationProvider.valueOf(providerStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            provider = IntegrationProvider.GITHUB;
        }

        WebhookPayload payload = (payloadDto != null)
                ? payloadDto.toDomain(provider)
                : new WebhookPayload("DEL-0", provider, WebhookEventType.PUSH_COMMIT, "unknown", "main", "system", null, null);

        WebhookProcessingResult result = integrationService.processWebhook(payload);
        return ResponseEntity.ok(ApiResponse.ok(WebhookProcessingResultDto.fromDomain(result)));
    }

    @PostMapping("/local/inspect")
    public ResponseEntity<ApiResponse<LocalRepositoryInspectResponseDto>> inspectLocalRepository(
            @RequestBody LocalRepositoryInspectRequestDto requestDto
    ) {
        LocalRepositoryInspectRequest request = (requestDto != null)
                ? requestDto.toDomain()
                : new LocalRepositoryInspectRequest(".", List.of(), null, 3);

        LocalRepositoryInspectResponse response = integrationService.inspectLocalRepository(request);
        return ResponseEntity.ok(ApiResponse.ok(LocalRepositoryInspectResponseDto.fromDomain(response)));
    }

    @GetMapping("/configs")
    public ResponseEntity<ApiResponse<List<IntegrationConfigDto>>> listConfigs() {
        List<IntegrationConfigDto> configs = integrationService.listConfigs().stream()
                .map(IntegrationConfigDto::fromDomain)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(configs));
    }

    @GetMapping("/configs/{integrationId}")
    public ResponseEntity<ApiResponse<IntegrationConfigDto>> getConfig(
            @PathVariable("integrationId") String integrationId
    ) {
        Optional<IntegrationConfig> config = integrationService.getConfig(integrationId);
        return config
                .map(c -> ResponseEntity.ok(ApiResponse.ok(IntegrationConfigDto.fromDomain(c))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.failure(404, "Integration config not found: " + integrationId)));
    }
}
