package io.github.timurpechenkin.domain.grid;

import io.github.timurpechenkin.geometry.Axis2D;

/**
 * Двумерная ортогональная (структурированная) сетка.
 *
 * <p>
 * <b>Соглашения и обозначения</b>:
 * <ul>
 * <li><b>Позиция ячейки — p</b>: номер ячейки вдоль оси
 * (или пара (w, h) в 2D). Целое число, 0 ≤ p &lt; n(axis).</li>
 * <li><b>Позиция ребра — e</b>: номер ребра вдоль оси.
 * 0 ≤ e ≤ n(axis).</li>
 * <li><b>Индекс — i</b>: линейный индекс ячейки в одномерном массиве
 * (row-major порядок хранения).</li>
 * <li><b>Координата — c</b>: физическое положение в метрах
 * (в этом интерфейсе напрямую не используется).</li>
 * </ul>
 *
 * <p>
 * <b>Единицы измерения</b>:
 * <ul>
 * <li><b>SCALE</b> — целочисленное fixed-point представление метра.</li>
 * <li><b>SCALED2</b> — удвоенная точность (2*SCALE), используется для хранения
 * координат центров ячеек без применения double.</li>
 * </ul>
 *
 * <p>
 * <b>Геометрическое соглашение</b>:
 * Ячейка с позицией {@code p} вдоль оси занимает полуинтервал
 * {@code [edges[e=p], edges[e=p+1])}.
 *
 * <p>
 * <b>Порядок линейной адресации (row-major)</b>:
 *
 * <pre>
 * i = w + width * h
 * </pre>
 *
 * где {@code width = n(Axis2D.W)} и {@code height = n(Axis2D.H)}.
 */
public interface Grid2D {

    /**
     * Общее количество ячеек сетки.
     *
     * <p>
     * Обычно равно {@code (long)width * (long)height}.
     * Тип {@code long} используется, чтобы избежать переполнения при больших
     * размерах,
     * даже если отдельные размеры осей укладываются в {@code int}.
     */
    long cellCount();

    /**
     * Количество ячеек вдоль указанной оси.
     *
     * <p>
     * Для оси ширины ожидается {@code width}, для оси высоты — {@code height}.
     *
     * @param axis2d ось (ширина/высота)
     * @return число ячеек по этой оси
     */
    int n(Axis2D axis2d);

    /**
     * Длина указанной оси в SCALE формате.
     *
     *
     * @param axis2d ось (ширина/высота)
     * @return SCALE длина этой оси
     */
    int lengthScaled(Axis2D axis2d);

    /**
     * Массив координат ребер вдоль оси в формате SCALE.
     *
     * <p>
     * Длина массива равна {@code n(axis2d) + 1}.
     *
     * <p>
     * {@code edges[e]} — координата e-го ребра.
     * Ячейка с позицией {@code p} занимает интервал
     * {@code [edges[p], edges[p+1])}.
     *
     * <p>
     * Массив строго возрастающий:
     * {@code edges[e+1] > edges[e]}.
     *
     * @param axis2d ось (ширина/высота)
     * @return массив координат ребер в формате SCALE
     */
    int[] edgesScaled(Axis2D axis2d);

    /**
     * Массив координат центров ячеек вдоль оси в формате SCALED2.
     *
     * <p>
     * Длина массива равна {@code n(axis2d)}.
     * Значения упорядочены по возрастанию и соответствуют центрам ячеек с позициями
     * {@code p=0..n-1}.
     *
     * @param axis2d ось (ширина/высота)
     * @return массив центров ячеек в SCALED2
     */
    int[] centersScaled2(Axis2D axis2d);

    /**
     * Массив длин сторон (шагов) ячеек вдоль оси в формате SCALED.
     *
     * <p>
     * Длина массива равна {@code n(axis2d)}.
     * Элемент {@code stepsScaled(axis)[p]} — длина стороны p-й ячейки по данной
     * оси.
     *
     * @param axis2d ось (ширина/высота)
     * @return массив шагов ячеек в SCALED
     */
    int[] stepsScaled(Axis2D axis2d);

    /**
     * Координата центра ячейки с позицией {@code i} вдоль оси в формате SCALED2
     * (2*SCALE).
     *
     * <p>
     * Это точечный (O(1)) доступ к {@link #centersScaled2(Axis2D)}.
     *
     * @param axis2d ось (ширина/высота)
     * @param p      позиция ячейки вдоль оси (0 ≤ p < n(axis2d))
     * @return координата центра в SCALED2
     * @throws IndexOutOfBoundsException если {@code p} выходит за пределы оси
     */
    int centerScaled2(Axis2D axis2d, int p);

    /**
     * Находит позицию ячейки вдоль оси по координате в формате SCALED.
     *
     * <p>
     * Метод используется при проецировании точки/координаты на дискретную сетку.
     * Конкретное правило выбора ячейки на границах (например, включительность
     * правой границы)
     * определяется реализацией и должно быть согласовано с остальной геометрией
     * сетки.
     *
     * @param axis2d ось (ширина/высота)
     * @param c      координата в SCALED
     * @return позиция ячейки p (0 ≤ p < n(axis2d))
     * @throws IndexOutOfBoundsException если координата вне диапазона сетки по этой
     *                                   оси
     */
    int findCellScaled(Axis2D axis2d, int c);

    /**
     * Длина соответствующей стороны сетки в формате SCALED.
     *
     * @param axis2d ось (ширина или высота)
     * @return размер в SCALED
     */
    int sizeScaled(Axis2D axis2d);

    /**
     * Преобразует позицию ячейки (w, h) в индекс в одномерном массиве (row-major).
     *
     * <p>
     * Контракт:
     * 
     * <pre>
     * index = w + width * h
     * </pre>
     * 
     * где {@code width = n(Axis2D.W)}.
     *
     * @param w номер столбца (0 ≤ w < width)
     * @param h номер строки (0 ≤ h < height)
     * @return линейный индекс (0 ≤ index < cellCount())
     * @throws IndexOutOfBoundsException если позиция выходит за пределы сетки
     */
    int index(int w, int h);

    /**
     * Преобразует индекс одномерного массива в позицию ячейки (w, h) в 2D сетке.
     *
     * <p>
     * Контракт (обратный к {@link #index(int, int)}):
     * 
     * <pre>
     * w = index % width
     * h = index / width
     * </pre>
     *
     * @param index линейный индекс (0 ≤ index < cellCount())
     * @return массив из двух элементов {@code [w, h]}
     * @throws IndexOutOfBoundsException если индекс выходит за пределы массива
     */
    int[] position(int index);

    /**
     * Проверяет, находится ли позиция (w, h) внутри сетки.
     *
     * <p>
     * Эквивалентно:
     * 
     * <pre>
     * 0 ≤ w < width  &&  0 ≤ h < height
     * </pre>
     *
     * @param w номер столбца
     * @param h номер строки
     * @return {@code true}, если позиция допустима; иначе {@code false}
     */
    boolean contains(int w, int h);
}