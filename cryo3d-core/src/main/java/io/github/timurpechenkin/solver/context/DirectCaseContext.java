package io.github.timurpechenkin.solver.context;

import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.material.Material;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.temperature.TemperatureField;

/**
 * Solver-ориентированное представление {@link SimulationCase}.
 *
 * <p>
 * Этот класс скрывает внутреннюю структуру доменной модели
 * (Grid, Field, Library) и предоставляет солверу простой API,
 * позволяющий получать параметры ячейки напрямую по её позиции
 * {@code (x, y, z)}.
 *
 * <p>
 * В отличие от "ленивого" доступа через библиотеку материалов,
 * данный класс при создании один раз строит кеш массивов параметров
 * по всем ячейкам. Благодаря этому в расчётных циклах солвер работает
 * только с примитивными массивами {@code double[]} и не выполняет
 * повторных обращений к {@link MaterialLibrary}.
 *
 * <p>
 * Основные преимущества:
 * <ul>
 * <li>отсутствие повторных вызовов {@code getByIndex()}</li>
 * <li>отсутствие повторного доступа к объектам {@link Material}</li>
 * <li>более быстрый и компактный код в горячих циклах солвера</li>
 * </ul>
 *
 * <p>
 * Класс является только представлением (view) и не изменяет данные задачи.
 *
 * <p>
 * Потокобезопасность: потокобезопасен, если исходные структуры данных
 * {@link SimulationCase} неизменяемы.
 */
public class DirectCaseContext implements CaseContext {

    private Grid3D grid;

    /** Индекс материала по ячейке. */
    private int[] materialIdByCell;

    /** Температура по ячейке, °C. */
    private double[] temperatureCByCell;

    /** Кеш теплопроводности талого грунта по ячейке. */
    private double[] thermalConductivityThawedByCell;

    /** Кеш теплопроводности мерзлого грунта по ячейке. */
    private double[] thermalConductivityFrozenByCell;

    /** Кеш теплоёмкости талого грунта по ячейке. */
    private double[] heatCapacityThawedByCell;

    /** Кеш теплоёмкости мерзлого грунта по ячейке. */
    private double[] heatCapacityFrozenByCell;

    /** Кеш скрытой теплоты фазового перехода по ячейке. */
    private double[] phaseTransitionsHeatByCell;

    /** Кеш температуры замерзания по ячейке. */
    private double[] freezingTemperatureByCell;

    /**
     * Создаёт solver-ориентированное представление задачи
     * и подготавливает кеш параметров по всем ячейкам.
     *
     * @param c задача моделирования
     * @throws NullPointerException  если один из обязательных элементов задачи
     *                               равен null
     * @throws IllegalStateException если размеры массивов полей не совпадают с
     *                               размером сетки
     */
    public DirectCaseContext() {

    }

    /**
     * Возвращает сетку задачи.
     *
     * <p>
     * Может использоваться солвером для доступа к геометрии:
     * шагам, центрам, объёмам ячеек и т.п.
     *
     * @return 3D-сетка задачи
     */
    public Grid3D grid() {
        return grid;
    }

    /**
     * Возвращает линейный индекс ячейки по позиции {@code (x,y,z)}.
     *
     * @param x позиция по оси X
     * @param y позиция по оси Y
     * @param z позиция по оси Z
     * @return линейный индекс ячейки
     * @throws IndexOutOfBoundsException если позиция выходит за пределы сетки
     */
    public int idx(int x, int y, int z) {
        return grid.index(x, y, z);
    }

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
    public int materialId(int x, int y, int z) {
        return materialIdByCell[idx(x, y, z)];
    }

    /**
     * Возвращает теплопроводность талого грунта.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return теплопроводность талого состояния
     */
    public double thermalConductivityThawed(int x, int y, int z) {
        return thermalConductivityThawedByCell[idx(x, y, z)];
    }

    /**
     * Возвращает теплопроводность мерзлого грунта.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return теплопроводность мерзлого состояния
     */
    public double thermalConductivityFrozen(int x, int y, int z) {
        return thermalConductivityFrozenByCell[idx(x, y, z)];
    }

    /**
     * Возвращает теплоёмкость талого грунта.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return теплоёмкость талого состояния
     */
    public double heatCapacityThawed(int x, int y, int z) {
        return heatCapacityThawedByCell[idx(x, y, z)];
    }

    /**
     * Возвращает теплоёмкость мерзлого грунта.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return теплоёмкость мерзлого состояния
     */
    public double heatCapacityFrozen(int x, int y, int z) {
        return heatCapacityFrozenByCell[idx(x, y, z)];
    }

    /**
     * Возвращает скрытую теплоту фазового перехода.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return скрытая теплота фазового перехода
     */
    public double phaseTransitionsHeat(int x, int y, int z) {
        return phaseTransitionsHeatByCell[idx(x, y, z)];
    }

