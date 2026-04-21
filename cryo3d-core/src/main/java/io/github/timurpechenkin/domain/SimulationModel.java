package io.github.timurpechenkin.domain;

import io.github.timurpechenkin.domain.bc.BoundaryConditionSetup;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.material.MaterialSetup;
import io.github.timurpechenkin.domain.temperature.TemperatureSetup;
import io.github.timurpechenkin.domain.time.TimeSettings;

public record SimulationModel(
                TimeSettings time,
                Grid3D grid,
                BoundaryConditionSetup bcSetup,
                MaterialSetup materialSetup,
                TemperatureSetup temperatureSetup) {
}