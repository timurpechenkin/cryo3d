package io.github.timurpechenkin.solver;

import io.github.timurpechenkin.solver.info.SimulationDefinition;
import io.github.timurpechenkin.solver.metadata.SimulationMetadata;
import io.github.timurpechenkin.solver.recording.RecordingResult;

public record SimulationResult(
        SimulationMetadata metadata,
        SimulationDefinition definition,
        RecordingResult recording) {

}
