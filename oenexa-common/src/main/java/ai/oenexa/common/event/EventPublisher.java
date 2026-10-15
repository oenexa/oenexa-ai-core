package ai.oenexa.common.event;

import reactor.core.publisher.Mono;

/** Outbound port. Adapters: in-process (default) and Kafka (profile "kafka"). */
public interface EventPublisher {

    Mono<Void> publish(DomainEvent event);
}
