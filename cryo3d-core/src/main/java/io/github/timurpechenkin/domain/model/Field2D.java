package io.github.timurpechenkin.domain.model;

/**
 * Вспомогательный класс для работы с двумерными пространственными данными,
 * хранящимися в одномерном массиве. Обеспечивает преобразование между
 * позициями ячеек в двумерной сетке и индексами в одномерном массиве.
 * 
 * <p>
 * Определения:
 * <ul>
 * <li><b>Позиция</b> - номер ячейки в сетке (w, h), целые числа</li>
 * <li><b>Индекс</b> - линейный индекс ячейки в одномерном массиве</li>
 * <li><b>Координата</b> - физическое положение в метрах (не используется в этом
 * классе)</li>
 * </ul>
 * 
 * <p>
 */
public class Field2D {
    private final int width;
    private final int height;

    /**
     * Создает новую двумерную сетку с указанными размерами.
     *
     * @param width  количество столбцов (ячеек по горизонтали)
     * @param height количество строк (ячеек по вертикали)
     * @throws IllegalArgumentException если width или height меньше или равны 0
     */
    public Field2D(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        this.width = width;
        this.height = height;
    }

    /**
     * Возвращает количество строк в сетке.
     *
     * @return количество строк
     */
    public int height() {
        return height;
    }

    /**
     * Возвращает количество столбцов в сетке.
     *
     * @return количество столбцов
     */
    public int width() {
        return width;
    }

    /**
     * Возвращает общее количество ячеек в сетке.
     *
     * @return общее количество ячеек
     */
    public int size() {
        return width * height;
    }

    /**
     * Преобразует позицию ячейки (столбец и строку) в индекс в одномерном массиве.
     * Работает по контракту index = w + width*h
     *
     * @param w номер столбца (0 ≤ w < width)
     * @param h номер строки (0 ≤ h < height)
     * @return индекс в одномерном массиве
     * @throws IndexOutOfBoundsException если позиция выходит за пределы сетки
     */
    public int index(int w, int h) {
        if (w < 0 || w >= width) {
            throw new IndexOutOfBoundsException("Column out of range: " + w);
        }
        if (h < 0 || h >= height) {
            throw new IndexOutOfBoundsException("Row out of range: " + h);
        }

        return w + width * h;
    }

    /**
     * Преобразует индекс одномерного массива в позицию ячейки в сетке.
     *
     * @param index индекс в одномерном массиве
     * @return массив из двух элементов [w, h], где:
     *         w - номер столбца, h - номер строки
     * @throws IndexOutOfBoundsException если индекс выходит за пределы массива
     */
    public int[] position(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
        }

        int h = index / width;
        int w = index % width;

        return new int[] { w, h };
    }

    /**
     * Проверяет, является ли позиция ячейки допустимой для этой сетки.
     *
     * @param w номер столбца
     * @param h номер строки
     * @return true, если позиция находится в пределах сетки, иначе false
     */
    public boolean contains(int w, int h) {
        return w >= 0 && w < width && h >= 0 && h < height;
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