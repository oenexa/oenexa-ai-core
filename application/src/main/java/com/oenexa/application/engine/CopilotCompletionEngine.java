package com.oenexa.application.engine;

import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @deprecated Superceded by {@link OenexaIntelligentCompletionEngine}. Retained for backward compatibility.
 */
@Deprecated
@Component
public class CopilotCompletionEngine {

    private final OenexaIntelligentCompletionEngine delegate;

    public CopilotCompletionEngine(OenexaIntelligentCompletionEngine delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    public CopilotCompletionEngine() {
        this(new OenexaIntelligentCompletionEngine());
    }

    public CopilotCompletionResponse generateCopilotInlineCompletion(CopilotCompletionRequest request) {
        return delegate.generateCopilotInlineCompletion(request);
    }
}
