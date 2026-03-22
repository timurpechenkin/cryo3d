package io.github.timurpechenkin.domain.bc;

import java.time.Month;
import java.util.EnumMap;

public record BoundaryCondition(
                String name,
                BoundaryConditionType type,
                EnumMap<Month, Double> temperature,
                EnumMap<Month, Double> heatFlux,
                EnumMap<Month, Double> ambientTemperature,
                EnumMap<Month, Double> heatTransferCoefficient) {
}