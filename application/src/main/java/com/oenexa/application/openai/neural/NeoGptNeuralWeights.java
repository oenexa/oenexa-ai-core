package com.oenexa.application.openai.neural;

import java.util.Random;

/**
 * Native Transformer Architecture Weights for OENEXA Neo-GPT.
 * Represents embedding tables, multi-head attention projections, RMSNorm weights,
 * SwiGLU feed-forward networks, and LM head projections.
 */
public final class NeoGptNeuralWeights {

    public static final int DEFAULT_HIDDEN_DIM = 64;
    public static final int DEFAULT_NUM_LAYERS = 4;
    public static final int DEFAULT_NUM_HEADS = 4;
    public static final int DEFAULT_HEAD_DIM = 16;
    public static final int DEFAULT_FFN_DIM = 128;

    private final int vocabSize;
    private final int hiddenDim;
    private final int numLayers;
    private final int numHeads;
    private final int headDim;
    private final int ffnDim;

    private final double[][] tokenEmbeddings;
    private final TransformerLayerWeights[] layerWeights;
    private final double[] finalNormWeights;
    private final double[][] lmHeadWeights;

    public record TransformerLayerWeights(
            double[][] qWeight,
            double[][] kWeight,
            double[][] vWeight,
            double[][] outWeight,
            double[] attnNormWeight,
            double[][] gateWeight,
            double[][] upWeight,
            double[][] downWeight,
            double[] ffnNormWeight
    ) {}

    public NeoGptNeuralWeights(int vocabSize) {
        this(vocabSize, DEFAULT_HIDDEN_DIM, DEFAULT_NUM_LAYERS, DEFAULT_NUM_HEADS, DEFAULT_HEAD_DIM, DEFAULT_FFN_DIM, 42L);
    }

    public NeoGptNeuralWeights(
            int vocabSize,
            int hiddenDim,
            int numLayers,
            int numHeads,
            int headDim,
            int ffnDim,
            long randomSeed
    ) {
        this.vocabSize = Math.max(16, vocabSize);
        this.hiddenDim = hiddenDim;
        this.numLayers = numLayers;
        this.numHeads = numHeads;
        this.headDim = headDim;
        this.ffnDim = ffnDim;

        Random rng = new Random(randomSeed);

        // 1. Initialize token embeddings
        this.tokenEmbeddings = initMatrix(this.vocabSize, hiddenDim, rng, 0.05);

        // 2. Initialize layer weights
        this.layerWeights = new TransformerLayerWeights[numLayers];
        for (int l = 0; l < numLayers; l++) {
            double scale = 1.0 / Math.sqrt(hiddenDim);
            double[][] wq = initMatrix(hiddenDim, hiddenDim, rng, scale);
            double[][] wk = initMatrix(hiddenDim, hiddenDim, rng, scale);
            double[][] wv = initMatrix(hiddenDim, hiddenDim, rng, scale);
            double[][] wo = initMatrix(hiddenDim, hiddenDim, rng, scale);

            double[] attnNorm = initVector(hiddenDim, 1.0);

            double[][] gate = initMatrix(hiddenDim, ffnDim, rng, scale);
            double[][] up = initMatrix(hiddenDim, ffnDim, rng, scale);
            double[][] down = initMatrix(ffnDim, hiddenDim, rng, scale);
            double[] ffnNorm = initVector(hiddenDim, 1.0);

            this.layerWeights[l] = new TransformerLayerWeights(
                    wq, wk, wv, wo, attnNorm, gate, up, down, ffnNorm
            );
        }

        // 3. Final norm and LM head
        this.finalNormWeights = initVector(hiddenDim, 1.0);
        this.lmHeadWeights = initMatrix(hiddenDim, this.vocabSize, rng, 0.05);
    }

    private static double[][] initMatrix(int rows, int cols, Random rng, double stdDev) {
        double[][] m = new double[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                m[r][c] = rng.nextGaussian() * stdDev;
            }
        }
        return m;
    }

    private static double[] initVector(int size, double defaultVal) {
        double[] v = new double[size];
        for (int i = 0; i < size; i++) {
            v[i] = defaultVal;
        }
        return v;
    }

    public int getVocabSize() { return vocabSize; }
    public int getHiddenDim() { return hiddenDim; }
    public int getNumLayers() { return numLayers; }
    public int getNumHeads() { return numHeads; }
    public int getHeadDim() { return headDim; }
    public int getFfnDim() { return ffnDim; }
    public double[][] getTokenEmbeddings() { return tokenEmbeddings; }
    public TransformerLayerWeights[] getLayerWeights() { return layerWeights; }
    public double[] getFinalNormWeights() { return finalNormWeights; }
    public double[][] getLmHeadWeights() { return lmHeadWeights; }
}
