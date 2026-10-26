package com.oenexa.application.engine;

import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @deprecated Superceded by {@link OenexaAutonomousExecutionEngine}. Retained for backward compatibility.
 */
@Deprecated
@Component
public class DevinExecutionEngine {

    private final OenexaAutonomousExecutionEngine delegate;

    public DevinExecutionEngine(CursorCodingEngine cursorCodingEngine) {
        Objects.requireNonNull(cursorCodingEngine, "cursorCodingEngine must not be null");
        this.delegate = new OenexaAutonomousExecutionEngine();
    }

    public DevinExecutionEngine() {
        this.delegate = new OenexaAutonomousExecutionEngine();
    }

    public DevinTaskExecutionPlan executeDevinAutonomousTask(String goal, String repositoryId, boolean enforceZeroMock) {
        return delegate.executeDevinAutonomousTask(goal, repositoryId, enforceZeroMock);
    }
}
