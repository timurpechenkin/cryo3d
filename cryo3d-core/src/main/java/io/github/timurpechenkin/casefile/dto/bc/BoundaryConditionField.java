package io.github.timurpechenkin.casefile.dto.bc;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.casefile.dto.common.Field;

public record BoundaryConditionField(
        @JsonProperty("faces") Map<Face, Field<String>> faces) {
}
