package com.oenexa.application.openai.neural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NeoGptNeuralWeights Zero-Mock Unit Tests")
class NeoGptNeuralWeightsTest {

    @Test
    @DisplayName("Should initialize neural weights with default transformer dimensions")
    void testDefaultWeightsInitialization() {
        NeoGptNeuralWeights weights = new NeoGptNeuralWeights(500);

        assertEquals(500, weights.getVocabSize());
        assertEquals(NeoGptNeuralWeights.DEFAULT_HIDDEN_DIM, weights.getHiddenDim());
        assertEquals(NeoGptNeuralWeights.DEFAULT_NUM_LAYERS, weights.getNumLayers());
        assertEquals(NeoGptNeuralWeights.DEFAULT_NUM_HEADS, weights.getNumHeads());
        assertEquals(NeoGptNeuralWeights.DEFAULT_HEAD_DIM, weights.getHeadDim());
        assertEquals(NeoGptNeuralWeights.DEFAULT_FFN_DIM, weights.getFfnDim());

        assertNotNull(weights.getTokenEmbeddings());
        assertEquals(500, weights.getTokenEmbeddings().length);
        assertEquals(NeoGptNeuralWeights.DEFAULT_HIDDEN_DIM, weights.getTokenEmbeddings()[0].length);

        assertNotNull(weights.getLayerWeights());
        assertEquals(NeoGptNeuralWeights.DEFAULT_NUM_LAYERS, weights.getLayerWeights().length);

        var layer0 = weights.getLayerWeights()[0];
        assertNotNull(layer0.qWeight());
        assertNotNull(layer0.kWeight());
        assertNotNull(layer0.vWeight());
        assertNotNull(layer0.outWeight());
        assertNotNull(layer0.attnNormWeight());
        assertNotNull(layer0.gateWeight());
        assertNotNull(layer0.upWeight());
        assertNotNull(layer0.downWeight());
        assertNotNull(layer0.ffnNormWeight());

        assertNotNull(weights.getFinalNormWeights());
        assertEquals(NeoGptNeuralWeights.DEFAULT_HIDDEN_DIM, weights.getFinalNormWeights().length);

        assertNotNull(weights.getLmHeadWeights());
        assertEquals(NeoGptNeuralWeights.DEFAULT_HIDDEN_DIM, weights.getLmHeadWeights().length);
        assertEquals(500, weights.getLmHeadWeights()[0].length);
    }

    @Test
    @DisplayName("Should initialize neural weights with custom dimensions and seed")
    void testCustomWeightsInitialization() {
        NeoGptNeuralWeights weights = new NeoGptNeuralWeights(
                100, 32, 2, 2, 16, 64, 12345L
        );

        assertEquals(100, weights.getVocabSize());
        assertEquals(32, weights.getHiddenDim());
        assertEquals(2, weights.getNumLayers());
        assertEquals(2, weights.getNumHeads());
        assertEquals(16, weights.getHeadDim());
        assertEquals(64, weights.getFfnDim());
    }
}
