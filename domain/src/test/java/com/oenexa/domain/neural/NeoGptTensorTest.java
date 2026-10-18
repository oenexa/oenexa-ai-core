package com.oenexa.domain.neural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NeoGptTensor Zero-Mock Unit Tests")
class NeoGptTensorTest {

    @Test
    @DisplayName("Should compute dot product and validate dimensions")
    void testDotProduct() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {4.0, 5.0, 6.0};
        assertEquals(32.0, NeoGptTensor.dot(a, b), 1e-6);

        assertThrows(NullPointerException.class, () -> NeoGptTensor.dot(null, b));
        assertThrows(NullPointerException.class, () -> NeoGptTensor.dot(a, null));
        assertThrows(IllegalArgumentException.class, () -> NeoGptTensor.dot(a, new double[]{1.0, 2.0}));
    }

    @Test
    @DisplayName("Should add and scale vectors")
    void testAddAndScale() {
        double[] a = {1.0, 2.0};
        double[] b = {3.0, 4.0};
        double[] sum = NeoGptTensor.add(a, b);
        assertArrayEquals(new double[]{4.0, 6.0}, sum, 1e-6);

        double[] scaled = NeoGptTensor.scale(a, 3.0);
        assertArrayEquals(new double[]{3.0, 6.0}, scaled, 1e-6);

        assertThrows(NullPointerException.class, () -> NeoGptTensor.add(null, b));
        assertThrows(NullPointerException.class, () -> NeoGptTensor.add(a, null));
        assertThrows(IllegalArgumentException.class, () -> NeoGptTensor.add(a, new double[]{1.0}));
        assertThrows(NullPointerException.class, () -> NeoGptTensor.scale(null, 2.0));
    }

    @Test
    @DisplayName("Should perform vector-matrix multiplication")
    void testMatmul() {
        double[] vec = {1.0, 2.0};
        double[][] mat = {
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        };
        double[] out = NeoGptTensor.matmul(vec, mat);
        assertEquals(3, out.length);
        assertEquals(9.0, out[0], 1e-6);
        assertEquals(12.0, out[1], 1e-6);
        assertEquals(15.0, out[2], 1e-6);

        assertThrows(NullPointerException.class, () -> NeoGptTensor.matmul(null, mat));
        assertThrows(NullPointerException.class, () -> NeoGptTensor.matmul(vec, null));
        assertThrows(IllegalArgumentException.class, () -> NeoGptTensor.matmul(vec, new double[0][0]));
        assertThrows(IllegalArgumentException.class, () -> NeoGptTensor.matmul(new double[]{1.0}, mat));
    }

    @Test
    @DisplayName("Should apply RMSNorm normalization")
    void testRmsNorm() {
        double[] x = {2.0, 2.0, 2.0, 2.0};
        double[] weights = {1.0, 1.0, 1.0, 1.0};
        double[] normed = NeoGptTensor.rmsNorm(x, weights, 1e-5);

        assertEquals(4, normed.length);
        for (double v : normed) {
            assertEquals(1.0, v, 1e-2);
        }

        double[] normedNoWeights = NeoGptTensor.rmsNorm(x, null, 1e-5);
        assertEquals(4, normedNoWeights.length);

        assertThrows(NullPointerException.class, () -> NeoGptTensor.rmsNorm(null, weights, 1e-5));
    }

    @Test
    @DisplayName("Should compute numerically stable Softmax")
    void testSoftmax() {
        double[] logits = {1.0, 2.0, 3.0};
        double[] probs = NeoGptTensor.softmax(logits);

        assertEquals(3, probs.length);
        double sum = 0.0;
        for (double p : probs) {
            sum += p;
            assertTrue(p > 0.0 && p <= 1.0);
        }
        assertEquals(1.0, sum, 1e-6);

        double[] empty = NeoGptTensor.softmax(new double[0]);
        assertEquals(0, empty.length);

        assertThrows(NullPointerException.class, () -> NeoGptTensor.softmax(null));
    }

    @Test
    @DisplayName("Should apply Rotary Positional Embeddings (RoPE)")
    void testRoPE() {
        double[] q = {1.0, 0.0, 0.0, 1.0};
        double[] rotated = NeoGptTensor.applyRope(q, 1, 10000.0);

        assertEquals(4, rotated.length);
        assertNotEquals(0.0, rotated[0]);

        assertThrows(NullPointerException.class, () -> NeoGptTensor.applyRope(null, 0, 10000.0));
    }

    @Test
    @DisplayName("Should compute SiLU and SwiGLU activation functions")
    void testSwiGLU() {
        double silu0 = NeoGptTensor.silu(0.0);
        assertEquals(0.0, silu0, 1e-6);

        double[] gate = {0.0, 2.0};
        double[] up = {1.0, 3.0};
        double[] out = NeoGptTensor.swiglu(gate, up);

        assertEquals(2, out.length);
        assertEquals(0.0, out[0], 1e-6);
        assertTrue(out[1] > 0.0);

        assertThrows(NullPointerException.class, () -> NeoGptTensor.swiglu(null, up));
        assertThrows(NullPointerException.class, () -> NeoGptTensor.swiglu(gate, null));
    }

    @Test
    @DisplayName("Should perform Top-P sampling and Argmax")
    void testSampling() {
        double[] probs = {0.1, 0.7, 0.2};
        assertEquals(1, NeoGptTensor.argmax(probs));
        assertEquals(1, NeoGptTensor.sampleTopP(probs, 0.9, 0.0, 0.5));

        int sample = NeoGptTensor.sampleTopP(probs, 0.9, 0.7, 0.5);
        assertTrue(sample >= 0 && sample < 3);

        assertEquals(0, NeoGptTensor.sampleTopP(new double[0], 0.9, 1.0, 0.5));
        assertThrows(NullPointerException.class, () -> NeoGptTensor.sampleTopP(null, 0.9, 1.0, 0.5));
        assertThrows(NullPointerException.class, () -> NeoGptTensor.argmax(null));
    }
}
