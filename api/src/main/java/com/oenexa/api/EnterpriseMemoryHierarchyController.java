package com.oenexa.api;

import com.oenexa.application.EnterpriseMemoryHierarchyService;
import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise REST API Controller for OENEXA V4 7-Layer Memory Subsystem.
 * Exposes multi-tier storage, retrieval, prefetching, and memory-mapped buffer telemetry.
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

    private final EnterpriseMemoryHierarchyService memoryHierarchyService;

    public EnterpriseMemoryHierarchyController() {
        this(new EnterpriseMemoryHierarchyService());
    }

    @Autowired
    public EnterpriseMemoryHierarchyController(EnterpriseMemoryHierarchyService memoryHierarchyService) {
        this.memoryHierarchyService = Objects.requireNonNull(memoryHierarchyService, "memoryHierarchyService must not be null");
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
}
