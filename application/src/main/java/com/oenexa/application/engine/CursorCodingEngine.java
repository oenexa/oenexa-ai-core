package com.oenexa.application.engine;

import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @deprecated Superceded by {@link OenexaCodeSynthesisEngine}. Retained for backward compatibility.
 */
@Deprecated
@Component
public class CursorCodingEngine {

    private final OenexaCodeSynthesisEngine delegate;

    public CursorCodingEngine(OenexaCodeSynthesisEngine delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    public CursorCodingEngine() {
        this(new OenexaCodeSynthesisEngine());
    }

    public List<CursorContextReference> curateCursorContext(String query, String repositoryId) {
        return delegate.curateCursorContext(query, repositoryId);
    }

    public AtomicMultiFilePatch generateCursorAtomicPatch(String objective, List<String> filePaths) {
        return delegate.generateCursorAtomicPatch(objective, filePaths);
    }
}
