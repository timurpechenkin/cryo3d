
package io.github.timurpechenkin.solver.recording;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.recording.Profile;
import io.github.timurpechenkin.domain.recording.SamplePoint;

/**
 * Накопитель результата расчёта.
 *
 * <p>
 * Предназначен для пошагового накопления истории температур
 * в контрольных точках и на профилях в процессе работы solver-а.
 *
 * <p>
 * Аккумулятор принимает состояние на каждом шаге расчёта,
 * но сохраняет кадры только для тех шагов, которые кратны
 * индивидуальному {@code saveStep} соответствующей точки или профиля.
 *
 * <p>
 * Шаг {@code 0} трактуется как начальное состояние расчёта
 * и также может быть сохранён.
 *
 * <p>
 * Аккумулятор хранит только историю по контрольным точкам и профилям.
 * Полное 3D-поле температур в результирующий объект не включается.
 */
public final class RecordingAccumulator {

    private final int maxStepIndex;

    private final List<SamplePoint> samplePoints;
    private final List<Profile> profiles;

    private final int[] profileSaveSteps;
    private final int[] pointSaveSteps;
    private final int[] profileStep;
    private final int[] pointStep;

    private final TemperatureFrame1D[][] pointTemperatureFrames;
    private final TemperatureFrame2D[][] profileTemperatureFrames;

    /**
     * Создаёт накопитель результата.
     *
     * @param simulationCase расчётный случай
     * @param steps          число шагов в расчёте состояний
     * @throws IllegalArgumentException если {@code steps <= 0}
     */
    public RecordingAccumulator(SimulationCase simulationCase, int steps) {
        Objects.requireNonNull(simulationCase, "simulationCase");
        if (steps <= 0) {
            throw new IllegalArgumentException("totalSteps must be > 0");
        }
        this.maxStepIndex = steps;
        this.samplePoints = List.copyOf(simulationCase.samplePoints());
        this.profiles = List.copyOf(simulationCase.profiles());

        this.profileSaveSteps = new int[profiles.size()];
        this.profileStep = new int[profiles.size()];
        this.pointSaveSteps = new int[samplePoints.size()];
        this.pointStep = new int[samplePoints.size()];

        for (int i = 0; i < profiles.size(); i++) {
            Profile p = profiles.get(i);
            if (p.saveStep() <= 0) {
                throw new IllegalArgumentException("Profile saveStep must be > 0: profile=" + p.name());
            }
            profileSaveSteps[i] = p.saveStep();
            profileStep[i] = 0;
        }

        for (int i = 0; i < samplePoints.size(); i++) {
            SamplePoint s = samplePoints.get(i);
            if (s.saveStep() <= 0) {
                throw new IllegalArgumentException("Sample point saveStep must be > 0: sample point=" + s.name());
            }
            pointSaveSteps[i] = s.saveStep();
            pointStep[i] = 0;
        }

        this.profileTemperatureFrames = new TemperatureFrame2D[profiles.size()][];
        for (int i = 0; i < profileTemperatureFrames.length; i++) {
            int frameCount = calculateFrameCount(steps, profileSaveSteps[i]);
            profileTemperatureFrames[i] = new TemperatureFrame2D[frameCount];
        }

        this.pointTemperatureFrames = new TemperatureFrame1D[samplePoints.size()][];
        for (int i = 0; i < pointTemperatureFrames.length; i++) {
            int frameCount = calculateFrameCount(steps, pointSaveSteps[i]);
            pointTemperatureFrames[i] = new TemperatureFrame1D[frameCount];
        }
    }

    /**
     * Вычисляет число кадров, которое будет сохранено для ряда
     * с заданным интервалом {@code saveStep}.
     *
     * <p>
     * Сохраняются шаги:
     * {@code 0, saveStep, 2 * saveStep, ...}, пока шаг не превысит {@code steps}.
     *
     * @param steps    общее число временных шагов расчёта без учёта начального
     *                 состояния
     * @param saveStep интервал сохранения в шагах
     * @return число сохраняемых кадров, включая начальное состояние
     */
    private int calculateFrameCount(int steps, int saveStep) {
        return steps / saveStep + 1;
    }

    /**
     * Обрабатывает состояние системы на очередном шаге расчёта.
     *
     * <p>
     * Для каждой контрольной точки и каждого профиля метод проверяет,
     * должен ли данный шаг быть сохранён согласно их индивидуальному
     * {@code saveStep}. Если шаг подлежит сохранению, из полного
     * температурного поля извлекаются нужные значения и записываются
     * во внутренние массивы кадров.
     *
     * @param step               индекс шага расчёта, где {@code 0} соответствует
     *                           начальному состоянию
     * @param timeSeconds        модельное время от начала расчёта, сек
     * @param temperatureCByCell полное поле температур по ячейкам сетки, °C
     * @throws IndexOutOfBoundsException если {@code step} вне допустимого диапазона
     * @throws NullPointerException      если {@code temperatureCByCell == null}
     */
    public void recordStep(int step, long timeSeconds, double[] temperatureCByCell) {
        if (step < 0 || step > maxStepIndex) {
            throw new IndexOutOfBoundsException("Step out of range: " + step);
        }
        Objects.requireNonNull(temperatureCByCell, "temperatureCByCell");

        // Точки
        for (int p = 0; p < samplePoints.size(); p++) {
            if (isSaveStep(step, pointSaveSteps[p])) {
                int actualPointStep = pointStep[p]++;
                SamplePoint point = samplePoints.get(p);
                int cellIndex = point.cellIndex();
                double temperature = temperatureCByCell[cellIndex];
                pointTemperatureFrames[p][actualPointStep] = new TemperatureFrame1D(temperature, timeSeconds);
            }
        }

        // Профили
        for (int p = 0; p < profiles.size(); p++) {
            if (isSaveStep(step, profileSaveSteps[p])) {
                int actualProfileStep = profileStep[p]++;
                Profile profile = profiles.get(p);
                int[] profileCellIndex = profile.cellIndex();
                double[] profileState = new double[profileCellIndex.length];

                for (int i = 0; i < profileCellIndex.length; i++) {
                    profileState[i] = temperatureCByCell[profileCellIndex[i]];
                }

                profileTemperatureFrames[p][actualProfileStep] = new TemperatureFrame2D(profileState, timeSeconds);
            }
        }
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
    public RecordingResult build() {
        List<SamplePointSeries> pointSeries = new ArrayList<>(samplePoints.size());
        for (int p = 0; p < samplePoints.size(); p++) {
            TemperatureFrame1D[] temperatureFrames = pointTemperatureFrames[p];
            pointSeries.add(new SamplePointSeries(
                    samplePoints.get(p),
                    temperatureFrames));
        }

        List<ProfileSeries> profileSeries = new ArrayList<>(profiles.size());
        for (int p = 0; p < profiles.size(); p++) {
            TemperatureFrame2D[] temperatureFrame2Ds = profileTemperatureFrames[p];
            profileSeries.add(new ProfileSeries(
                    profiles.get(p),
                    temperatureFrame2Ds));
        }

        return new RecordingResult(
                pointSeries,
                profileSeries);
    }

    private static boolean isSaveStep(int step, int saveStep) {
        return step % saveStep == 0;
    }
}