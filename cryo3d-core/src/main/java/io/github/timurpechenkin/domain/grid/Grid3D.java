package io.github.timurpechenkin.domain.grid;

import io.github.timurpechenkin.geometry.Axis3D;
import io.github.timurpechenkin.geometry.Face;

/**
 * Трёхмерная ортогональная (структурированная) сетка.
 *
 * <p>
 * <b>Соглашения и обозначения</b>:
 * <ul>
 * <li><b>Позиция ячейки — p</b>: номер ячейки вдоль оси (0 ≤ p &lt;
 * n(axis)).</li>
 * <li><b>Позиция ребра — e</b>: номер ребра вдоль оси (0 ≤ e ≤ n(axis)).</li>
 * <li><b>Индекс — i</b>: линейный индекс ячейки в одномерном массиве
 * (row-major).</li>
 * <li><b>Координата — c</b>: физическое положение в метрах (в интерфейсе
 * используется fixed-point).</li>
 * </ul>
 *
 * <p>
 * <b>Единицы измерения</b>:
 * <ul>
 * <li><b>SCALE</b> — fixed-point представление метра.</li>
 * <li><b>SCALED2</b> — удвоенная точность (2*SCALE), используется для
 * центров.</li>
 * <li><b>SCALED3</b> — SCALE^3, используется для объёмов.</li>
 * </ul>
 *
 * <p>
 * <b>Геометрическое соглашение</b>:
 * <ul>
 * <li>{@code edges[e]} — координата e-го ребра в SCALE.</li>
 * <li>Ячейка {@code p} вдоль оси занимает полуинтервал
 * {@code [edges[p], edges[p+1])}.</li>
 * <li>{@code centersScaled2[p]} — координата центра ячейки p в SCALED2.</li>
 * </ul>
 *
 * <p>
 * <b>Порядок линейной адресации (row-major)</b>:
 * 
 * <pre>
 * i = x + nx * (y + ny * z)
 * </pre>
 * 
 * где {@code nx = n(X)}, {@code ny = n(Y)}.
 * Ось X — самая “быстрая” (соседние x идут подряд в памяти).
 */
public interface Grid3D {

    /**
     * Возвращает ось сетки
     * 
     * @param axis3d ось X/Y/Z
     * @return объект с масивами координат центров, длин и сторон ячеек по заданной
     *         оси
     */
    AxisGrid axis(Axis3D axis3d);

    /**
     * Возвращает двумерное представление (Grid2D) указанной грани трёхмерной сетки.
     *
     * <p>
     * Грань определяется фиксированной координатой по одной из осей
     * (X, Y или Z) и двумерной дискретизацией по двум оставшимся осям.
     *
     * <p>
     * <b>Соответствие граней и плоскостей:</b>
     * <ul>
     * <li>{@code Face.X_MIN}, {@code Face.X_MAX} — плоскость YZ</li>
     * <li>{@code Face.Y_MIN}, {@code Face.Y_MAX} — плоскость XZ</li>
     * <li>{@code Face.Z_MIN}, {@code Face.Z_MAX} — плоскость XY</li>
     * </ul>
     *
     * <p>
     * Возвращаемый {@link Grid2D} описывает только дискретизацию плоскости
     * (позиции ячеек {@code p}, позиции рёбер {@code e}, размеры в SCALE и центры в
     * SCALED2),
     * но не хранит информацию о фиксированной координате вдоль нормальной оси
     * (MIN или MAX).
     *
     * <p>
     * Для граней MIN и MAX одной и той же ориентации используется одинаковая
     * двумерная сетка (различается только фиксированное положение вдоль нормали).
     *
     * @param face грань трёхмерной сетки
     * @return двумерная сетка, соответствующая данной грани
     * @throws IllegalArgumentException если грань не поддерживается
     */
    Grid2D faceGrid(Face face);

    /**
     * Общее количество ячеек сетки.
     *
     * <p>
     * Обычно равно {@code (long)n(X) * n(Y) * n(Z)}.
     * Тип {@code long} используется, чтобы избежать переполнения при больших
     * размерах.
     */
    long cellCount();

    /**
     * Количество ячеек вдоль указанной оси.
     *
     * @param axis3d ось X/Y/Z
     * @return число ячеек по оси
     */
    int n(Axis3D axis3d);

    /**
     * Массив координат ребер вдоль оси в формате SCALE.
     *
     * <p>
     * Длина массива равна {@code n(axis3d) + 1}.
     *
     * <p>
     * {@code edges[e]} — координата e-го ребра.
     * Ячейка с позицией {@code p} занимает полуинтервал
     * {@code [edges[p], edges[p+1])}.
     *
     * <p>
     * Массив строго возрастающий: {@code edges[e+1] > edges[e]}.
     *
     * @param axis3d ось X/Y/Z
     * @return массив координат ребер в SCALE
     */
    int[] edgesScaled(Axis3D axis3d);

