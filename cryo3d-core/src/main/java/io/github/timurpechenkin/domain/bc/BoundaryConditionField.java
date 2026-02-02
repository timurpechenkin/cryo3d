package io.github.timurpechenkin.domain.bc;

import java.util.EnumMap;
import java.util.Objects;

import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.domain.model.AbstractField2D;
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
public final class BoundaryConditionField extends AbstractField2D {

    /**
     * Для каждой грани хранится массив индексов BC.
     *
     * <p>
     * Длина массива должна быть равна {@code width(face) * height(face)}.
     * </p>
     */
    private final EnumMap<Face, int[]> faceBcIndex;

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
        super(grid);
        this.faceBcIndex = Objects.requireNonNull(faceBcIndex, "faceBcIndex");
        Objects.requireNonNull(grid, "grid");

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
}
