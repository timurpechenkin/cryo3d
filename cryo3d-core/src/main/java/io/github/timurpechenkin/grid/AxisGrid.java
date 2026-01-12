package io.github.timurpechenkin.grid;

import static io.github.timurpechenkin.Constants.SCALE;

/**
 * Ось сетки в fixed-point представлении:
 * все координаты в "scaled units" (например, SCALE=100 => 1 unit = 0.01 м).
 */
public record AxisGrid(
        /**
         * Координаты ребер ячеек (edges.length = cells + 1).
         * edges[i] - левый край i-й ячейки, edges[i+1] - правый.
         */
        int[] edgesScaled,
        /** Центры ячеек (centers.length = cells). */
        int[] centersScaled,
        /** Длины ячеек (steps.length = cells). */
        int[] stepsScaled) {

    // Минимальная защита от некорректной оси
    public AxisGrid {
        if (edgesScaled == null || centersScaled == null || stepsScaled == null) {
            throw new IllegalArgumentException("AxisGrid arrays must not be null");
        }
        if (stepsScaled.length != centersScaled.length) {
            throw new IllegalArgumentException("stepsScaled.length must equal centersScaled.length");
        }
        if (edgesScaled.length != stepsScaled.length + 1) {
            throw new IllegalArgumentException("edgesScaled.length must equal stepsScaled.length + 1");
        }
        for (int i = 0; i < stepsScaled.length; i++) {
            if (stepsScaled[i] <= 0) {
                throw new IllegalArgumentException("Non-positive step at cell " + i);
            }
            if (edgesScaled[i + 1] <= edgesScaled[i]) {
                throw new IllegalArgumentException("Edges are not strictly increasing at " + i);
            }
        }
    }

    public int cells() {
        return stepsScaled.length;
    }

    public int minEdgeScaled() {
        return edgesScaled[0];
    }

    public int maxEdgeScaled() {
        return edgesScaled[edgesScaled.length - 1];
    }

    public int sizeScaled() {
        return maxEdgeScaled() - minEdgeScaled();
    }

    // ----- Внешний API в метрах -----

    public double edgeMeters(int edgeIndex) {
        return edgesScaled[edgeIndex] / (double) SCALE;
    }

    public double centerMeters(int cellIndex) {
        return centersScaled[cellIndex] / (double) SCALE;
    }

    public double stepMeters(int cellIndex) {
        return stepsScaled[cellIndex] / (double) SCALE;
    }

    public double sizeMeters() {
        return sizeScaled() / (double) SCALE;
    }
}
