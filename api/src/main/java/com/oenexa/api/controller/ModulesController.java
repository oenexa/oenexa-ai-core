package com.oenexa.api.controller;

import com.oenexa.domain.PlannedModule;
import com.oenexa.domain.PlatformModule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/**
 * Enterprise Module Discovery REST Controller.
 * Provides live discovery across all 20 platform modules for UI navigation and service registration.
 */
@RestController
@RequestMapping("/api/v1/modules")
public class ModulesController {

    private final List<PlatformModule> modules = PlannedModule.allModules();

    @GetMapping
    public ResponseEntity<List<PlatformModule.ModuleInfo>> listModules() {
        List<PlatformModule.ModuleInfo> list = modules.stream()
                .map(PlatformModule::info)
                .sorted(Comparator.comparing(PlatformModule.ModuleInfo::status)
                        .thenComparing(PlatformModule.ModuleInfo::id))
                .toList();

        return ResponseEntity.ok(list);
    }
}
