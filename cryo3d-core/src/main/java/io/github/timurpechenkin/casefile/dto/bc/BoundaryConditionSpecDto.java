package io.github.timurpechenkin.casefile.dto.bc;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BoundaryConditionSpecDto(
                @JsonProperty("definitions") Map<String, BoundaryConditionDefinition> definitions,
                @JsonProperty("field") BoundaryConditionField field) {
}