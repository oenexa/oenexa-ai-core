package com.oenexa.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaIntelligentPrefetchEngine Zero-Mock Unit Tests")
class OenexaIntelligentPrefetchEngineTest {

    private final OenexaIntelligentPrefetchEngine engine = new OenexaIntelligentPrefetchEngine();

    @Test
    @DisplayName("Should predict next context using structural code relations")
    void shouldPredictNextContextUsingRelations() {
        var prediction = engine.predictNextContext("UserService.java");
        assertNotNull(prediction);
        assertEquals("UserService.java", prediction.triggerKey());
        assertTrue(prediction.predictedKeys().contains("UserRepository.java"));
        assertTrue(prediction.predictedKeys().contains("UserController.java"));
        assertTrue(prediction.confidenceScore() > 0.90);

        var unknownPrediction = engine.predictNextContext("BillingService.java");
        assertTrue(unknownPrediction.predictedKeys().contains("BillingRepository.java"));
        assertTrue(unknownPrediction.predictedKeys().contains("BillingController.java"));
    }

    @Test
    @DisplayName("Should trigger asynchronous background prefetching using virtual threads")
    void shouldTriggerVirtualThreadPrefetch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean called = new AtomicBoolean(false);

        engine.triggerBackgroundPrefetch("UserService.java", key -> {
            called.set(true);
            latch.countDown();
        });

        boolean completed = latch.await(3, TimeUnit.SECONDS);
        assertTrue(completed);
        assertTrue(called.get());

        engine.recordHit();
        assertTrue(engine.getPrefetchHits() > 0);
        assertTrue(engine.getPrefetchAccuracy() > 0.0);
    }
}
