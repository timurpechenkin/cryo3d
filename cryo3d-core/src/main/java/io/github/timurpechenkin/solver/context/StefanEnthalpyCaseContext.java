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
 * Энтальпийная реализация {@link CaseContext} для решения задачи Стефана.
 *
 * <p>
 * Основной расчётной величиной является энтальпия ячейки, [Дж/м³].
 * Температура и эффективные теплофизические свойства материала
 * восстанавливаются из энтальпии.
 *
 * <p>
 * В зоне фазового перехода:
 * <ul>
 * <li>температура фиксируется на {@code Tbf};</li>
 * <li>доля талой фазы изменяется от 0 до 1;</li>
 * <li>теплопроводность и объёмная теплоёмкость
 * интерполируются линейно по доле талой фазы.</li>
 * </ul>
 */
public final class StefanEnthalpyCaseContext implements CaseContext {

    private final LocalDateTime startDate;
    private LocalDateTime currentDate;

    private final int cellCount;
    private final Grid3D grid;

    /** Идентификатор материала по ячейке. */
    private final int[] materialIdByCell;

    /** Температура по ячейке, [°C]. Производная от энтальпии. */
    private double[] temperatureCByCell;

    /** Энтальпия по ячейке, [Дж/м³]. Основная расчётная величина. */
    private double[] enthalpyByCell;

    private final double[] thermalConductivityThawedByCell;
    private final double[] thermalConductivityFrozenByCell;
    private final double[] heatCapacityThawedByCell;
    private final double[] heatCapacityFrozenByCell;
    private final double[] phaseTransitionsHeatByCell;
    private final double[] freezingTemperatureByCell;

    private final EnumMap<Face, BoundaryConditionType[]> typeByCellFaceMap = new EnumMap<>(Face.class);
    private final EnumMap<Face, int[]> bcIdByFaceMap = new EnumMap<>(Face.class);
    private final BoundaryConditionLibrary bcLibrary;

    private final EnumMap<Axis3D, double[]> cellSideByAxisMap = new EnumMap<>(Axis3D.class);
    private final EnumMap<Axis3D, double[]> areaNormalByAxisMap = new EnumMap<>(Axis3D.class);
    private final double[] volumeByCell;

    public StefanEnthalpyCaseContext(SimulationCase simulationCase) {
        Objects.requireNonNull(simulationCase, "simulationCase");

        this.startDate = requireStartDate(simulationCase);
        this.currentDate = startDate;
        this.grid = requireGrid(simulationCase);
        this.cellCount = resolveCellCount(grid);

        this.materialIdByCell = requireMaterialIds(simulationCase);
        this.temperatureCByCell = requireTemperatureField(simulationCase);

        validateCellArrayLengths();

        this.thermalConductivityThawedByCell = new double[cellCount];
        this.thermalConductivityFrozenByCell = new double[cellCount];
        this.heatCapacityThawedByCell = new double[cellCount];
        this.heatCapacityFrozenByCell = new double[cellCount];
        this.phaseTransitionsHeatByCell = new double[cellCount];
        this.freezingTemperatureByCell = new double[cellCount];

        this.bcLibrary = requireBoundaryConditionLibrary(simulationCase);
        initMaterialCache(simulationCase);
        initBoundaryConditions(simulationCase);

        this.volumeByCell = new double[cellCount];
        initGeometry();

        // Начальная энтальпия из начальной температуры
        this.enthalpyByCell = new double[cellCount];
        for (int i = 0; i < cellCount; i++) {
            enthalpyByCell[i] = enthalpyFromTemperature(i, temperatureCByCell[i]);
        }
    }

    private static LocalDateTime requireStartDate(SimulationCase simulationCase) {
        return Objects.requireNonNull(simulationCase.time().startDate(), "startDate");
    }

    private static Grid3D requireGrid(SimulationCase simulationCase) {
        return Objects.requireNonNull(simulationCase.grid(), "grid");
    }

