package com.oenexa.application.engine;

import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @deprecated Superceded by {@link OenexaDeepReasoningEngine}. Retained for backward compatibility.
 */
@Deprecated
@Component
public class ClaudeReasoningEngine {

    private final OenexaDeepReasoningEngine delegate;

    public ClaudeReasoningEngine(OenexaDeepReasoningEngine delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    public ClaudeReasoningEngine() {
        this(new OenexaDeepReasoningEngine());
    }

    public ClaudeReasoningTrace analyzeWithClaudeReasoning(String objective, String codeContext) {
        return delegate.analyzeWithClaudeReasoning(objective, codeContext);
    }
}
