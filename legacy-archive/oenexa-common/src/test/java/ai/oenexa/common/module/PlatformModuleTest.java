package ai.oenexa.common.module;

import ai.oenexa.common.error.ErrorCode;
import ai.oenexa.common.error.OenexaException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformModuleTest {

    static class DummyPlannedModule extends PlannedModule {
        DummyPlannedModule() {
            super("dummy", "Dummy Module", "A dummy module for testing", List.of("cap1", "cap2"));
        }
    }

    @Test
    void plannedModuleReportsCorrectMetadataAndPlannedStatus() {
        var module = new DummyPlannedModule();
        assertThat(module.id()).isEqualTo("dummy");
        assertThat(module.displayName()).isEqualTo("Dummy Module");
        assertThat(module.description()).isEqualTo("A dummy module for testing");
        assertThat(module.capabilities()).containsExactly("cap1", "cap2");
        assertThat(module.status()).isEqualTo(PlatformModule.ModuleStatus.PLANNED);

        var info = module.info();
        assertThat(info.id()).isEqualTo("dummy");
        assertThat(info.displayName()).isEqualTo("Dummy Module");
        assertThat(info.description()).isEqualTo("A dummy module for testing");
        assertThat(info.capabilities()).containsExactly("cap1", "cap2");
        assertThat(info.status()).isEqualTo(PlatformModule.ModuleStatus.PLANNED);
    }

    @Test
    void oenexaExceptionCoverage() {
        var ex1 = new OenexaException(ErrorCode.VALIDATION, "Validation failed");
        assertThat(ex1.code()).isEqualTo(ErrorCode.VALIDATION);
        assertThat(ex1.getMessage()).isEqualTo("Validation failed");

        var cause = new RuntimeException("cause");
        var ex2 = new OenexaException(ErrorCode.INTERNAL, "Internal error", cause);
        assertThat(ex2.code()).isEqualTo(ErrorCode.INTERNAL);
        assertThat(ex2.getCause()).isSameAs(cause);
    }
}
