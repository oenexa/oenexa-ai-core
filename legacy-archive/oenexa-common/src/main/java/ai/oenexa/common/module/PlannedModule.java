package ai.oenexa.common.module;

import java.util.List;

/**
 * Base for modules whose contracts are defined but whose implementation is still on the roadmap.
 * They register with the gateway as PLANNED so the UI and API report capability honestly.
 */
public abstract class PlannedModule implements PlatformModule {

    private final String id;
    private final String displayName;
    private final String description;
    private final List<String> capabilities;

    protected PlannedModule(String id, String displayName, String description, List<String> capabilities) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.capabilities = List.copyOf(capabilities);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public List<String> capabilities() {
        return capabilities;
    }
}
