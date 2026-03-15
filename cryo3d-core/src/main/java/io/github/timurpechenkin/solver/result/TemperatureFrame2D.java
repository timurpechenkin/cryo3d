package io.github.timurpechenkin.solver.result;

/**
 * Запись значения температуры в плоскости в момент времени.
 */
public record TemperatureFrame2D(double[] temperatureCByCell, long seconds) {

}
