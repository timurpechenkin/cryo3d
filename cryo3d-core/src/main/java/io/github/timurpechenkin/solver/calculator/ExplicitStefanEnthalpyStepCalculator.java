package io.github.timurpechenkin.solver.calculator;

import io.github.timurpechenkin.domain.bc.BoundaryConditionType;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.geometry.Axis3D;
import io.github.timurpechenkin.geometry.Face;
import io.github.timurpechenkin.solver.context.CaseContext;

/**
 * Явная конечно-объёмная схема одного шага для задачи Стефана
 * в энтальпийной формулировке.
 *
 * <p>
 * Схема решает уравнение теплопроводности в трёхмерной структурированной сетке
 * через баланс тепловых потоков по шести граням ячейки. В качестве основной
 * расчётной величины использует энтальпию:
 *
 * <pre>
 * H_new = H_old + dt / V * Σ(q_face * A_face)
 * </pre>
 *
 * где:
 * <ul>
 * <li>{@code H} — объёмная энтальпия ячейки, [Дж/м³]</li>
 * <li>{@code V} — объём ячейки, [м³]</li>
 * <li>{@code q_face} — плотность теплового потока через грань, [Вт/м²]</li>
 * <li>{@code A_face} — площадь грани, [м²]</li>
 * </ul>
 *
 * <p>
 * После обновления энтальпии контекст сам восстанавливает температуру
 * и эффективные теплофизические свойства материала.
 *
 * <p>
 * Для внутренних граней поток вычисляется по разности температур между
 * соседними ячейками. Для внешних граней применяются граничные условия:
 * <ul>
 * <li>I рода: задана температура на границе</li>
 * <li>II рода: задана плотность теплового потока</li>
 * <li>III рода: задана температура внешней среды и коэффициент теплообмена</li>
 * </ul>
 *
 * <p>
 * <b>Принятое соглашение по знаку потока для II рода:</b>
 * положительный {@code q} означает поток тепла <b>внутрь расчётной области</b>.
 *
 * <p>
 * Схема является явной и условно устойчивой.
 * Корректный шаг по времени должен обеспечиваться постановкой задачи
 * или внешней валидацией.
 */
public final class ExplicitStefanEnthalpyStepCalculator implements StepCalculator {

    @Override
    public void calculateStep(CaseContext context, long dtSeconds, long currentTime) {
        context.setCurrentTime(currentTime);

        Grid3D grid = context.grid();
        int nx = grid.n(Axis3D.X);
        int ny = grid.n(Axis3D.Y);
        int nz = grid.n(Axis3D.Z);

        double[] currentTemperature = context.currentTemperatureByCell();
        double[] currentEnthalpy = context.currentEnthalpyByCell();
        double[] nextEnthalpy = new double[currentEnthalpy.length];

        for (int z = 0; z < nz; z++) {
            for (int y = 0; y < ny; y++) {
                for (int x = 0; x < nx; x++) {
                    int i = context.idx(x, y, z);

                    double heatRateIn = 0.0;
                    for (Face face : Face.values()) {
                        heatRateIn += faceHeatRate(context, currentTemperature, x, y, z, face);
                    }

                    double volume = context.volumeMeters3(i);
                    nextEnthalpy[i] = currentEnthalpy[i] + (dtSeconds * heatRateIn) / volume;
                }
            }
        }

        context.setNewEnthalpy(nextEnthalpy);
    }

    private double faceHeatRate(
            CaseContext context,
            double[] currentTemperature,
            int x, int y, int z,
            Face face) {

        Axis3D normal = switch (face) {
            case X_MAX, X_MIN -> Axis3D.X;
            case Y_MAX, Y_MIN -> Axis3D.Y;
            case Z_MAX, Z_MIN -> Axis3D.Z;
        };

        if (!context.hasBoundaryCondition(x, y, z, face)) {
            int i = context.idx(x, y, z);
            int j = switch (face) {
                case X_MAX -> context.idx(x + 1, y, z);
                case X_MIN -> context.idx(x - 1, y, z);
                case Y_MAX -> context.idx(x, y + 1, z);
                case Y_MIN -> context.idx(x, y - 1, z);
                case Z_MAX -> context.idx(x, y, z + 1);
                case Z_MIN -> context.idx(x, y, z - 1);
            };

            double tCell = currentTemperature[i];
            double tNb = currentTemperature[j];

            double side = context.cellSideMeters(i, normal);
            double sideNb = context.cellSideMeters(j, normal);

            double lambdaCell = context.thermalConductivity(i);
            double lambdaNb = context.thermalConductivity(j);

            double distance = 0.5 * side + 0.5 * sideNb;
            double lambdaFace = harmonicMean(lambdaCell, 0.5 * side, lambdaNb, 0.5 * sideNb);
            double q = lambdaFace * (tNb - tCell) / distance;

            double area = context.areaNormalToAxisMeters2(i, normal);
            return q * area;
        }

        return boundaryHeatRate(context, x, y, z, face, normal, currentTemperature[context.idx(x, y, z)]);
    }

    /**
     * Возвращает тепловую мощность через внешнюю грань ячейки, [Вт].
     *
     * <p>
     * Положительное значение означает приток тепла в ячейку.
     */
    private double boundaryHeatRate(
            CaseContext context,
            int x, int y, int z,
            Face face,
            Axis3D normal,
            double tCell) {

        int i = context.idx(x, y, z);

        double area = context.areaNormalToAxisMeters2(i, normal);
        double dNormal = context.cellSideMeters(i, normal);
        double lambdaCell = context.thermalConductivity(i);

        BoundaryConditionType type = context.boundaryConditionType(x, y, z, face);

        return switch (type) {
            case FIRST_KIND -> {
                double tBoundary = context.boundaryTemperatureC(x, y, z, face);
                double q = lambdaCell * (tBoundary - tCell) / (0.5 * dNormal);
                yield q * area;
            }
            case SECOND_KIND -> {
                double q = context.boundaryHeatFlux(x, y, z, face);
                yield q * area;
            }
            case THIRD_KIND -> {
                double tAmbient = context.boundaryAmbientTemperatureC(x, y, z, face);
                double alpha = context.boundaryHeatTransferCoeff(x, y, z, face);
                double q = alpha * (tAmbient - tCell);
                yield q * area;
            }
        };
    }

    /**
     * Гармоническое среднее теплопроводности на внутренней грани.
     *
     * <p>
     * Используется для корректного вычисления потока между двумя соседними
     * ячейками, особенно если их теплопроводности различаются.
     */
    private double harmonicMean(double lambda1, double d1, double lambda2, double d2) {
        return (d1 + d2) / (d1 / lambda1 + d2 / lambda2);
    }
}