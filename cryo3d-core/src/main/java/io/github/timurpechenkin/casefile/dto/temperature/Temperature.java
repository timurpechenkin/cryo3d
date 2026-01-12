package io.github.timurpechenkin.casefile.dto.temperature;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.casefile.dto.common.Field;

public record Temperature(
        @JsonProperty("field") Field<TemperatureValue> field) {
}
