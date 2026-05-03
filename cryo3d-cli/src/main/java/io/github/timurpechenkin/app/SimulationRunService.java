package io.github.timurpechenkin.app;

import java.nio.file.Path;

public interface SimulationRunService {

    SimulationPreparationReport prepare(Path casePath);

    SimulationRunReport run(PreparedSimulationCase preparedCase, Path outDir);
}