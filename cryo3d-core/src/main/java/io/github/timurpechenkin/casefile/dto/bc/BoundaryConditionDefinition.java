package io.github.timurpechenkin.casefile.dto.bc;

import java.time.Month;
import java.util.EnumMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.domain.bc.BoundaryConditionType;

/**
 * Поля temperature / heatFlow / heatTransferCoefficient
 * будут валидироваться в зависимости от type на следующем шаге.
 */
public record BoundaryConditionDefinition(
                @JsonProperty("type") BoundaryConditionType type,
                @JsonProperty("temperature") EnumMap<Month, Double> temperature,
                @JsonProperty("heatFlow") EnumMap<Month, Double> heatFlow,
                @JsonProperty("ambientTemperature") EnumMap<Month, Double> ambientTemperature,
                @JsonProperty("heatTransferCoefficient") EnumMap<Month, Double> heatTransferCoefficient) {

}