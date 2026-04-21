package io.github.timurpechenkin.domain.temperature;

public record TemperatureSetup(
        TemperatureLibrary library,
        TemperatureField field) {
}