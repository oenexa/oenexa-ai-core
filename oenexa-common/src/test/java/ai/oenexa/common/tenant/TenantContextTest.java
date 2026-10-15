package ai.oenexa.common.tenant;

import ai.oenexa.common.error.ErrorCode;
import ai.oenexa.common.error.OenexaException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class TenantContextTest {

    @Test
    void currentReturnsBoundTenant() {
        var tenant = new TenantContext.TenantId(UUID.randomUUID());
        StepVerifier.create(TenantContext.write(TenantContext.current(), tenant))
                .expectNext(tenant)
                .verifyComplete();
    }

    @Test
    void currentFailsWithoutTenant() {
        StepVerifier.create(TenantContext.current())
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(OenexaException.class);
                    assertThat(((OenexaException) e).code()).isEqualTo(ErrorCode.UNAUTHORIZED);
                })
                .verify();
    }

    @Test
    void tenantIdParsesFromString() {
        var id = UUID.randomUUID();
        assertThat(TenantContext.TenantId.of(id.toString()).value()).isEqualTo(id);
        assertThat(TenantContext.TenantId.of(id.toString())).hasToString(id.toString());
    }
}
