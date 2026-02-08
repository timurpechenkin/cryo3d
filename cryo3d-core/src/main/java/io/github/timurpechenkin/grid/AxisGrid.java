package io.github.timurpechenkin.grid;

import static io.github.timurpechenkin.geometry.GeometryScale.*;

/**
 * Ось сетки в fixed-point представлении:
 * все координаты в "scaled units" (например, SCALE=100 => 1 unit = 0.01 м).
 */
public record AxisGrid(
        /**
         * Координаты ребер ячеек (edges.length = cells + 1).
         * edges[i] - левый край i-й ячейки, edges[i+1] - правый. Умножены на SCALE.
         */
        int[] edgesScaled,
        /** Центры ячеек (centers.length = cells). Умножены на SCALE*2. */
        int[] centersScaled2,
        /** Длины ячеек (steps.length = cells). Умножены на SCALE. */
        int[] stepsScaled) {

    // Минимальная защита от некорректной оси
    public AxisGrid {
        if (edgesScaled == null || centersScaled2 == null || stepsScaled == null) {
            throw new IllegalArgumentException("AxisGrid arrays must not be null");
        }
        if (stepsScaled.length != centersScaled2.length) {
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

    /* Количетво ячеек по оси */
    public int cells() {
        return stepsScaled.length;
    }

    /* Кордината начала оси SCALED */
    public int minEdgeScaled() {
        return edgesScaled[0];
    }

    /* Координата конца оси SCALED */
    public int maxEdgeScaled() {
        return edgesScaled[edgesScaled.length - 1];
    }

    /* Длинна оси SCALED */
    public int sizeScaled() {
        return maxEdgeScaled() - minEdgeScaled();
    }

    // ----- Внешний API в метрах -----

    /* Коордната края ячейки по индексу (не SCALED) */
    public double edgeMeters(int edgeIndex) {
        return scaledToMeters(edgesScaled[edgeIndex]);
    }

    /* Коордната центра ячейки по индексу (не SCALED) */
    public double centerMeters(int cellIndex) {
        return scaled2ToMeters(centersScaled2[cellIndex]);
    }

    /* Длинна стороны ячейки по индексу (не SCALED) */
    public double stepMeters(int cellIndex) {
        return scaledToMeters(stepsScaled[cellIndex]);
    }

    /* Длинна оси (не SCALED) */
    public double sizeMeters() {
        return scaledToMeters(sizeScaled());
    }
}
