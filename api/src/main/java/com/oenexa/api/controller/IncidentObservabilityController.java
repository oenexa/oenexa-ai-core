package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.observability.IncidentObservabilityDtos.*;
import com.oenexa.application.observability.IncidentObservabilityService;
import com.oenexa.domain.model.observability.IncidentObservabilityDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Enterprise Incident & Observability Intelligence REST Controller.
 * Provides automated incident triage, root cause analysis (RCA),
 * SLO violation correlation, and runbook matching.
 */
@RestController
@RequestMapping("/api/v4/incidents")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class IncidentObservabilityController {

    private final IncidentObservabilityService observabilityService;

    public IncidentObservabilityController(IncidentObservabilityService observabilityService) {
        this.observabilityService = Objects.requireNonNull(observabilityService, "observabilityService must not be null");
    }

    @PostMapping("/triage")
    public ResponseEntity<ApiResponse<IncidentTriageResponseDto>> triageIncident(
            @RequestBody IncidentTriageRequestDto requestDto
    ) {
        IncidentTriageRequest request = (requestDto != null)
                ? requestDto.toDomain()
                : new IncidentTriageRequest("Untitled", "unknown", "", List.of(), List.of(), null);

        IncidentTriageResponse domainResponse = observabilityService.triageIncident(request);
        IncidentTriageResponseDto responseDto = IncidentTriageResponseDto.fromDomain(domainResponse);

        return ResponseEntity.ok(ApiResponse.ok(responseDto));
    }

    @GetMapping("/{incidentId}")
    public ResponseEntity<ApiResponse<IncidentDossierDto>> getIncidentDossier(
            @PathVariable("incidentId") String incidentId
    ) {
        Optional<IncidentDossier> dossier = observabilityService.getDossier(incidentId);
        return dossier
                .map(d -> ResponseEntity.ok(ApiResponse.ok(IncidentDossierDto.fromDomain(d))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.failure(404, "Incident not found: " + incidentId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IncidentDossierDto>>> listIncidents() {
        List<IncidentDossierDto> dossiers = observabilityService.listDossiers().stream()
                .map(IncidentDossierDto::fromDomain)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(dossiers));
    }
}
