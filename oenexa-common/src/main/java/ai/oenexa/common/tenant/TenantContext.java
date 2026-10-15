package ai.oenexa.common.tenant;

import java.util.Objects;
import java.util.UUID;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

/**
 * Tenant isolation helper for reactive pipelines. The tenant id is written into the
 * Reactor context by the authentication layer and read by every adapter that touches
 * tenant-scoped data (repositories, vector collections, search indices).
 */
public final class TenantContext {

    public static final String KEY = "oenexa.tenant";

    private TenantContext() {
    }

    public record TenantId(UUID value) {
        public TenantId {
            Objects.requireNonNull(value, "tenant id");
        }

        public static TenantId of(String raw) {
            return new TenantId(UUID.fromString(raw));
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    public static Context with(Context context, TenantId tenant) {
        return context.put(KEY, tenant);
    }

    public static <T> Mono<T> write(Mono<T> mono, TenantId tenant) {
        return mono.contextWrite(ctx -> with(ctx, tenant));
    }

    public static Mono<TenantId> current() {
        return Mono.deferContextual(ctx -> fromContext(ctx));
    }

    private static Mono<TenantId> fromContext(ContextView ctx) {
        return ctx.<TenantId>getOrEmpty(KEY)
                .map(Mono::just)
                .orElseGet(() -> Mono.error(new ai.oenexa.common.error.OenexaException(
                        ai.oenexa.common.error.ErrorCode.UNAUTHORIZED, "No tenant bound to request")));
    }
}
