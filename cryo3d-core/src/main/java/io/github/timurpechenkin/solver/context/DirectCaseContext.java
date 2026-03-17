package io.github.timurpechenkin.solver.context;

import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.material.Material;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.temperature.TemperatureField;

/**
 * Базовая реализация {@link CaseContext}, создающая начальное runtime-состояние
 * расчёта напрямую из {@link SimulationCase}.
 *
 * <p>
 * Контекст хранит:
 * <ul>
 * <li>расчётную сетку;</li>
 * <li>текущее температурное поле;</li>
 * <li>кеш параметров материалов по всем ячейкам;</li>
 * <li>логику вычисления эффективных теплофизических свойств
 * при текущей температуре.</li>
 * </ul>
 *
 * <p>
 * В данной реализации теплопроводность и теплоёмкость
 * выбираются по бинарной схеме:
 * ячейка считается талой или мёрзлой в зависимости от сравнения
 * текущей температуры с температурой замерзания материала.
 *
 * <p>
 * Контекст является изменяемым и не потокобезопасен.
 * Он предназначен для использования одним солвером
 * в рамках одного расчёта.
 */
public class DirectCaseContext implements CaseContext {

    private int cellCount;

    private Grid3D grid;

    /** Индекс материала по ячейке. */
    private int[] materialIdByCell;

    /** Текущая температура по ячейке, °C. */
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
     * Создаёт начальное runtime-состояние расчёта
     * на основе расчётного случая.
     *
     * <p>
     * Конструктор подготавливает кеш параметров материалов
     * по всем ячейкам, чтобы сократить число обращений к библиотеке
     * материалов в горячих циклах солвера.
     *
     * @param c расчётный случай
     * @throws NullPointerException  если один из обязательных элементов
     *                               расчётного случая равен {@code null}
     * @throws IllegalStateException если размеры полей не совпадают
     *                               с размером расчётной сетки
     */
    public DirectCaseContext(SimulationCase c) {
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
        this.cellCount = (int) cellCountLong;

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

    @Override
    public Grid3D grid() {
        return grid;
    }

    @Override
    public int idx(int x, int y, int z) {
        return grid.index(x, y, z);
    }

    // -------------------------------------------------------------------------
    // Материал
    // -------------------------------------------------------------------------

    @Override
    public int materialId(int x, int y, int z) {
        return materialIdByCell[idx(x, y, z)];
    }

    @Override
    public double thermalConductivity(int x, int y, int z) {
        int idx = idx(x, y, z);
        if (isFrozen(idx)) {
            return thermalConductivityFrozenByCell[idx];
        } else {
            return thermalConductivityThawedByCell[idx];
        }
    }

    @Override
    public double heatCapacity(int x, int y, int z) {
        int idx = idx(x, y, z);
        if (isFrozen(idx)) {
            return heatCapacityFrozenByCell[idx];
        } else {
            return heatCapacityThawedByCell[idx];
        }
    }

    @Override
    public double phaseTransitionsHeat(int x, int y, int z) {
        return phaseTransitionsHeatByCell[idx(x, y, z)];
    }

    @Override
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
    @Override
    public double temperatureC(int x, int y, int z) {
        return temperatureCByCell[idx(x, y, z)];
    }

    /**
     * Заменяет текущее температурное поле новым состоянием.
     *
     * <p>
     * После вызова этого метода все зависящие от температуры
     * эффективные свойства ячеек будут вычисляться уже
     * по новому температурному полю.
     *
     * @param newTemperature новое температурное поле, °C
     * @throws NullPointerException  если {@code newTemperature == null}
     * @throws IllegalStateException если длина массива не совпадает
     *                               с числом ячеек расчётной сетки
     */
    @Override
    public void setNewTemperature(double[] newTemperature) {
        Objects.requireNonNull(newTemperature, "newTemperature");
        if (newTemperature.length != cellCount) {
            throw new IllegalStateException(
                    "newTemperature length mismatch: expected " + cellCount + ", actual "
                            + newTemperature.length);
        }
        this.temperatureCByCell = newTemperature;
    }

    /**
     * Возвращает текущее температурное поле.
     *
     * <p>
     * <b>Важно:</b> возвращается внутренняя ссылка на массив.
     * Это сделано для производительности в численных расчётах.
     * Изменение массива снаружи напрямую изменяет состояние контекста.
     *
     * @return текущее температурное поле, °C
     */
    @Override
    public double[] currentTemperatureByCell() {
        return temperatureCByCell;
    }

    // -------------------------------------------------------------------------
    // Быстрый доступ по линейному индексу
    // -------------------------------------------------------------------------

    @Override
    public double thermalConductivity(int idx) {
        if (isFrozen(idx)) {
            return thermalConductivityFrozenByCell[idx];
        } else {
            return thermalConductivityThawedByCell[idx];
        }
    }

    @Override
    public double heatCapacity(int idx) {
        if (isFrozen(idx)) {
            return heatCapacityFrozenByCell[idx];
        } else {
            return heatCapacityThawedByCell[idx];
        }
    }

    @Override
    public double phaseTransitionsHeat(int cellIndex) {
        return phaseTransitionsHeatByCell[cellIndex];
    }

    @Override
    public double freezingTemperature(int cellIndex) {
        return freezingTemperatureByCell[cellIndex];
    }

    @Override
    public double temperatureC(int cellIndex) {
        return temperatureCByCell[cellIndex];
    }

    private boolean isFrozen(int idx) {
        double t = temperatureC(idx);
        double tFreezing = freezingTemperature(idx);
        return t <= tFreezing;
    }
}