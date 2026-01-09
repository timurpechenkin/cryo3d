package io.github.timurpechenkin.casefile.temperature;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConstantTemperature(
                @JsonProperty("temperature") double temperature) implements TemperatureValue {
}
