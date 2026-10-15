package ai.oenexa.gateway;

import ai.oenexa.common.module.PlatformModule;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Capability discovery: the UI renders its navigation from this endpoint. */
@RestController
@RequestMapping("/api/v1/modules")
public class ModulesController {

    private final List<PlatformModule> modules;

    public ModulesController(List<PlatformModule> modules) {
        this.modules = modules;
    }

    @GetMapping
    public List<PlatformModule.ModuleInfo> list() {
        return modules.stream().map(PlatformModule::info)
                .sorted(Comparator.comparing(PlatformModule.ModuleInfo::status)
                        .thenComparing(PlatformModule.ModuleInfo::id))
                .toList();
    }
}
