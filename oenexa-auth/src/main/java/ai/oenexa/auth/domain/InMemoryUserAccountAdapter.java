package ai.oenexa.auth.domain;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Mono;

/** Default adapter for local development and tests. Replaced by a JPA adapter when a datasource is configured. */
public class InMemoryUserAccountAdapter implements UserAccountPort {

    private final Map<UUID, UserAccount> byId = new ConcurrentHashMap<>();

    @Override
    public Mono<UserAccount> findByEmail(String email) {
        return Mono.defer(() -> Mono.justOrEmpty(byId.values().stream()
                .filter(u -> u.email().equalsIgnoreCase(email)).findFirst()));
    }

    @Override
    public Mono<UserAccount> findById(UUID id) {
        return Mono.defer(() -> Mono.justOrEmpty(byId.get(id)));
    }

    @Override
    public Mono<UserAccount> save(UserAccount account) {
        return Mono.fromRunnable(() -> byId.put(account.id(), account)).thenReturn(account);
    }
}
