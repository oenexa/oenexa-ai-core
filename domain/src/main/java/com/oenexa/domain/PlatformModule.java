package com.oenexa.domain;

import java.util.List;

/**
 * Platform Module contract for discovery across the enterprise platform.
 */
public interface PlatformModule {

    String id();

    String displayName();

    String description();

    List<String> capabilities();

    default ModuleStatus status() {
        return ModuleStatus.ACTIVE;
    }

    default ModuleInfo info() {
        return new ModuleInfo(id(), displayName(), description(), capabilities(), status());
    }

    enum ModuleStatus { ACTIVE, PLANNED }

    record ModuleInfo(String id, String displayName, String description,
                      List<String> capabilities, ModuleStatus status) {}
}
