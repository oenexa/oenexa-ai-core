package ai.oenexa.common.module;

import java.util.List;

/**
 * Contract every OENEXA module implements so the gateway can discover it at runtime.
 * Modules are plain Spring beans; the modular monolith can later be split into
 * services by moving a module (and its beans) behind its own boot application.
 */
public interface PlatformModule {

    String id();

    String displayName();

    String description();

    List<String> capabilities();

    default ModuleStatus status() {
        return ModuleStatus.PLANNED;
    }

    default ModuleInfo info() {
        return new ModuleInfo(id(), displayName(), description(), capabilities(), status());
    }

    enum ModuleStatus { ACTIVE, PLANNED }

    record ModuleInfo(String id, String displayName, String description,
                      List<String> capabilities, ModuleStatus status) {
    }
}
