package com.oenexa.application.service.orchestration;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.common.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Enterprise Event Publisher supporting in-process event multicasting and future Kafka forwarding.
 */
@Service
public class PlatformEventPublisher {

    private static final System.Logger log = System.getLogger(PlatformEventPublisher.class.getName());

    private final ApplicationEventPublisher springPublisher;

    public PlatformEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    public void publish(DomainEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        log.log(System.Logger.Level.INFO, "Publishing domain event [type={0}, id={1}, tenant={2}]",
                event.type(), event.eventId(), event.tenantId());
        springPublisher.publishEvent(event);
    }
}


