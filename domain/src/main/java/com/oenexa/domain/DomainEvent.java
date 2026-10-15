package com.oenexa.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard Domain Event contract for inter-module communication.
 */
public interface DomainEvent {

    UUID eventId();

    String type();

    String tenantId();

    Instant occurredAt();

    record StandardDomainEvent(UUID eventId, String type, String tenantId, Instant occurredAt, Object payload)
            implements DomainEvent {
        public static StandardDomainEvent of(String type, String tenantId, Object payload) {
            return new StandardDomainEvent(UUID.randomUUID(), type, tenantId, Instant.now(), payload);
        }
    }
}
