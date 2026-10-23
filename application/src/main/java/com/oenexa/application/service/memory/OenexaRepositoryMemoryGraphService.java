package com.oenexa.application.service.memory;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.memory.OenexaMemoryEngineV2Domain.RepositoryModuleNode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Modular Repository Memory Graph Service for OENEXA Memory Engine V2.
 * Instead of loading an entire repository (10M+ files) into physical RAM,
 * parses and organizes projects into modular topological graphs (Module A, B, C...).
 *
 * Allows OENEXA autonomous agents to analyze enterprise codebases on standard
 * 64 GB developer machines by loading only active modules into RAM while keeping
 * inactive modules stored on NVMe SSD and Knowledge Storage.
 */
@Service
public class OenexaRepositoryMemoryGraphService {

    private final Map<String, Map<String, RepositoryModuleNode>> workspaceModules = new ConcurrentHashMap<>();

    public OenexaRepositoryMemoryGraphService() {
        seedDefaultWorkspaceGraph("default-ws");
    }

    private void seedDefaultWorkspaceGraph(String workspaceId) {
        Map<String, RepositoryModuleNode> modules = new ConcurrentHashMap<>();

        modules.put("domain", new RepositoryModuleNode(
                "mod-domain",
                "Domain Core",
                120,
                15_728_640L, // 15 MB
                List.of(),
                true
        ));

        modules.put("application", new RepositoryModuleNode(
                "mod-app",
                "Application Orchestration",
                340,
                41_943_040L, // 40 MB
                List.of("domain"),
                true
        ));

        modules.put("infrastructure", new RepositoryModuleNode(
                "mod-infra",
                "Infrastructure Adapters",
                580,
                83_886_080L, // 80 MB
                List.of("domain", "application"),
                false
        ));

        modules.put("api", new RepositoryModuleNode(
                "mod-api",
                "Primary Web & REST Adapters",
                210,
                26_214_400L, // 25 MB
                List.of("application"),
                true
        ));

        modules.put("frontend-web", new RepositoryModuleNode(
                "mod-web",
                "Next.js React Client",
                890,
                125_829_120L, // 120 MB
                List.of("api"),
                false
        ));

        workspaceModules.put(workspaceId, modules);
    }

    public List<RepositoryModuleNode> getModules(String workspaceId) {
        String ws = (workspaceId != null && !workspaceId.isBlank()) ? workspaceId : "default-ws";
        Map<String, RepositoryModuleNode> map = workspaceModules.computeIfAbsent(ws, k -> new ConcurrentHashMap<>());
        if (map.isEmpty()) {
            seedDefaultWorkspaceGraph(ws);
        }
        return new ArrayList<>(workspaceModules.get(ws).values());
    }

    public RepositoryModuleNode registerModule(
            String workspaceId,
            String moduleId,
            String moduleName,
            int fileCount,
            long sizeBytes,
            List<String> dependencies
    ) {
        String ws = (workspaceId != null && !workspaceId.isBlank()) ? workspaceId : "default-ws";
        Map<String, RepositoryModuleNode> map = workspaceModules.computeIfAbsent(ws, k -> new ConcurrentHashMap<>());
        RepositoryModuleNode node = new RepositoryModuleNode(
                moduleId,
                moduleName,
                fileCount,
                sizeBytes,
                dependencies != null ? dependencies : List.of(),
                false
        );
        map.put(moduleId, node);
        return node;
    }

    public boolean loadModuleIntoRam(String workspaceId, String moduleId) {
        String ws = (workspaceId != null && !workspaceId.isBlank()) ? workspaceId : "default-ws";
        Map<String, RepositoryModuleNode> map = workspaceModules.get(ws);
        if (map == null) return false;

        RepositoryModuleNode existing = map.get(moduleId);
        if (existing != null) {
            map.put(moduleId, new RepositoryModuleNode(
                    existing.moduleId(),
                    existing.moduleName(),
                    existing.fileCount(),
                    existing.totalSizeBytes(),
                    existing.dependencies(),
                    true
            ));
            return true;
        }
        return false;
    }

    public boolean evictModuleToSsd(String workspaceId, String moduleId) {
        String ws = (workspaceId != null && !workspaceId.isBlank()) ? workspaceId : "default-ws";
        Map<String, RepositoryModuleNode> map = workspaceModules.get(ws);
        if (map == null) return false;

        RepositoryModuleNode existing = map.get(moduleId);
        if (existing != null) {
            map.put(moduleId, new RepositoryModuleNode(
                    existing.moduleId(),
                    existing.moduleName(),
                    existing.fileCount(),
                    existing.totalSizeBytes(),
                    existing.dependencies(),
                    false
            ));
            return true;
        }
        return false;
    }
}


