package com.oenexa.domain.model.common;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

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

