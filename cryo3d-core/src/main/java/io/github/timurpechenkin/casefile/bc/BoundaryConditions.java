package io.github.timurpechenkin.casefile.bc;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BoundaryConditions(
                @JsonProperty("definitions") Map<String, BoundaryConditionDefinition> definitions,
                @JsonProperty("field") BoundaryConditionField field) {
}