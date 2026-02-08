package io.github.timurpechenkin.domain.model;

import io.github.timurpechenkin.domain.grid.Grid;

/**
 * Вспомогательный класс для работы с трёхмерными пространственными данными,
 * хранящимися в одномерном массиве. Обеспечивает преобразование между
 * позициями ячеек в трёхмерной сетке и индексами в одномерном массиве.
 * 
 * <p>
 * Определения:
 * <ul>
 * <li><b>Позиция</b> - номер ячейки в сетке (x, y, z), целые числа</li>
 * <li><b>Индекс</b> - линейный индекс ячейки в одномерном массиве</li>
 * <li><b>Координата</b> - физическое положение в метрах (не используется в этом
 * классе)</li>
 * </ul>
 * <p>
 */
public class Field3D {
    private final int xSize;
    private final int ySize;
    private final int zSize;

    /**
     * Создает новую трёхмерную сетку с указанными размерами.
     *
     * @param xSize количество ячеек по оси X
     * @param ySize количество ячеек по оси Y
     * @param zSize количество ячеек по оси Z
     * @throws IllegalArgumentException если xSize, ySize или zSize меньше или равны
     *                                  0
     */
    public Field3D(int xSize, int ySize, int zSize) {
        if (xSize <= 0 || ySize <= 0 || zSize <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
    }

    /**
     * Создает новую трёхмерную сетку на основе grid.
     *
     * @param grid объект Grid
     * 
     */
    public Field3D(Grid grid) {
        this.xSize = grid.nx();
        this.ySize = grid.ny();
        this.zSize = grid.nz();
    }

    /**
     * Возвращает количество ячеек по оси X.
     *
     * @return количество ячеек по X
     */
    public int xSize() {
        return xSize;
    }

    /**
     * Возвращает количество ячеек по оси Y.
     *
     * @return количество ячеек по Y
     */
    public int ySize() {
        return ySize;
    }

    /**
     * Возвращает количество ячеек по оси Z.
     *
     * @return количество ячеек по Z
     */
    public int zSize() {
        return zSize;
    }

    /**
     * Возвращает общее количество ячеек в сетке.
     *
     * @return общее количество ячеек
     */
    public int size() {
        return xSize * ySize * zSize;
    }

    /**
     * Преобразует позицию ячейки (x, y, z) в индекс в одномерном массиве.
     *
     * @param x позиция по оси X (0 ≤ x < xSize)
     * @param y позиция по оси Y (0 ≤ y < ySize)
     * @param z позиция по оси Z (0 ≤ z < zSize)
     * @return индекс в одномерном массиве
     * @throws IndexOutOfBoundsException если позиция выходит за пределы сетки
     */
    public int index(int x, int y, int z) {
        if (x < 0 || x >= xSize) {
            throw new IndexOutOfBoundsException("X out of range: " + x);
        }
        if (y < 0 || y >= ySize) {
            throw new IndexOutOfBoundsException("Y out of range: " + y);
        }
        if (z < 0 || z >= zSize) {
            throw new IndexOutOfBoundsException("Z out of range: " + z);
        }
        return x + xSize * (y + ySize * z);
    }

    /**
     * Преобразует индекс одномерного массива в позицию ячейки в сетке.
     *
     * @param index индекс в одномерном массиве
     * @return массив из трёх элементов [x, y, z], где:
     *         x - позиция по оси X, y - позиция по оси Y, z - позиция по оси Z
     * @throws IndexOutOfBoundsException если индекс выходит за пределы массива
     */
    public int[] position(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
        }

        int z = index / (xSize * ySize);
        int remainder = index % (xSize * ySize);
        int y = remainder / xSize;
        int x = remainder % xSize;

        return new int[] { x, y, z };
    }

    /**
     * Проверяет, является ли позиция ячейки допустимой для этой сетки.
     *
     * @param x позиция по оси X
     * @param y позиция по оси Y
     * @param z позиция по оси Z
     * @return true, если позиция находится в пределах сетки, иначе false
     */
    public boolean contains(int x, int y, int z) {
        return x >= 0 && x < xSize && y >= 0 && y < ySize && z >= 0 && z < zSize;
    }

    /**
     * Проверяет, является ли индекс допустимым для этой сетки.
     *
     * @param index проверяемый индекс
     * @return true, если индекс находится в пределах массива, иначе false
     */
    public boolean isValid(int index) {
        return index >= 0 && index < size();
    }
}