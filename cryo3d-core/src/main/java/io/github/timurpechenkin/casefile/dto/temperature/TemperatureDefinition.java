package io.github.timurpechenkin.casefile.dto.temperature;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.domain.temperature.TemperatureType;

public record TemperatureDefinition(
        @JsonProperty("type") TemperatureType type,
        @JsonProperty("temperature") double temperature) {

}
