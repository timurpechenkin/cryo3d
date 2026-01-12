package io.github.timurpechenkin.casefile.dto.temperature;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConstantTemperature(
        @JsonProperty("temperature") double temperature) implements TemperatureValue {
}
