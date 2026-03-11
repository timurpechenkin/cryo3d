
package io.github.timurpechenkin.solver.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.measurement.Profile;
import io.github.timurpechenkin.domain.measurement.SamplePoint;

/**
 * Накопитель результата расчёта.
 *
 * <p>
 * Предназначен для пошагового заполнения истории температур
 * в контрольных точках и на профилях в процессе работы солвера.
 *
 * <p>
 * Сценарий использования:
 * <ol>
 * <li>создать аккумулятор с известным числом сохраняемых шагов;</li>
 * <li>на каждом шаге вызвать {@link #recordStep(int, double, double[])},
 * передав текущее время и полное 3D-поле температур;</li>
 * <li>после завершения расчёта вызвать {@link #build()}.</li>
 * </ol>
 *
 * <p>
 * Аккумулятор хранит только историю по точкам и профилям.
 * Полное 3D-поле температур внутри результата не сохраняется.
 */
public final class CaseResultAccumulator {

    private final String caseName;
    private final List<SamplePoint> samplePoints;
    private final List<Profile> profiles;

    private final double[] timeSeconds;
    private final double[][] pointTemperatureByPointAndStep;
    private final double[][][] profileTemperatureByProfileAndStepAndCell;

    private final int totalSteps;
    private int recordedSteps;

    /**
     * Создаёт накопитель результата.
     *
     * @param simulationCase расчётный случай
     * @param totalSteps     число сохраняемых состояний
     * @throws IllegalArgumentException если {@code totalSteps <= 0}
     */
    public CaseResultAccumulator(SimulationCase simulationCase, int totalSteps) {
        Objects.requireNonNull(simulationCase, "simulationCase");
        if (totalSteps <= 0) {
            throw new IllegalArgumentException("totalSteps must be > 0");
        }

        this.caseName = simulationCase.caseName();
        this.samplePoints = List.copyOf(simulationCase.samplePoints());
        this.profiles = List.copyOf(simulationCase.profiles());

        this.totalSteps = totalSteps;
        this.recordedSteps = 0;

        this.timeSeconds = new double[totalSteps];

        this.pointTemperatureByPointAndStep = new double[samplePoints.size()][totalSteps];

        this.profileTemperatureByProfileAndStepAndCell = new double[profiles.size()][][];
        for (int p = 0; p < profiles.size(); p++) {
            Profile profile = profiles.get(p);
            int profileCellCount = profile.cellIndex().length;
            this.profileTemperatureByProfileAndStepAndCell[p] = new double[totalSteps][profileCellCount];
        }
    }

    /**
     * Сохраняет состояние системы на шаге.
     *
     * <p>
     * Из полного 3D-поля температур извлекаются значения:
     * <ul>
     * <li>в контрольных точках;</li>
     * <li>во всех ячейках профилей.</li>
     * </ul>
     *
     * @param step               индекс сохраняемого шага
     * @param timeSeconds        время шага, сек
     * @param temperatureCByCell полное поле температур 3D-сетки, °C
     *
     * @throws IndexOutOfBoundsException если {@code step} вне диапазона
     * @throws IllegalArgumentException  если поле температур равно null
     */
    public void recordStep(int step, double timeSeconds, double[] temperatureCByCell) {
        if (step < 0 || step >= totalSteps) {
            throw new IndexOutOfBoundsException("Step out of range: " + step);
        }
        Objects.requireNonNull(temperatureCByCell, "temperatureCByCell");

        this.timeSeconds[step] = timeSeconds;

        // Точки
        for (int p = 0; p < samplePoints.size(); p++) {
            SamplePoint point = samplePoints.get(p);
            int cellIndex = point.cellIndex();
            pointTemperatureByPointAndStep[p][step] = temperatureCByCell[cellIndex];
        }

        // Профили
        for (int p = 0; p < profiles.size(); p++) {
            Profile profile = profiles.get(p);
            int[] profileCellIndex = profile.cellIndex();
            double[] profileState = profileTemperatureByProfileAndStepAndCell[p][step];

            for (int i = 0; i < profileCellIndex.length; i++) {
                profileState[i] = temperatureCByCell[profileCellIndex[i]];
            }
        }

        if (step + 1 > recordedSteps) {
            recordedSteps = step + 1;
        }
    }

    /**
     * Возвращает число уже записанных шагов.
     */
    public int recordedSteps() {
        return recordedSteps;
    }

    /**
     * Возвращает общее число шагов, предусмотренных в результате.
     */
    public int totalSteps() {
        return totalSteps;
    }

    /**
     * Собирает итоговый результат расчёта.
     *
     * <p>
     * Если были записаны не все шаги, в результат всё равно попадут
     * массивы полной длины. Предполагается, что вызывающий код сам контролирует,
     * какие шаги были реально сохранены.
     *
     * @return итоговый результат расчёта
     */
    public CaseResult build() {
        List<SamplePointSeries> pointSeries = new ArrayList<>(samplePoints.size());
        for (int p = 0; p < samplePoints.size(); p++) {
            pointSeries.add(new SamplePointSeries(
                    samplePoints.get(p),
                    pointTemperatureByPointAndStep[p]));
        }

        List<ProfileSeries> profileSeries = new ArrayList<>(profiles.size());
        for (int p = 0; p < profiles.size(); p++) {
            profileSeries.add(new ProfileSeries(
                    profiles.get(p),
                    profileTemperatureByProfileAndStepAndCell[p]));
        }

        return new CaseResult(
                caseName,
                timeSeconds,
                pointSeries,
                profileSeries);
    }
}