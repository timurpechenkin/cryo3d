package io.github.timurpechenkin.solver;

import io.github.timurpechenkin.solver.info.SimulationDefinition;
import io.github.timurpechenkin.solver.metadata.SimulationMetadata;
import io.github.timurpechenkin.solver.recording.SimulationRecording;

public record SimulationResult(
                SimulationMetadata metadata,
                SimulationDefinition definition,
                SimulationRecording recording) {

}
