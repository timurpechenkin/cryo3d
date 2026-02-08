package io.github.timurpechenkin.domain.bc;

import java.util.EnumMap;
import java.util.Objects;

import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.domain.model.Field2D;
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
     * Массив индексов граничных условий по граням.
     *
     * <p>
     * Длина массива должна быть равна {@code width(face) * height(face)}.
     * </p>
     */
    private final EnumMap<Face, int[]> faceBcIndex;

    /**
     * Двухмерные поля по граням.
     *
     */
    private final EnumMap<Face, Field2D> faceField;

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

        // Проверка консистентности размеров массивов
        EnumMap<Face, Field2D> map = new EnumMap<>(Face.class);
        for (Face f : Face.values()) {
            int[] arr = faceBcIndex.get(f);
            if (arr == null) {
                throw new IllegalArgumentException("There is no boundary condition index arraye for face " + f);
            }

            int height = switch (f) {
                case X_MIN, X_MAX -> grid.nz();
                case Y_MIN, Y_MAX -> grid.nz();
                case Z_MIN, Z_MAX -> grid.ny();
            };
            int width = switch (f) {
                case X_MIN, X_MAX -> grid.ny();
                case Y_MIN, Y_MAX -> grid.nx();
                case Z_MIN, Z_MAX -> grid.nx();
            };

            int expected = width * height;
            if (arr.length != expected) {
                throw new IllegalArgumentException(
                        "Face " + f + ": array length " + arr.length +
                                " != width*height = " + width + "*" + height +
                                " = " + expected);
            }

            map.put(f, new Field2D(width, height));
        }

        faceField = new EnumMap<>(map);
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
        Field2D field2d = faceField.get(face);
        return raw(face)[field2d.index(w, h)];
    }
}
