package io.github.timurpechenkin.domain.bc;

public record BoundaryCondition(String name, BoundaryConditionType type, Double temperature, Double heatFlow,
                Double heatTransferCoefficient) {
}