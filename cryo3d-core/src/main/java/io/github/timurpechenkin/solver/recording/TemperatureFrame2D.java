package io.github.timurpechenkin.solver.recording;

/**
 * Запись значения температуры в плоскости в момент времени.
 */
public record TemperatureFrame2D(double[] temperatureCByCell, long seconds) {

}