    /**
     * Возвращает температуру замерзания материала.
     *
     * @param x позиция по X
     * @param y позиция по Y
     * @param z позиция по Z
     * @return температура замерзания
     */
    public double freezingTemperature(int x, int y, int z) {
        return freezingTemperatureByCell[idx(x, y, z)];
    }

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
    public double temperatureC(int x, int y, int z) {
        return temperatureCByCell[idx(x, y, z)];
    }

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
    public double thermalConductivityThawed(int cellIndex) {
        return thermalConductivityThawedByCell[cellIndex];
    }

    /**
     * Возвращает теплопроводность мерзлого состояния по линейному индексу.
     *
     * @param cellIndex линейный индекс ячейки
     * @return теплопроводность мерзлого состояния
     */
    public double thermalConductivityFrozen(int cellIndex) {
        return thermalConductivityFrozenByCell[cellIndex];
    }

    /**
     * Возвращает теплоёмкость талого состояния по линейному индексу.
     *
     * @param cellIndex линейный индекс ячейки
     * @return теплоёмкость талого состояния
     */
    public double heatCapacityThawed(int cellIndex) {
        return heatCapacityThawedByCell[cellIndex];
    }

    /**
     * Возвращает теплоёмкость мерзлого состояния по линейному индексу.
     *
     * @param cellIndex линейный индекс ячейки
     * @return теплоёмкость мерзлого состояния
     */
    public double heatCapacityFrozen(int cellIndex) {
        return heatCapacityFrozenByCell[cellIndex];
    }

    /**
     * Возвращает скрытую теплоту фазового перехода по линейному индексу.
     *
     * @param cellIndex линейный индекс ячейки
     * @return скрытая теплота фазового перехода
     */
    public double phaseTransitionsHeat(int cellIndex) {
        return phaseTransitionsHeatByCell[cellIndex];
    }

    /**
     * Возвращает температуру замерзания по линейному индексу.
     *
     * @param cellIndex линейный индекс ячейки
     * @return температура замерзания
     */
    public double freezingTemperature(int cellIndex) {
        return freezingTemperatureByCell[cellIndex];
    }

    /**
     * Возвращает температуру по линейному индексу.
     *
     * @param cellIndex линейный индекс ячейки
     * @return температура, °C
     */
    public double temperatureC(int cellIndex) {
        return temperatureCByCell[cellIndex];
    }

    @Override
    public void createFrom(SimulationCase c) {
        Objects.requireNonNull(c, "simulation case");

        this.grid = Objects.requireNonNull(c.grid(), "grid");

        MaterialField materialField = Objects.requireNonNull(c.materialField(), "materialField");
        MaterialLibrary materialLibrary = Objects.requireNonNull(c.materialLibrary(), "materialLibrary");
        this.materialIdByCell = Objects.requireNonNull(materialField.materialIndexByCell(), "materialIndexByCell");

        TemperatureField temperatureField = Objects.requireNonNull(c.temperatureField(), "temperatureField");
        this.temperatureCByCell = Objects.requireNonNull(temperatureField.temperatureCByCell(), "temperatureCByCell");

        long cellCountLong = grid.cellCount();
        if (cellCountLong > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "Grid is too large for int[]-based solver arrays: cellCount=" + cellCountLong);
        }
        int cellCount = (int) cellCountLong;

        if (materialIdByCell.length != cellCount) {
            throw new IllegalStateException(
                    "materialIndexByCell length mismatch: expected " + cellCount + ", actual "
                            + materialIdByCell.length);
        }
        if (temperatureCByCell.length != cellCount) {
            throw new IllegalStateException(
                    "temperatureCByCell length mismatch: expected " + cellCount + ", actual "
                            + temperatureCByCell.length);
        }

        this.thermalConductivityThawedByCell = new double[cellCount];
        this.thermalConductivityFrozenByCell = new double[cellCount];
        this.heatCapacityThawedByCell = new double[cellCount];
        this.heatCapacityFrozenByCell = new double[cellCount];
        this.phaseTransitionsHeatByCell = new double[cellCount];
        this.freezingTemperatureByCell = new double[cellCount];

        for (int i = 0; i < cellCount; i++) {
            int materialId = materialIdByCell[i];
            Material material = materialLibrary.getByIndex(materialId);

            thermalConductivityThawedByCell[i] = material.thermalConductivityThawed();
            thermalConductivityFrozenByCell[i] = material.thermalConductivityFrozen();
            heatCapacityThawedByCell[i] = material.heatCapacityThawed();
            heatCapacityFrozenByCell[i] = material.heatCapacityFrozen();
            phaseTransitionsHeatByCell[i] = material.phaseTransitionsHeat();
            freezingTemperatureByCell[i] = material.freezingTemperature();
        }
    }
}