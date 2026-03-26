package io.github.timurpechenkin.solver.context;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.EnumMap;
import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.bc.BoundaryCondition;
import io.github.timurpechenkin.domain.bc.BoundaryConditionLibrary;
import io.github.timurpechenkin.domain.bc.BoundaryConditionType;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.material.Material;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.temperature.TemperatureField;
import io.github.timurpechenkin.geometry.Axis3D;
import io.github.timurpechenkin.geometry.Face;

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
 * при текущей температуре;</li>
 * <li>текущее модельное время;</li>
 * <li>доступ к параметрам граничных условий на внешних гранях области.</li>
 * </ul>
 *
 * <p>
 * В данной реализации теплопроводность и объёмная теплоёмкость
 * выбираются по бинарной схеме:
 * ячейка считается талой или мёрзлой в зависимости от сравнения
 * текущей температуры с температурой замерзания материала.
 *
 * <p>
 * Параметры граничных условий могут зависеть от месяца года.
 * Контекст возвращает значения, соответствующие текущему времени контекста.
 *
 * <p>
 * Контекст является изменяемым и не потокобезопасен.
 * Он предназначен для использования одним солвером
 * в рамках одного расчёта.
 */
public class DirectCaseContext implements CaseContext {

    private final LocalDateTime startDate;

    private LocalDateTime currentDate;

    private int cellCount;

    private Grid3D grid;

    /** Идентификатор материала по ячейке. */
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

    private EnumMap<Face, BoundaryConditionType[]> typeByCellFaceMap = new EnumMap<>(Face.class);
    private EnumMap<Face, int[]> bcIdFaceMap = new EnumMap<>(Face.class);
    private BoundaryConditionLibrary bcLibrary;

    private EnumMap<Axis3D, double[]> cellSideAxis3dMap = new EnumMap<>(Axis3D.class);
    private EnumMap<Axis3D, double[]> areaNormalAxisMap = new EnumMap<>(Axis3D.class);
    private double[] volumeByCell;

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

        this.startDate = Objects.requireNonNull(c.time().startDate(), "startDate");
        this.currentDate = startDate;

        this.grid = Objects.requireNonNull(c.grid(), "grid");

        MaterialField materialField = Objects.requireNonNull(c.materialField(), "materialField");
        MaterialLibrary materialLibrary = Objects.requireNonNull(c.materialLibrary(), "materialLibrary");
        this.materialIdByCell = Objects.requireNonNull(materialField.materialIdByCell(), "materialIdByCell");

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
                    "materialIdByCell length mismatch: expected " + cellCount + ", actual "
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
            Material material = materialLibrary.getById(materialId);

