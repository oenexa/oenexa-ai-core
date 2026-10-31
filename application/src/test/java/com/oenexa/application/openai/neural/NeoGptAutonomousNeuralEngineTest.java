package com.oenexa.application.openai.neural;

import com.oenexa.domain.neural.NeoGptTokenizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NeoGptAutonomousNeuralEngine Zero-Mock Unit Tests")
class NeoGptAutonomousNeuralEngineTest {

    private final NeoGptAutonomousNeuralEngine engine = new NeoGptAutonomousNeuralEngine();

    @Test
    @DisplayName("Should execute forward pass through multi-layer transformer architecture")
    void testForwardPass() {
        List<Integer> tokens = List.of(10, 25, 42, 60);
        double[][] hiddenStates = engine.forwardPass(tokens);

        assertNotNull(hiddenStates);
        assertEquals(4, hiddenStates.length);
        assertEquals(engine.getWeights().getHiddenDim(), hiddenStates[0].length);

        double[][] emptyStates = engine.forwardPass(List.of());
        assertEquals(0, emptyStates.length);
    }

    @Test
    @DisplayName("Should compute dense embeddings autonomously from scratch")
    void testComputeEmbedding() {
        double[] emb = engine.computeEmbedding("Synthesize reactive payment service with Java 27");

        assertNotNull(emb);
        assertEquals(engine.getWeights().getHiddenDim(), emb.length);

        double[] emptyEmb = engine.computeEmbedding("");
        assertEquals(engine.getWeights().getHiddenDim(), emptyEmb.length);
    }

    @Test
    @DisplayName("Should execute autonomous neural generation with o3 deliberative reasoning and code synthesis")
    void testGenerateGeneral() {
        var result = engine.generate("Build payment microservice", "oenexa-neo-gpt-o3-max", "high");

        assertNotNull(result);
        assertNotNull(result.text());
        assertTrue(result.text().contains("Autonomous Software Engineer"));
        assertNotNull(result.thinkingTrace());
        assertTrue(result.thinkingTrace().contains("Native Deliberative Reasoning"));
        assertTrue(result.thinkingTrace().contains("Stage 1: Intent & Invariant Extraction"));
        assertFalse(result.generatedTokens().isEmpty());
        assertEquals(engine.getWeights().getHiddenDim(), result.pooledEmbedding().length);
        assertTrue(result.inferenceTimeMs() >= 0);
    }

    @Test
    @DisplayName("Should generate specialized architecture and testing solutions")
    void testGenerateSpecialized() {
        var archResult = engine.generate("Show C4 system architecture and hexagonal boundaries", "oenexa-neo-gpt-deepseek-arch", "medium");
        assertTrue(archResult.text().contains("Hexagonal Modular Monolith"));
        assertTrue(archResult.text().contains("mermaid"));

        var testResult = engine.generate("Generate JUnit 5 zero-mock test coverage", "oenexa-neo-gpt-coder-32b", "low");
        assertTrue(testResult.text().contains("Strictly Zero-Mock Verification Engine"));
        assertTrue(testResult.text().contains("assertNotNull"));
    }

    @Test
    @DisplayName("Should support custom constructor and null checks")
    void testCustomConstructorAndNullChecks() {
        NeoGptTokenizer tokenizer = new NeoGptTokenizer();
        NeoGptNeuralWeights weights = new NeoGptNeuralWeights(tokenizer.getVocabSize());
        NeoGptAutonomousNeuralEngine customEngine = new NeoGptAutonomousNeuralEngine(tokenizer, weights);

        assertSame(tokenizer, customEngine.getTokenizer());
        assertSame(weights, customEngine.getWeights());

        assertThrows(NullPointerException.class, () -> new NeoGptAutonomousNeuralEngine(null, weights));
        assertThrows(NullPointerException.class, () -> new NeoGptAutonomousNeuralEngine(tokenizer, null));
    }
}
