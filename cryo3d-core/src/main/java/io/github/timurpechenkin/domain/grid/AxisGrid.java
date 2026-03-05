package io.github.timurpechenkin.domain.grid;

import static io.github.timurpechenkin.geometry.GeometryScale.*;

/**
 * Ось структурированной сетки в fixed-point представлении.
 *
 * <p>
 * <b>Единицы</b>:
 * <ul>
 * <li>{@code SCALE}: целочисленное представление метра (например, SCALE=100 ⇒ 1
 * unit = 0.01 м)</li>
 * <li>{@code SCALED2}: удвоенная точность (2*SCALE), используется для центров
 * ячеек</li>
 * </ul>
 *
 * <p>
 * <b>Соглашения</b>:
 * <ul>
 * <li><b>Позиция ячейки</b> {@code p} — индекс ячейки вдоль оси: 0 ≤ p <
 * cells()</li>
 * <li><b>Позиция ребра</b> {@code e} — индекс ребра: 0 ≤ e ≤ cells()</li>
 * </ul>
 *
 * <p>
 * Ячейка {@code p} занимает интервал по оси
 * [{@code edgesScaled[p]}, {@code edgesScaled[p+1]}), а её центр хранится в
 * {@code centersScaled2[p]}.
 *
 * <p>
 * Инварианты (гарантируются конструктором):
 * <ul>
 * <li>{@code edgesScaled.length == stepsScaled.length + 1}</li>
 * <li>{@code centersScaled2.length == stepsScaled.length}</li>
 * <li>{@code edgesScaled} строго возрастает</li>
 * <li>{@code stepsScaled[p] > 0}</li>
 * </ul>
 */
public record AxisGrid(
        /**
         * Координаты ребер ячеек в формате SCALE.
         *
         * <p>
         * Длина массива: {@code edgesScaled.length = cells + 1}.
         * {@code edgesScaled[e]} — координата e-го ребра.
         * Для ячейки {@code p}: левый край = {@code edgesScaled[p]}, правый край =
         * {@code edgesScaled[p+1]}.
         */
        int[] edgesScaled,

        /**
         * Координаты центров ячеек в формате SCALED2 (2*SCALE).
         *
         * <p>
         * Длина массива: {@code centersScaled2.length = cells}.
         * SCALED2 позволяет хранить половинные значения шага без double.
         */
        int[] centersScaled2,

        /**
         * Длины ячеек (шаги) в формате SCALE.
         *
         * <p>
         * Длина массива: {@code stepsScaled.length = cells}.
         * {@code stepsScaled[p]} соответствует длине интервала
         * {@code edgesScaled[p+1] - edgesScaled[p]} (допускается проверять это как
         * инвариант при желании).
         */
        int[] stepsScaled) {

    /**
     * Проверки согласованности оси.
     *
     * <p>
     * Цель — поймать ошибки построения сетки на ранней стадии:
     * несовпадение длин массивов, нестрого возрастающие ребра,
     * нулевые/отрицательные шаги.
     */
    public AxisGrid {
        if (edgesScaled == null || centersScaled2 == null || stepsScaled == null) {
            throw new IllegalArgumentException("AxisGrid arrays must not be null");
        }
        if (stepsScaled.length != centersScaled2.length) {
            throw new IllegalArgumentException("stepsScaled.length must equal centersScaled2.length");
        }
        if (edgesScaled.length != stepsScaled.length + 1) {
            throw new IllegalArgumentException("edgesScaled.length must equal stepsScaled.length + 1");
        }
        for (int p = 0; p < stepsScaled.length; p++) {
            if (stepsScaled[p] <= 0) {
                throw new IllegalArgumentException("Non-positive step at cell " + p);
            }
            if (edgesScaled[p + 1] <= edgesScaled[p]) {
                throw new IllegalArgumentException("Edges are not strictly increasing at cell " + p);
            }
        }
    }

    /** Количество ячеек по оси (число интервалов). */
    public int cells() {
        return stepsScaled.length;
    }

    /** Координата начала оси (левое граничное ребро) в формате SCALE. */
    public int minEdgeScaled() {
        return edgesScaled[0];
    }

    /** Координата конца оси (правое граничное ребро) в формате SCALE. */
    public int maxEdgeScaled() {
        return edgesScaled[edgesScaled.length - 1];
    }

    /** Длина оси в формате SCALE: {@code maxEdgeScaled() - minEdgeScaled()}. */
    public int sizeScaled() {
        return maxEdgeScaled() - minEdgeScaled();
    }

    // ----- Внешний API в метрах -----

    /**
     * Координата ребра в метрах по позиции ребра {@code e}.
     *
     * @param e позиция ребра (0 ≤ e ≤ cells())
     */
    public double edgeMeters(int e) {
        return scaledToMeters(edgesScaled[e]);
    }

    /**
     * Координата центра ячейки в метрах по позиции ячейки {@code p}.
     *
     * @param p позиция ячейки (0 ≤ p < cells())
     */
    public double centerMeters(int p) {
        return scaled2ToMeters(centersScaled2[p]);
    }

    /**
     * Длина ячейки (шага) в метрах по позиции ячейки {@code p}.
     *
     * @param p позиция ячейки (0 ≤ p < cells())
     */
    public double stepMeters(int p) {
        return scaledToMeters(stepsScaled[p]);
    }

    /** Длина оси в метрах. */
    public double sizeMeters() {
        return scaledToMeters(sizeScaled());
    }
}