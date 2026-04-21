package io.github.timurpechenkin.domain.bc;

public record BoundaryConditionSetup(
        BoundaryConditionLibrary library,
        BoundaryConditionField field) {
}