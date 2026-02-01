package io.github.timurpechenkin.domain.bc;

import java.util.EnumMap;
import java.util.Objects;

import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.geometry.Face;

/**
 * BoundaryConditionField хранит дискретизированные граничные условия
 * для каждой грани расчетной области.
 *
 * <p>
 * Для каждой грани {@link Face} хранится одномерный массив индексов
 * граничных условий (BC), где индекс ссылается на
 * {@link BoundaryConditionLibrary}.
 * </p>
 *
 * <p>
 * ВАЖНО: массивы для граней имеют строго определённый порядок индексирования,
 * зависящий от ориентации грани. См. описание в {@link #index(Face, int, int)}.
 * </p>
 *
 * <p>
 * Размеры граней (width/height) НЕ хранятся явно, а вычисляются на основе
 * {@link Grid}.
 * Это гарантирует консистентность размеров при любых изменениях сетки.
 * </p>
 */
public final class BoundaryConditionField {

    /**
     * Для каждой грани хранится массив индексов BC.
     *
     * <p>
     * Длина массива должна быть равна {@code width(face) * height(face)}.
     * </p>
     */
    private final EnumMap<Face, int[]> faceBcIndex;

    private final int nx, ny, nz;

    /**
     * Создаёт поле граничных условий.
     *
     * @param faceBcIndex отображение грань → массив индексов BC
     * @param grid        сетка, определяющая размеры граней
     *
     * @throws NullPointerException     если {@code faceBcIndex} или {@code grid}
     *                                  равны {@code null}
     * @throws IllegalArgumentException если длина массива для какой-либо грани
     *                                  не равна {@code width(face) * height(face)}
     */
    public BoundaryConditionField(EnumMap<Face, int[]> faceBcIndex, Grid grid) {
        this.faceBcIndex = Objects.requireNonNull(faceBcIndex, "faceBcIndex");
        Objects.requireNonNull(grid, "grid");
        nx = grid.nx();
        ny = grid.ny();
        nz = grid.nz();

        // Проверка консистентности размеров массивов
        for (Face f : Face.values()) {
            int[] arr = faceBcIndex.get(f);
            if (arr == null) {
                continue; // допускаем отсутствие массива для грани (мягкий режим)
            }

            int expected = width(f) * height(f);
            if (arr.length != expected) {
                throw new IllegalArgumentException(
                        "Face " + f + ": array length " + arr.length +
                                " != width*height = " + width(f) + "*" + height(f) +
                                " = " + expected);
            }
        }
    }

    /**
     * Возвращает «сырой» массив индексов BC для грани.
     *
     * @param face грань
     * @return массив индексов BC
     *
     * @throws IllegalArgumentException если для грани отсутствует массив
     */
    public int[] raw(Face face) {
        int[] arr = faceBcIndex.get(face);
        if (arr == null) {
            throw new IllegalArgumentException("No BC array for face: " + face);
        }
        return arr;
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

    /**
     * Возвращает индекс граничного условия (BC) для заданной грани
     * и координат на этой грани.
     *
     * @param face грань
     * @param w    координата по ширине
     * @param h    координата по высоте
     * @return индекс BC в {@link BoundaryConditionLibrary}
     */
    public int bcIndex(Face face, int w, int h) {
        return raw(face)[index(face, w, h)];
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
}
