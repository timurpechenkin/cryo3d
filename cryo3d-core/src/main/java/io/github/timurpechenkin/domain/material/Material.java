package io.github.timurpechenkin.domain.material;

public record Material(
                String name,
                double thermalConductivityThawed,
                double thermalConductivityFrozen,
                double heatCapacityThawed,
                double heatCapacityFrozen,
                double phaseTransitionsHeat,
                double freezingTemperature) {

}
