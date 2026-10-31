package com.oenexa.domain.neural;

import java.util.Arrays;
import java.util.Objects;

/**
 * Enterprise Native Neural Tensor Mathematics Engine for OENEXA Neo-GPT.
 * Implements high-performance matrix multiplications, RMSNorm, Rotary Positional Embeddings (RoPE),
 * Softmax, and SwiGLU activation functions in pure Java 27.
 */
public final class NeoGptTensor {

    private NeoGptTensor() {}

    public static double dot(double[] a, double[] b) {
        Objects.requireNonNull(a, "Vector a must not be null");
        Objects.requireNonNull(b, "Vector b must not be null");
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vector lengths must match: " + a.length + " != " + b.length);
        }
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    public static double[] add(double[] a, double[] b) {
        Objects.requireNonNull(a, "Vector a must not be null");
        Objects.requireNonNull(b, "Vector b must not be null");
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vector lengths must match: " + a.length + " != " + b.length);
        }
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    public static double[] scale(double[] a, double factor) {
        Objects.requireNonNull(a, "Vector a must not be null");
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * factor;
        }
        return result;
    }

    public static double[] matmul(double[] vec, double[][] matrix) {
        Objects.requireNonNull(vec, "Vector must not be null");
        Objects.requireNonNull(matrix, "Matrix must not be null");
        if (matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Matrix must not be empty");
        }
        if (vec.length != matrix.length) {
            throw new IllegalArgumentException("Dimension mismatch: vec.length=" + vec.length + ", matrix.rows=" + matrix.length);
        }
        int cols = matrix[0].length;
        double[] out = new double[cols];
        for (int c = 0; c < cols; c++) {
            double sum = 0.0;
            for (int r = 0; r < vec.length; r++) {
                sum += vec[r] * matrix[r][c];
            }
            out[c] = sum;
        }
        return out;
    }

    public static double[] rmsNorm(double[] x, double[] weight, double eps) {
        Objects.requireNonNull(x, "Input x must not be null");
        double sumSq = 0.0;
        for (double v : x) {
            sumSq += v * v;
        }
        double meanSq = sumSq / Math.max(1, x.length);
        double rms = Math.sqrt(meanSq + eps);
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            double w = (weight != null && i < weight.length) ? weight[i] : 1.0;
            out[i] = (x[i] / rms) * w;
        }
        return out;
    }

    public static double[] softmax(double[] logits) {
        Objects.requireNonNull(logits, "Logits must not be null");
        if (logits.length == 0) {
            return new double[0];
        }
        double max = Double.NEGATIVE_INFINITY;
        for (double v : logits) {
            if (v > max) max = v;
        }
        double sum = 0.0;
        double[] exp = new double[logits.length];
        for (int i = 0; i < logits.length; i++) {
            exp[i] = Math.exp(logits[i] - max);
            sum += exp[i];
        }
        double invSum = sum > 0.0 ? 1.0 / sum : 1.0;
        for (int i = 0; i < logits.length; i++) {
            exp[i] *= invSum;
        }
        return exp;
    }

    public static double[] applyRope(double[] qOrK, int position, double baseTheta) {
        Objects.requireNonNull(qOrK, "Vector must not be null");
        double[] out = Arrays.copyOf(qOrK, qOrK.length);
        int half = out.length / 2;
        for (int i = 0; i < half; i++) {
            double theta = 1.0 / Math.pow(baseTheta, (2.0 * i) / out.length);
            double angle = position * theta;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double v0 = out[i];
            double v1 = out[i + half];
            out[i] = v0 * cos - v1 * sin;
            out[i + half] = v0 * sin + v1 * cos;
        }
        return out;
    }

    public static double silu(double x) {
        return x / (1.0 + Math.exp(-x));
    }

    public static double[] swiglu(double[] gate, double[] up) {
        Objects.requireNonNull(gate, "Gate vector must not be null");
        Objects.requireNonNull(up, "Up vector must not be null");
        int len = Math.min(gate.length, up.length);
        double[] out = new double[len];
        for (int i = 0; i < len; i++) {
            out[i] = silu(gate[i]) * up[i];
        }
        return out;
    }

    public static int sampleTopP(double[] probabilities, double topP, double temperature, double randomVal) {
        Objects.requireNonNull(probabilities, "Probabilities must not be null");
        if (probabilities.length == 0) return 0;
        if (temperature <= 0.01) {
            return argmax(probabilities);
        }

        // Cumulative sum thresholding
        double p = Math.clamp(topP, 0.01, 1.0);
        double r = Math.clamp(randomVal, 0.0, 0.9999);

        double cumulative = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulative += probabilities[i];
            if (cumulative >= r || cumulative >= p) {
                return i;
            }
        }
        return probabilities.length - 1;
    }

    public static int argmax(double[] values) {
        Objects.requireNonNull(values, "Values must not be null");
        int bestIdx = 0;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > bestVal) {
                bestVal = values[i];
                bestIdx = i;
            }
        }
        return bestIdx;
    }
}
