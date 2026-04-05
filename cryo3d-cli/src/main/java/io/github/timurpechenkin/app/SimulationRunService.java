package io.github.timurpechenkin.app;

import java.nio.file.Path;

public interface SimulationRunService {
    SimulationRunReport run(Path casePath, Path outDir);
}