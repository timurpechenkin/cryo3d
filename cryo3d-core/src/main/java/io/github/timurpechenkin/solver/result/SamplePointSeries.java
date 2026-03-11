package io.github.timurpechenkin.solver.result;

import io.github.timurpechenkin.domain.measurement.SamplePoint;

/**
 * История температуры в контрольной точке.
 *
 * <p>
 * Массив {@code temperatureCByStep} согласован с общей временной сеткой
 * результата: {@code temperatureCByStep[step]} — температура в данной точке
 * на момент {@code SimulationResult.timeSeconds()[step]}.
 *
 * @param samplePoint        описание контрольной точки
 * @param temperatureCByStep температура по шагам, °C
 */
public record SamplePointSeries(
        SamplePoint samplePoint,
        double[] temperatureCByStep) {
}