package com.oenexa.api;

import com.oenexa.application.EnterpriseMemoryHierarchyService;
import com.oenexa.application.OenexaContextCompressionEngine;
import com.oenexa.application.OenexaIntelligentPrefetchEngine;
import com.oenexa.application.OenexaMemoryEngineV2Service;
import com.oenexa.application.OenexaRepositoryMemoryGraphService;
import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.*;
import com.oenexa.domain.OenexaMemoryEngineV2Domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise REST API Controller for OENEXA V2 / V4 Memory Subsystem.
 * Exposes multi-tier storage, retrieval, prefetching, and memory-mapped buffer telemetry,
 * as well as the V2 SSD-First 6-Step Data Movement Engine.
 */
@RestController
@RequestMapping("/api/v4/memory")
@CrossOrigin(origins = "*")
public class EnterpriseMemoryHierarchyController {

    public record StoreMemoryRequest(
            String workspaceId,
            String userId,
            String key,
            String content,
            List<Float> embedding,
            MemoryLayerTier tier
    ) {}

    public record PrefetchRequest(
            String workspaceId,
            List<String> keys,
            MemoryLayerTier targetTier
    ) {}

    public record V2QueryRequest(String workspaceId, String key) {}
    public record V2CompressRequest(String context) {}
    public record V2PrefetchPredictionRequest(String currentKey) {}

    private final EnterpriseMemoryHierarchyService memoryHierarchyService;
    private final OenexaMemoryEngineV2Service memoryEngineV2Service;
    private final OenexaContextCompressionEngine compressionEngine;
    private final OenexaIntelligentPrefetchEngine prefetchEngine;
    private final OenexaRepositoryMemoryGraphService memoryGraphService;

    public EnterpriseMemoryHierarchyController() {
        this(
                new EnterpriseMemoryHierarchyService(),
                new OenexaMemoryEngineV2Service(),
                new OenexaContextCompressionEngine(),
                new OenexaIntelligentPrefetchEngine(),
                new OenexaRepositoryMemoryGraphService()
        );
    }

    @Autowired
    public EnterpriseMemoryHierarchyController(
            EnterpriseMemoryHierarchyService memoryHierarchyService,
            OenexaMemoryEngineV2Service memoryEngineV2Service,
            OenexaContextCompressionEngine compressionEngine,
            OenexaIntelligentPrefetchEngine prefetchEngine,
            OenexaRepositoryMemoryGraphService memoryGraphService
    ) {
        this.memoryHierarchyService = Objects.requireNonNull(memoryHierarchyService, "memoryHierarchyService must not be null");
        this.memoryEngineV2Service = Objects.requireNonNull(memoryEngineV2Service, "memoryEngineV2Service must not be null");
        this.compressionEngine = Objects.requireNonNull(compressionEngine, "compressionEngine must not be null");
        this.prefetchEngine = Objects.requireNonNull(prefetchEngine, "prefetchEngine must not be null");
        this.memoryGraphService = Objects.requireNonNull(memoryGraphService, "memoryGraphService must not be null");
    }

    @PostMapping("/store")
    public ResponseEntity<HierarchicalMemoryRecord> storeMemory(@RequestBody StoreMemoryRequest request) {
        if (request == null || request.workspaceId() == null || request.key() == null || request.content() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(memoryHierarchyService.store(
                request.workspaceId(),
                request.userId(),
                request.key(),
                request.content(),
                request.embedding(),
                request.tier()
        ));
    }

    @GetMapping("/retrieve")
    public ResponseEntity<MemoryRetrievalResult> retrieveMemory(
            @RequestParam(defaultValue = "default-ws") String workspaceId,
            @RequestParam String key
    ) {
        if (key == null || key.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(memoryHierarchyService.retrieve(workspaceId, key));
    }

    @PostMapping("/prefetch")
    public ResponseEntity<Integer> prefetchMemory(@RequestBody PrefetchRequest request) {
        if (request == null || request.workspaceId() == null || request.keys() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(memoryHierarchyService.prefetch(request.workspaceId(), request.keys(), request.targetTier()));
    }

    @GetMapping("/hierarchy-stats")
    public ResponseEntity<MemoryHierarchyStats> getHierarchyStats() {
        return ResponseEntity.ok(memoryHierarchyService.getHierarchyStats());
    }

    // =========================================================================
    // OENEXA MEMORY ENGINE V2: SSD-FIRST AI MEMORY & DATA MOVEMENT ENDPOINTS
    // =========================================================================

    @PostMapping("/v2/query")
    public ResponseEntity<DataMovementTrace> queryV2(@RequestBody V2QueryRequest request) {
        if (request == null || request.key() == null || request.key().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(memoryEngineV2Service.executeDataMovementPipeline(request.workspaceId(), request.key()));
    }

    @PostMapping("/v2/compress")
    public ResponseEntity<ContextCompressionMetrics> compressV2(@RequestBody V2CompressRequest request) {
        if (request == null || request.context() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(compressionEngine.compressContext(request.context()).metrics());
    }

    @PostMapping("/v2/prefetch")
    public ResponseEntity<IntelligentPrefetchPrediction> prefetchV2(@RequestBody V2PrefetchPredictionRequest request) {
        if (request == null || request.currentKey() == null || request.currentKey().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(prefetchEngine.predictNextContext(request.currentKey()));
    }

    @PostMapping("/v2/evict")
    public ResponseEntity<Integer> evictV2() {
        return ResponseEntity.ok(memoryEngineV2Service.runSmartEvictionCycle());
    }

    @PostMapping("/v2/lifecycle")
    public ResponseEntity<Integer> lifecycleV2() {
        return ResponseEntity.ok(memoryEngineV2Service.runLifecycleDecayCycle());
    }

    @GetMapping("/v2/graph")
    public ResponseEntity<List<RepositoryModuleNode>> getGraphV2(
            @RequestParam(defaultValue = "default-ws") String workspaceId
    ) {
        return ResponseEntity.ok(memoryGraphService.getModules(workspaceId));
    }

    @GetMapping("/v2/stats")
    public ResponseEntity<MemoryEngineV2Stats> getStatsV2() {
        return ResponseEntity.ok(memoryEngineV2Service.getV2Stats());
    }
}
