package io.github.timurpechenkin.domain.temperature;

public record Temperature(
                String name,
                TemperatureType type,
                double value) {
}