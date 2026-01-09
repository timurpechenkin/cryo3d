package io.github.timurpechenkin.casefile.bc;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.casefile.common.Field;

public record BoundaryConditionField(
                @JsonProperty("faces") Map<Face, Field<String>> faces) {
}
