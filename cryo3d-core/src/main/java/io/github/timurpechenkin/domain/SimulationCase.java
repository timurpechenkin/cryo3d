package io.github.timurpechenkin.domain;

import java.util.List;

import io.github.timurpechenkin.domain.bc.BoundaryConditionField;
import io.github.timurpechenkin.domain.bc.BoundaryConditionLibrary;
import io.github.timurpechenkin.domain.config.CaseConfig;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.recording.Profile;
import io.github.timurpechenkin.domain.recording.SamplePoint;
import io.github.timurpechenkin.domain.temperature.TemperatureField;
import io.github.timurpechenkin.domain.temperature.TemperatureLibrary;
import io.github.timurpechenkin.domain.time.TimeSettings;

public record SimulationCase(
        String caseName,
        CaseConfig solverConfig,
        TimeSettings time,
        Grid3D grid,
        BoundaryConditionLibrary bcLibrary,
        BoundaryConditionField bcField,
        MaterialLibrary materialLibrary,
        MaterialField materialField,
        TemperatureLibrary temperatureLibrary,
        TemperatureField temperatureField,
        List<Profile> profiles,
        List<SamplePoint> samplePoints) {
}