    /**
     * Массив координат центров ячеек вдоль оси в формате SCALED2 (2*SCALE).
     *
     * <p>
     * Длина массива равна {@code n(axis3d)}.
     * Значения упорядочены по возрастанию и соответствуют центрам ячеек с позициями
     * {@code p=0..n-1}.
     *
     * @param axis3d ось X/Y/Z
     * @return массив координат центров в SCALED2
     */
    int[] centersScaled2(Axis3D axis3d);

    /**
     * Массив длин ячеек (шагов) вдоль оси в формате SCALE.
     *
     * <p>
     * Длина массива равна {@code n(axis3d)}.
     * {@code stepsScaled[p]} — длина ячейки с позицией {@code p} по данной оси.
     *
     * @param axis3d ось X/Y/Z
     * @return массив шагов в SCALE
     */
    int[] stepsScaled(Axis3D axis3d);

    /**
     * Координата центра ячейки с позицией {@code p} вдоль оси в формате SCALED2
     * (2*SCALE).
     *
     * <p>
     * Эквивалентно {@code centersScaled2(axis3d)[p]}.
     *
     * @param axis3d ось X/Y/Z
     * @param p      позиция ячейки (0 ≤ p < n(axis3d))
     * @return координата центра в SCALED2
     * @throws IndexOutOfBoundsException если {@code p} вне диапазона оси
     */
    int centerScaled2(Axis3D axis3d, int p);

    /**
     * Находит позицию ячейки {@code p} вдоль оси по координате {@code c} в формате
     * SCALE.
     *
     * <p>
     * Метод используется при проецировании координаты на дискретную сетку.
     * Правило выбора ячейки на границах должно быть согласовано с соглашением
     * полуинтервалов {@code [edges[p], edges[p+1])}.
     *
     * <p>
     * Ожидаемое поведение:
     * <ul>
     * <li>если {@code c == edges[n]} (правый край оси), возвращается
     * {@code p = n-1};</li>
     * <li>если {@code c} вне диапазона {@code [edges[0], edges[n]]}, выбрасывается
     * исключение.</li>
     * </ul>
     *
     * @param axis3d ось X/Y/Z
     * @param c      координата в SCALE
     * @return позиция ячейки p (0 ≤ p < n(axis3d))
     * @throws IndexOutOfBoundsException если координата вне диапазона оси
     */
    int findCellScaled(Axis3D axis3d, int c);

    /**
     * Длина оси в формате SCALE.
     *
     * <p>
     * Обычно равна {@code edgesScaled(axis3d)[n] - edgesScaled(axis3d)[0]}.
     *
     * @param axis3d ось X/Y/Z
     * @return длина оси в SCALE
     */
    int sizeScaled(Axis3D axis3d);

    /**
     * Объём ячейки (x, y, z) в формате SCALED3 (SCALE^3).
     *
     * <p>
     * Равен {@code stepX[x] * stepY[y] * stepZ[z]}.
     *
     * @throws IndexOutOfBoundsException если любая позиция вне диапазона
     *                                   соответствующей оси
     */
    long cellVolumeScaled3(int x, int y, int z);

    /**
     * Преобразует позицию ячейки (x, y, z) в индекс в одномерном массиве.
     *
     * @param x позиция по оси X (0 ≤ x < n(Axis3D.X))
     * @param y позиция по оси Y (0 ≤ y < n(Axis3D.Y))
     * @param z позиция по оси Z (0 ≤ z < n(Axis3D.Z))
     * @return индекс в одномерном массиве
     * @throws IndexOutOfBoundsException если позиция выходит за пределы сетки
     */
    int index(int x, int y, int z);

    /**
     * Преобразует индекс одномерного массива в позицию ячейки в сетке.
     *
     * @param index индекс в одномерном массиве
     * @return массив из трёх элементов [x, y, z], где:
     *         x - позиция по оси X, y - позиция по оси Y, z - позиция по оси Z
     * @throws IndexOutOfBoundsException если индекс выходит за пределы массива
     */
    int[] position(int index);

    /**
     * Проверяет, является ли позиция ячейки допустимой для этой сетки.
     *
     * @param x позиция по оси X
     * @param y позиция по оси Y
     * @param z позиция по оси Z
     * @return true, если позиция находится в пределах сетки, иначе false
     */
    boolean contains(int x, int y, int z);
}