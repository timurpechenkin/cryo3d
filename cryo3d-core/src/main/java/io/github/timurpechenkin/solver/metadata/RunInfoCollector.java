package io.github.timurpechenkin.solver.metadata;

import java.time.Instant;
import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.solver.info.SolverRunInfo;

public class RunInfoCollector {
    private Instant calculationStartedUtc;
    private int stepCount;

    public RunInfoCollector(SimulationCase simulationCase) {
        this.calculationStartedUtc = Instant.now();
    }

    public void addStepCount(int steps) {
        this.stepCount = steps;
    }

    public SolverRunInfo info() {
        Instant calculationEndedUtc = Instant.now();
        return new SolverRunInfo(calculationStartedUtc, calculationEndedUtc, stepCount);
    }
}
