package com.oenexa.application.bdd;

import com.oenexa.application.service.memory.OenexaContextCompressionEngine;
import com.oenexa.application.service.memory.OenexaMemoryEngineV2Service;
import com.oenexa.domain.model.memory.OenexaMemoryEngineV2Domain.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber BDD Step Definitions for OENEXA Memory Engine V2 SSD-First Architecture.
 */
@Tag("bdd")
public class MemoryEngineV2Steps {

    private final OenexaMemoryEngineV2Service memoryService = new OenexaMemoryEngineV2Service();
    private final OenexaContextCompressionEngine compressionEngine = new OenexaContextCompressionEngine();

    private DataMovementTrace initialTrace;
    private DataMovementTrace subsequentTrace;
    private String rawContextBuffer;
    private OenexaContextCompressionEngine.CompressionResult compressionResult;

    @Given("a memory engine initialized with sample repository files")
    public void aMemoryEngineInitializedWithSampleRepositoryFiles() {
        assertNotNull(memoryService.getV2Stats());
    }

    @When("the user requests file {string} in workspace {string}")
    public void theUserRequestsFileInWorkspace(String key, String workspaceId) {
        this.initialTrace = memoryService.executeDataMovementPipeline(workspaceId, key);
        this.subsequentTrace = memoryService.executeDataMovementPipeline(workspaceId, key);
    }

    @Then("the data movement pipeline returns the item successfully")
    public void theDataMovementPipelineReturnsTheItemSuccessfully() {
        assertNotNull(initialTrace);
        assertNotNull(initialTrace.finalContent());
        assertFalse(initialTrace.finalContent().isBlank());
        assertTrue(initialTrace.found());
    }

    @Then("the item is promoted to the L1 cache")
    public void theItemIsPromotedToTheL1Cache() {
        MemoryEngineV2Stats stats = memoryService.getV2Stats();
        assertTrue(stats.l1Count() > 0, "Item must be promoted into L1 cache");
    }

    @Then("subsequent retrieval hits the hot cache immediately")
    public void subsequentRetrievalHitsTheHotCacheImmediately() {
        assertNotNull(subsequentTrace);
        assertEquals(BrainMemoryTier.L1_HOT_EXECUTION, subsequentTrace.resolvedTier());
        assertTrue(subsequentTrace.totalDurationNanos() < 50_000_000L);
    }

    @Given("a large context buffer of 1000 lines with duplicate import statements")
    public void aLargeContextBufferOf1000LinesWithDuplicateImportStatements() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("import java.util.List;\n");
            sb.append("import java.util.Map;\n");
        }
        sb.append("public class OrderService {\n");
        sb.append("    public void processOrder(String orderId) {}\n");
        sb.append("}\n");
        this.rawContextBuffer = sb.toString();
    }

    @When("the context compression engine processes the buffer")
    public void theContextCompressionEngineProcessesTheBuffer() {
        this.compressionResult = compressionEngine.compressContext(this.rawContextBuffer);
    }

    @Then("the compressed size should be significantly smaller than original size")
    public void theCompressedSizeShouldBeSignificantlySmallerThanOriginalSize() {
        assertNotNull(compressionResult);
        long original = compressionResult.metrics().originalSizeBytes();
        long compressed = compressionResult.metrics().compressedSizeBytes();
        assertTrue(compressed < original, "Compressed size must be strictly smaller than original");
        assertTrue(compressionResult.metrics().compressionRatioPercent() > 50.0, "Compression ratio should exceed 50%");
    }

    @Then("critical function signatures must be preserved")
    public void criticalFunctionSignaturesMustBePreserved() {
        String decompressed = compressionEngine.decompressContext(compressionResult.compressedBytes());
        assertTrue(decompressed.contains("processOrder(String orderId)"));
    }
}


