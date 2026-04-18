package io.github.timurpechenkin.solver.recording;

/**
 * Запись значения температуры в точке в момент времени.
 */
public record TemperatureFrame0D(
                double temperature, long seconds) {
}