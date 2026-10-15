package ai.oenexa.common.event;

import java.time.Instant;
import java.util.UUID;

/** Marker for events published across module boundaries (in-process now, Kafka later). */
public interface DomainEvent {

    UUID eventId();

    String type();

    UUID tenantId();

    Instant occurredAt();
}
