package io.github.timurpechenkin.solver.metadata;

import java.time.Instant;
import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.time.TimeFormat;

public class SimulationMetadataCollector {
    private String caseName;
    private Instant calculationStartedUtc;
    private TimeFormat timeFormat;

    public SimulationMetadataCollector(SimulationCase simulationCase) {
        this.calculationStartedUtc = Instant.now();
        this.caseName = simulationCase.caseName();
        this.timeFormat = simulationCase.config().timeFormat();
    }

    public SimulationMetadata metadata() {
        Instant calculationEndedUtc = Instant.now();
        return new SimulationMetadata(caseName, calculationStartedUtc, calculationEndedUtc, timeFormat);
    }
}
