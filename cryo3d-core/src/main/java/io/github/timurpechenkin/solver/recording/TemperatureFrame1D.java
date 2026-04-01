package io.github.timurpechenkin.solver.recording;

/**
 * Запись значения температуры в точке в момент времени.
 */
public record TemperatureFrame1D(
        double temperature, long seconds) {
}