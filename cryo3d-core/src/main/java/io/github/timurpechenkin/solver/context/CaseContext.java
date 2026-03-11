package io.github.timurpechenkin.solver.context;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.grid.Grid3D;

/**
 * Solver-ориентированное представление {@link SimulationCase}.
 *
 * <p>
 * Интерфейс предоставляет solver-ориентированный доступ к данным
 * {@link SimulationCase}
 * (Grid, Field, Library) и
 * позволяет получать параметры ячейки напрямую по её позиции
 * {@code (x, y, z)}.
 *
 * <p>
 *
 * Потокобезопасность: потокобезопасен, если исходные структуры данных
 * {@link SimulationCase} неизменяемы.
 */
public interface CaseContext {

    /**
     * Заполняет контекст на основе кейса
     * 
     * @param simulationCase
     */
    public void createFrom(SimulationCase simulationCase);

    /**
     * Возвращает сетку задачи.
     *
     * <p>
     * Может использоваться солвером для доступа к геометрии:
     * шагам, центрам, объёмам ячеек и т.п.
     *
     * @return 3D-сетка задачи
     */
    public Grid3D grid();

    /**
     * Возвращает линейный индекс ячейки по позиции {@code (x,y,z)}.
     *
     * @param x позиция по оси X
     * @param y позиция по оси Y
     * @param z позиция по оси Z
     * @return линейный индекс ячейки
     * @throws IndexOutOfBoundsException если позиция выходит за пределы сетки
     */
    public int idx(int x, int y, int z);

    // -------------------------------------------------------------------------
    // Материал
    // -------------------------------------------------------------------------

    /**
     * Возвращает идентификатор материала для ячейки.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return индекс материала в библиотеке материалов
     */
    public int materialId(int x, int y, int z);

    /**
     * Возвращает теплопроводность талого грунта.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return теплопроводность талого состояния
     */
    public double thermalConductivityThawed(int x, int y, int z);

    /**
     * Возвращает теплопроводность мерзлого грунта.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return теплопроводность мерзлого состояния
     */
    public double thermalConductivityFrozen(int x, int y, int z);

    /**
     * Возвращает теплоёмкость талого грунта.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return теплоёмкость талого состояния
     */
    public double heatCapacityThawed(int x, int y, int z);

    /**
     * Возвращает теплоёмкость мерзлого грунта.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return теплоёмкость мерзлого состояния
     */
    public double heatCapacityFrozen(int x, int y, int z);

    /**
     * Возвращает скрытую теплоту фазового перехода.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return скрытая теплота фазового перехода
     */
    public double phaseTransitionsHeat(int x, int y, int z);

    /**
     * Возвращает температуру замерзания материала.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return температура замерзания
     */
    public double freezingTemperature(int x, int y, int z);

    // -------------------------------------------------------------------------
    // Температура
    // -------------------------------------------------------------------------

    /**
     * Возвращает температуру ячейки в градусах Цельсия.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return температура ячейки, °C
     */
    public double temperatureC(int x, int y, int z);

    // -------------------------------------------------------------------------
    // Быстрый доступ по линейному индексу
    // -------------------------------------------------------------------------

    /**
     * Возвращает теплопроводность талого состояния по линейному индексу.
     *
     * <p>
     * Удобно использовать внутри плотных расчётных циклов,
     * если индекс уже вычислен заранее.
     *
     * @param cellIndex линейный индекс ячейки
     * @return теплопроводность талого состояния
     */
    public double thermalConductivityThawed(int cellIndex);

    /**
     * Возвращает теплопроводность мерзлого состояния по линейному индексу.
     *
     * @param cellIndex линейный индекс ячейки
     * @return теплопроводность мерзлого состояния
     */
    public double thermalConductivityFrozen(int cellIndex);

    /**
     * Возвращает теплоёмкость талого состояния по линейному индексу.
     *
     * @param cellIndex линейный индекс ячейки
     * @return теплоёмкость талого состояния
     */
    public double heatCapacityThawed(int cellIndex);

    /**
     * Возвращает теплоёмкость мерзлого состояния по линейному индексу.
     *
     * @param cellIndex линейный индекс ячейки
     * @return теплоёмкость мерзлого состояния
     */
    public double heatCapacityFrozen(int cellIndex);

    /**
     * Возвращает скрытую теплоту фазового перехода по линейному индексу.
     *
     * @param cellIndex линейный индекс ячейки
     * @return скрытая теплота фазового перехода
     */
    public double phaseTransitionsHeat(int cellIndex);

    /**
     * Возвращает температуру замерзания по линейному индексу.
     *
     * @param cellIndex линейный индекс ячейки
     * @return температура замерзания
     */
    public double freezingTemperature(int cellIndex);

    /**
     * Возвращает температуру по линейному индексу.
     *
     * @param cellIndex линейный индекс ячейки
     * @return температура, °C
     */
    public double temperatureC(int cellIndex);
}