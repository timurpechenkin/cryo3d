package io.github.timurpechenkin.domain.model;

import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.geometry.Face;

public abstract class AbstractField2D {
    private final int nx, ny, nz;

    public AbstractField2D(Grid grid) {
        nx = grid.nx();
        ny = grid.ny();
        nz = grid.nz();
    }

    /**
     * Высота грани (количество ячеек по медленной оси).
     *
     * @param face грань
     * @return размер по высоте
     */
    public int height(Face face) {
        return switch (face) {
            case X_MIN, X_MAX -> nz; // Z
            case Y_MIN, Y_MAX -> nz; // Z
            case Z_MIN, Z_MAX -> ny; // Y
        };
    }

    /**
     * Ширина грани (количество ячеек по быстрой оси).
     *
     * @param face грань
     * @return размер по ширине
     */
    public int width(Face face) {
        return switch (face) {
            case X_MIN, X_MAX -> ny; // Y
            case Y_MIN, Y_MAX -> nx; // X
            case Z_MIN, Z_MAX -> nx; // X
        };
    }

    /**
     * Вычисляет индекс в одномерном массиве BC по координатам (w, h).
     *
     * <h3>Соглашение об индексировании</h3>
     *
     * <pre>
     * idx = w + width(face) * h
     * </pre>
     *
     * Где:
     * <ul>
     * <li>{@code w} — координата по «быстрой» оси (width)</li>
     * <li>{@code h} — координата по «медленной» оси (height)</li>
     * </ul>
     *
     * <h3>Ориентация граней</h3>
     * <ul>
     * <li>{@code X_*}: w = Y, h = Z → width = ny, height = nz</li>
     * <li>{@code Y_*}: w = X, h = Z → width = nx, height = nz</li>
     * <li>{@code Z_*}: w = X, h = Y → width = nx, height = ny</li>
     * </ul>
     *
     * @param face грань
     * @param w    координата по ширине (0 ≤ w &lt; width(face))
     * @param h    координата по высоте (0 ≤ h &lt; height(face))
     * @return индекс в одномерном массиве
     *
     * @throws IndexOutOfBoundsException если координаты выходят за допустимые
     *                                   пределы
     */
    public int index(Face face, int w, int h) {
        int W = width(face);
        int H = height(face);

        if (w < 0 || w >= W) {
            throw new IndexOutOfBoundsException("w out of range: " + w);
        }
        if (h < 0 || h >= H) {
            throw new IndexOutOfBoundsException("h out of range: " + h);
        }

        return w + W * h;
    }
}
