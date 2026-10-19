package ai.oenexa.code;

import ai.oenexa.common.module.PlannedModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodeModule extends PlannedModule {
    public CodeModule() {
        super("code", "OENEXA Code", "Repository-aware code generation, completion, refactoring and review.",
                List.of("inline-suggestions", "repo-chat", "refactoring", "code-explanation"));
    }
}
