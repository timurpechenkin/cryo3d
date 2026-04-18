package io.github.timurpechenkin.solver.recording;

import io.github.timurpechenkin.domain.recording.SamplePoint;

/**
 * История температуры в контрольной точке.
 *
 *
 * @param samplePoint       описание контрольной точки
 * @param temperatureFrames записи температуры по шагам, °C
 */
public record SamplePointSeries(
        SamplePoint samplePoint,
        TemperatureFrame0D[] temperatureFrames) {
}