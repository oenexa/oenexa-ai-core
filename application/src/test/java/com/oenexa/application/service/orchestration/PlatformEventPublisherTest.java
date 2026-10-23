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
import com.oenexa.domain.model.common.DomainEvent.StandardDomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlatformEventPublisher Zero-Mock Unit Tests")
class PlatformEventPublisherTest {

    static class RecordingEventPublisher implements ApplicationEventPublisher {
        final List<Object> publishedEvents = new ArrayList<>();

        @Override
        public void publishEvent(Object event) {
            publishedEvents.add(event);
        }
    }

    @Test
    @DisplayName("Should publish domain event using real event publisher implementation")
    void shouldPublishDomainEvent() {
        RecordingEventPublisher fakePublisher = new RecordingEventPublisher();
        PlatformEventPublisher publisher = new PlatformEventPublisher(fakePublisher);

        DomainEvent event = StandardDomainEvent.of("USER_REGISTERED", "tenant-alpha", Map.of("user", "alice"));

        publisher.publish(event);

        assertEquals(1, fakePublisher.publishedEvents.size());
        assertSame(event, fakePublisher.publishedEvents.get(0));
    }

    @Test
    @DisplayName("Should throw NullPointerException when publishing null event")
    void shouldThrowNpeOnNullEvent() {
        RecordingEventPublisher fakePublisher = new RecordingEventPublisher();
        PlatformEventPublisher publisher = new PlatformEventPublisher(fakePublisher);

        assertThrows(NullPointerException.class, () -> publisher.publish(null));
        assertTrue(fakePublisher.publishedEvents.isEmpty());
    }
}


