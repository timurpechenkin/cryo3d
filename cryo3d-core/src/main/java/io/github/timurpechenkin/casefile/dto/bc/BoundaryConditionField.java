package io.github.timurpechenkin.casefile.dto.bc;

import java.util.EnumMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.casefile.dto.common.Field;
import io.github.timurpechenkin.geometry.Face;

public record BoundaryConditionField(
        @JsonProperty("faces") EnumMap<Face, Field<String>> faces) {
}
