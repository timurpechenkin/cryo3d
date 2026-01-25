package io.github.timurpechenkin.casefile.dto.temperature;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.casefile.dto.common.Field;

public record TemperatureSpecDto(
        @JsonProperty("definitions") Map<String, TemperatureDefinition> definitions,
        @JsonProperty("field") Field<String> field) {
}
