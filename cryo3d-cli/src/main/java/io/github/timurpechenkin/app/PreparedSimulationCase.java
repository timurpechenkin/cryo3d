package io.github.timurpechenkin.app;

import java.nio.file.Path;

import io.github.timurpechenkin.domain.SimulationCase;

public record PreparedSimulationCase(Path path, SimulationCase simulationCase) {

}
