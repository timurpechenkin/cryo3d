package io.github.timurpechenkin.solver.metadata;

import java.time.Instant;
import io.github.timurpechenkin.domain.SimulationCase;

public class SimulationMetadataCollector {
    private String caseName;
    private Instant calculationStartedUtc;

    public SimulationMetadataCollector(SimulationCase simulationCase) {
        this.calculationStartedUtc = Instant.now();
        this.caseName = simulationCase.caseName();
    }

    public SimulationMetadata metadata() {
        Instant calculationEndedUtc = Instant.now();
        return new SimulationMetadata(caseName, calculationStartedUtc, calculationEndedUtc);
    }
}
