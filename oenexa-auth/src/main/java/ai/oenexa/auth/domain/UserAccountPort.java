package ai.oenexa.auth.domain;

import java.util.UUID;
import reactor.core.publisher.Mono;

/** Outbound port: persistence adapter lives in oenexa-core (JPA) in production. */
public interface UserAccountPort {

    Mono<UserAccount> findByEmail(String email);

    Mono<UserAccount> findById(UUID id);

    Mono<UserAccount> save(UserAccount account);
}
