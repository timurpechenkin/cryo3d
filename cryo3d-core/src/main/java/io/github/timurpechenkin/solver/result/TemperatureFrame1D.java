package io.github.timurpechenkin.solver.result;

/**
 * Запись значения температуры в точке в момент времени.
 */
public record TemperatureFrame1D(
                double temperature, long seconds) {
}