    private static int resolveCellCount(Grid3D grid) {
        long cellCountLong = grid.cellCount();
        if (cellCountLong > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "Grid is too large for int[]/double[] solver arrays: cellCount=" + cellCountLong);
        }
        return (int) cellCountLong;
    }

    private static int[] requireMaterialIds(SimulationCase simulationCase) {
        MaterialField materialField = Objects.requireNonNull(simulationCase.materialField(), "materialField");
        return Objects.requireNonNull(materialField.materialIdByCell(), "materialIdByCell");
    }

    private static double[] requireTemperatureField(SimulationCase simulationCase) {
        TemperatureField temperatureField = Objects.requireNonNull(simulationCase.temperatureField(),
                "temperatureField");
        return Objects.requireNonNull(temperatureField.temperatureCByCell(), "temperatureCByCell");
    }

    private static BoundaryConditionLibrary requireBoundaryConditionLibrary(SimulationCase simulationCase) {
        return Objects.requireNonNull(simulationCase.bcLibrary(), "bcLibrary");
    }

    private void validateCellArrayLengths() {
        if (materialIdByCell.length != cellCount) {
            throw new IllegalStateException(
                    "materialIdByCell length mismatch: expected " + cellCount + ", actual " + materialIdByCell.length);
        }
        if (temperatureCByCell.length != cellCount) {
            throw new IllegalStateException(
                    "temperatureCByCell length mismatch: expected " + cellCount + ", actual "
                            + temperatureCByCell.length);
        }
    }

    private void initMaterialCache(SimulationCase simulationCase) {
        MaterialLibrary materialLibrary = Objects.requireNonNull(simulationCase.materialLibrary(), "materialLibrary");

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
    }

    private void initBoundaryConditions(SimulationCase simulationCase) {
        Objects.requireNonNull(simulationCase.bcField(), "bcField");

        for (Face face : Face.values()) {
            int[] bcIdByFaceCell = simulationCase.bcField().raw(face);
            validateBoundaryConditionArrayLength(face, bcIdByFaceCell);

            BoundaryConditionType[] typeByFaceCell = new BoundaryConditionType[bcIdByFaceCell.length];
            for (int i = 0; i < bcIdByFaceCell.length; i++) {
                BoundaryCondition condition = bcLibrary.getById(bcIdByFaceCell[i]);
                typeByFaceCell[i] = condition.type();
            }

            bcIdByFaceMap.put(face, bcIdByFaceCell);
            typeByCellFaceMap.put(face, typeByFaceCell);
        }
    }

    private void validateBoundaryConditionArrayLength(Face face, int[] bcIdByFaceCell) {
        int expected = (int) grid.faceGrid(face).cellCount();
        if (bcIdByFaceCell.length != expected) {
            throw new IllegalStateException(
                    "Boundary condition array length mismatch for face " + face
                            + ": expected " + expected + ", actual " + bcIdByFaceCell.length);
        }
    }

    private void initGeometry() {
        initCellSideByAxis();
        initVolumeByCell();
        initAreaNormalByAxis();
    }

    private void initCellSideByAxis() {
        for (Axis3D axis : Axis3D.values()) {
            double[] cellSideByCell = new double[cellCount];
            for (int i = 0; i < cellCount; i++) {
                int[] position = grid.position(i);
                int axisPosition = switch (axis) {
                    case X -> position[0];
                    case Y -> position[1];
                    case Z -> position[2];
                };
                cellSideByCell[i] = grid.axis(axis).stepMeters(axisPosition);
            }
            cellSideByAxisMap.put(axis, cellSideByCell);
        }
    }

    private void initVolumeByCell() {
        double[] sideX = cellSideByAxisMap.get(Axis3D.X);
        double[] sideY = cellSideByAxisMap.get(Axis3D.Y);
        double[] sideZ = cellSideByAxisMap.get(Axis3D.Z);

        for (int i = 0; i < cellCount; i++) {
            volumeByCell[i] = sideX[i] * sideY[i] * sideZ[i];
        }
    }

    private void initAreaNormalByAxis() {
        double[] sideX = cellSideByAxisMap.get(Axis3D.X);
        double[] sideY = cellSideByAxisMap.get(Axis3D.Y);
        double[] sideZ = cellSideByAxisMap.get(Axis3D.Z);

        for (Axis3D axis : Axis3D.values()) {
            double[] areaByCell = new double[cellCount];
            for (int i = 0; i < cellCount; i++) {
                areaByCell[i] = switch (axis) {
                    case X -> sideY[i] * sideZ[i];
                    case Y -> sideX[i] * sideZ[i];
                    case Z -> sideX[i] * sideY[i];
                };
            }
            areaNormalByAxisMap.put(axis, areaByCell);
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

    @Override
    public int materialId(int x, int y, int z) {
        return materialIdByCell[idx(x, y, z)];
    }

    @Override
    public double thermalConductivity(int x, int y, int z) {
        return thermalConductivity(idx(x, y, z));
    }

    @Override
    public double thermalConductivity(int cellIndex) {
        double f = thawedFraction(cellIndex);
        return (1.0 - f) * thermalConductivityFrozenByCell[cellIndex]
                + f * thermalConductivityThawedByCell[cellIndex];
    }

    @Override
    public double volumetricHeatCapacity(int x, int y, int z) {
        return volumetricHeatCapacity(idx(x, y, z));
    }

    @Override
    public double volumetricHeatCapacity(int cellIndex) {
        double f = thawedFraction(cellIndex);
        return (1.0 - f) * heatCapacityFrozenByCell[cellIndex]
                + f * heatCapacityThawedByCell[cellIndex];
    }

    @Override
    public double phaseTransitionsHeat(int x, int y, int z) {
        return phaseTransitionsHeat(idx(x, y, z));
    }

    @Override
    public double phaseTransitionsHeat(int cellIndex) {
        return phaseTransitionsHeatByCell[cellIndex];
    }

    @Override
    public double freezingTemperature(int x, int y, int z) {
        return freezingTemperature(idx(x, y, z));
    }

    @Override
    public double freezingTemperature(int cellIndex) {
        return freezingTemperatureByCell[cellIndex];
    }

    @Override
    public double temperatureC(int x, int y, int z) {
        return temperatureC(idx(x, y, z));
    }

    @Override
    public double temperatureC(int cellIndex) {
        return temperatureCByCell[cellIndex];
    }

    @Override
    public void setNewTemperature(double[] newTemperature) {
        throw new IllegalArgumentException("Method setNewTemperature is unsupported for StefanEnthalpyCaseContext");
        // Objects.requireNonNull(newTemperature, "newTemperature");
        // if (newTemperature.length != cellCount) {
        // throw new IllegalStateException(
        // "newTemperature length mismatch: expected " + cellCount + ", actual " +
        // newTemperature.length);
        // }

        // this.temperatureCByCell = newTemperature;

        // double[] newEnthalpy = new double[cellCount];
        // for (int i = 0; i < cellCount; i++) {
        // newEnthalpy[i] = enthalpyFromTemperature(i, this.temperatureCByCell[i]);
        // }
        // this.enthalpyByCell = newEnthalpy;
    }

    @Override
    public double[] currentTemperatureByCell() {
        return temperatureCByCell;
    }

    @Override
    public double enthalpy(int x, int y, int z) {
        return enthalpy(idx(x, y, z));
    }

    @Override
    public double enthalpy(int cellIndex) {
        return enthalpyByCell[cellIndex];
    }

    @Override
    public double[] currentEnthalpyByCell() {
        return enthalpyByCell;
    }

    @Override
    public void setNewEnthalpy(double[] newEnthalpy) {
        Objects.requireNonNull(newEnthalpy, "newEnthalpy");
        if (newEnthalpy.length != cellCount) {
            throw new IllegalStateException(
                    "newEnthalpy length mismatch: expected " + cellCount + ", actual " + newEnthalpy.length);
        }

        this.enthalpyByCell = newEnthalpy;
        this.temperatureCByCell = new double[cellCount];
        for (int i = 0; i < cellCount; i++) {
            this.temperatureCByCell[i] = temperatureFromEnthalpy(i, newEnthalpy[i]);
        }
    }

    @Override
    public double thawedFraction(int cellIndex) {
        double h = enthalpyByCell[cellIndex];
        double l = phaseTransitionsHeatByCell[cellIndex];

        if (h < 0.0) {
            return 0.0;
        }
        if (h > l) {
            return 1.0;
        }
        if (l == 0.0) {
            return 1.0;
        }
        return h / l;
    }

    @Override
    public double thawedFraction(int x, int y, int z) {
        int cellIndex = idx(x, y, z);
        return thawedFraction(cellIndex);
    }

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

    @Override
    public double cellSideMeters(int x, int y, int z, Axis3D axis3d) {
        return cellSideMeters(idx(x, y, z), axis3d);
    }

    @Override
    public double volumeMeters3(int x, int y, int z) {
        return volumeMeters3(idx(x, y, z));
    }

    @Override
    public double cellSideMeters(int index, Axis3D axis3d) {
        return cellSideByAxisMap.get(axis3d)[index];
    }

    @Override
    public double volumeMeters3(int index) {
        return volumeByCell[index];
    }

    @Override
    public double areaNormalToAxisMeters2(int index, Axis3D axis3d) {
        return areaNormalByAxisMap.get(axis3d)[index];
    }

    @Override
    public double areaNormalToAxisMeters2(int x, int y, int z, Axis3D axis3d) {
        int index = idx(x, y, z);
        return areaNormalByAxisMap.get(axis3d)[index];
    }

    private double enthalpyFromTemperature(int cellIndex, double temperatureC) {
        double tbf = freezingTemperatureByCell[cellIndex];
        double cFrozen = heatCapacityFrozenByCell[cellIndex];
        double cThawed = heatCapacityThawedByCell[cellIndex];
        double l = phaseTransitionsHeatByCell[cellIndex];

        if (temperatureC < tbf) {
            return cFrozen * (temperatureC - tbf);
        }
        if (temperatureC > tbf) {
            return l + cThawed * (temperatureC - tbf);
        }

        return 0.0;
    }

    private double temperatureFromEnthalpy(int cellIndex, double enthalpy) {
        double tbf = freezingTemperatureByCell[cellIndex];
        double cFrozen = heatCapacityFrozenByCell[cellIndex];
        double cThawed = heatCapacityThawedByCell[cellIndex];
        double l = phaseTransitionsHeatByCell[cellIndex];

        if (enthalpy < 0.0) {
            return tbf + enthalpy / cFrozen;
        }
        if (enthalpy <= l) {
            return tbf;
        }
        return tbf + (enthalpy - l) / cThawed;
    }

    private BoundaryCondition getBoundaryCondition(int x, int y, int z, Face face) {
        if (hasBoundaryCondition(x, y, z, face)) {
            int bcId = bcIdByFaceMap.get(face)[position3dToIndex2d(x, y, z, face)];
            return bcLibrary.getById(bcId);
        }
        throw new IllegalArgumentException(
                "There is no boundary condition near position = (" + x + ", " + y + ", " + z + ")");
    }

    private int position3dToIndex2d(int x, int y, int z, Face face) {
        return switch (face) {
            case X_MAX, X_MIN -> grid.faceGrid(face).index(y, z);
            case Y_MAX, Y_MIN -> grid.faceGrid(face).index(x, z);
            case Z_MAX, Z_MIN -> grid.faceGrid(face).index(x, y);
        };
    }
}