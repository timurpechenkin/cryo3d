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
     * Создаёт solver-ориентированное представление задачи
     * и подготавливает кеш параметров по всем ячейкам.
     *
     * @param c задача моделирования
     * @throws NullPointerException  если один из обязательных элементов задачи
     *                               равен null
     * @throws IllegalStateException если размеры массивов полей не совпадают с
     *                               размером сетки
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
            return thermalConductivityFrozenByCell[idx];
        } else {
            return thermalConductivityThawedByCell[idx];
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
        if (t > tFreezing) {
            return false;
        } else {
            return true;
        }
    }
}