            thermalConductivityThawedByCell[i] = material.thermalConductivityThawed();
            thermalConductivityFrozenByCell[i] = material.thermalConductivityFrozen();
            heatCapacityThawedByCell[i] = material.heatCapacityThawed();
            heatCapacityFrozenByCell[i] = material.heatCapacityFrozen();
            phaseTransitionsHeatByCell[i] = material.phaseTransitionsHeat();
            freezingTemperatureByCell[i] = material.freezingTemperature();
        }

        // Граничные условия
        this.bcLibrary = c.bcLibrary();
        for (Face face : Face.values()) {
            int[] faceBcId = c.bcField().raw(face);
            int expected = (int) grid.faceGrid(face).cellCount();
            if (faceBcId.length != expected) {
                throw new IllegalStateException(
                        "Boundary condition array length mismatch for face " + face +
                                ": expected " + expected + ", actual " + faceBcId.length);
            }
            BoundaryConditionType[] typeByCell = new BoundaryConditionType[faceBcId.length];
            for (int i = 0; i < faceBcId.length; i++) {
                BoundaryCondition condition = bcLibrary.getById(faceBcId[i]);
                typeByCell[i] = condition.type();
            }

            typeByCellFaceMap.put(face, typeByCell);
            bcIdFaceMap.put(face, faceBcId);
        }

        // Геометрия
        for (Axis3D axis3d : Axis3D.values()) {
            double[] sellSide = new double[cellCount];
            for (int i = 0; i < cellCount; i++) {
                int[] position3d = grid.position(i);
                int p1d = switch (axis3d) {
                    case X -> position3d[0];
                    case Y -> position3d[1];
                    case Z -> position3d[2];
                };
                sellSide[i] = grid.axis(axis3d).stepMeters(p1d);
            }
            cellSideAxis3dMap.put(axis3d, sellSide);
        }

        this.volumeByCell = new double[cellCount];
        for (int i = 0; i < cellCount; i++) {
            double sideX = cellSideAxis3dMap.get(Axis3D.X)[i];
            double sideY = cellSideAxis3dMap.get(Axis3D.Y)[i];
            double sideZ = cellSideAxis3dMap.get(Axis3D.Z)[i];
            volumeByCell[i] = sideX * sideY * sideZ;
        }

        for (Axis3D axis : Axis3D.values()) {
            double[] area = new double[cellCount];
            for (int i = 0; i < cellCount; i++) {
                double sideX = cellSideAxis3dMap.get(Axis3D.X)[i];
                double sideY = cellSideAxis3dMap.get(Axis3D.Y)[i];
                double sideZ = cellSideAxis3dMap.get(Axis3D.Z)[i];
                area[i] = switch (axis) {
                    case X -> sideY * sideZ;
                    case Y -> sideX * sideZ;
                    case Z -> sideX * sideY;
                };
            }
            areaNormalAxisMap.put(axis, area);
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
    // ТЕПЛОФИЗИЧЕСКИЕ СВОЙСТВА
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
    public double volumetricHeatCapacity(int x, int y, int z) {
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

    @Override
    public double thermalConductivity(int index) {
        if (isFrozen(index)) {
            return thermalConductivityFrozenByCell[index];
        } else {
            return thermalConductivityThawedByCell[index];
        }
    }

    @Override
    public double volumetricHeatCapacity(int index) {
        if (isFrozen(index)) {
            return heatCapacityFrozenByCell[index];
        } else {
            return heatCapacityThawedByCell[index];
        }
    }

    @Override
    public double phaseTransitionsHeat(int index) {
        return phaseTransitionsHeatByCell[index];
    }

    @Override
    public double freezingTemperature(int index) {
        return freezingTemperatureByCell[index];
    }

    // -------------------------------------------------------------------------
    // ТЕМПЕРАТУРА
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
    public double temperatureC(int cellIndex) {
        return temperatureCByCell[cellIndex];
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

    private boolean isFrozen(int idx) {
        double t = temperatureC(idx);
        double tFreezing = freezingTemperature(idx);
        return t <= tFreezing;
    }

    // -------------------------------------------------------------------------
    // ВРЕМЯ
    // -------------------------------------------------------------------------

    @Override
    public LocalDateTime getStartDate() {
        return startDate;
    }

    @Override
    public LocalDateTime getCurrentDate() {
        return currentDate;
    }

    @Override
    public void setCurrentTime(long seconds) {
        currentDate = startDate.plusSeconds(seconds);
    }

    // -------------------------------------------------------------------------
    // ГРАНИЧНЫЕ УСЛОВИЯ
    // -------------------------------------------------------------------------

    @Override
    public boolean hasBoundaryCondition(int x, int y, int z, Face face) {
        return switch (face) {
            case X_MAX -> x == grid.n(Axis3D.X) - 1;
            case X_MIN -> x == 0;
            case Y_MAX -> y == grid.n(Axis3D.Y) - 1;
            case Y_MIN -> y == 0;
            case Z_MAX -> z == grid.n(Axis3D.Z) - 1;
            case Z_MIN -> z == 0;
        };
    }

    @Override
    public BoundaryConditionType boundaryConditionType(int x, int y, int z, Face face) {
        if (hasBoundaryCondition(x, y, z, face)) {
            return typeByCellFaceMap.get(face)[position3dToIndex2d(x, y, z, face)];
        }
        throw new IllegalArgumentException(
                "There is no boundary condition near position = (" + x + ", " + y + ", " + z + ")");
    }

    @Override
    public double boundaryTemperatureC(int x, int y, int z, Face face) {
        Month currentMonth = currentDate.getMonth();
        return getBoundaryCondition(x, y, z, face).temperature().get(currentMonth);
    }

    @Override
    public double boundaryHeatFlux(int x, int y, int z, Face face) {
        Month currentMonth = currentDate.getMonth();
        return getBoundaryCondition(x, y, z, face).heatFlux().get(currentMonth);
    }

    @Override
    public double boundaryAmbientTemperatureC(int x, int y, int z, Face face) {
        Month currentMonth = currentDate.getMonth();
        return getBoundaryCondition(x, y, z, face).ambientTemperature().get(currentMonth);
    }

    @Override
    public double boundaryHeatTransferCoeff(int x, int y, int z, Face face) {
        Month currentMonth = currentDate.getMonth();
        return getBoundaryCondition(x, y, z, face).heatTransferCoefficient().get(currentMonth);
    }

    private BoundaryCondition getBoundaryCondition(int x, int y, int z, Face face) {
        if (hasBoundaryCondition(x, y, z, face)) {
            int bcId = bcIdFaceMap.get(face)[position3dToIndex2d(x, y, z, face)];
            return bcLibrary.getById(bcId);
        }
        throw new IllegalArgumentException(
                "There is no boundary condition near position = (" + x + ", " + y + ", " + z + ")");
    }

    /**
     * Переводит позицию в трехмерной сетке в индекс одномерного массива для
     * двухмерной плоскости
     * 
     * @param x
     * @param y
     * @param z
     * @param face
     * @return индекс массива для двухмерной плоскости
     */
    private int position3dToIndex2d(int x, int y, int z, Face face) {
        return switch (face) {
            case X_MAX, X_MIN -> grid.faceGrid(face).index(y, z);
            case Y_MAX, Y_MIN -> grid.faceGrid(face).index(x, z);
            case Z_MAX, Z_MIN -> grid.faceGrid(face).index(x, y);
        };
    }

    // ------------------------------------------------
    // ГЕОМЕТРИЯ
    // ------------------------------------------------

    @Override
    public double cellSideMeters(int x, int y, int z, Axis3D axis3d) {
        int index = idx(x, y, z);
        return cellSideAxis3dMap.get(axis3d)[index];
    }

    @Override
    public double areaNormalToAxisMeters2(int x, int y, int z, Axis3D axis3d) {
        int index = idx(x, y, z);
        return areaNormalAxisMap.get(axis3d)[index];
    }

    @Override
    public double volumeMeters3(int x, int y, int z) {
        int index = idx(x, y, z);
        return volumeByCell[index];
    }

    @Override
    public double cellSideMeters(int index, Axis3D axis3d) {
        return cellSideAxis3dMap.get(axis3d)[index];
    }

    @Override
    public double areaNormalToAxisMeters2(int index, Axis3D axis3d) {
        return areaNormalAxisMap.get(axis3d)[index];
    }

    @Override
    public double volumeMeters3(int index) {
        return volumeByCell[index];
    }
}