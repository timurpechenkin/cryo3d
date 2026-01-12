package io.github.timurpechenkin.casefile.dto.bc;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Поля temperature / heatFlow / heatTransferCoefficient
 * будут валидироваться в зависимости от type на следующем шаге.
 */
public record BoundaryConditionDefinition(
        @JsonProperty("type") BoundaryConditionType type,
        @JsonProperty("temperature") Double temperature,
        @JsonProperty("heatFlow") Double heatFlow,
        @JsonProperty("heatTransferCoefficient") Double heatTransferCoefficient) {
}