package io.github.timurpechenkin.solver;

import io.github.timurpechenkin.solver.info.SolverRunInfo;
import io.github.timurpechenkin.solver.recording.SimulationRecording;

public record SimulationResult(
                SimulationRecording recording,
                SolverRunInfo runInfo) {

}
