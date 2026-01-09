package io.github.timurpechenkin.casefile.temperature;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.casefile.common.Field;

public record Temperature(
                @JsonProperty("field") Field<TemperatureValue> field) {
}
