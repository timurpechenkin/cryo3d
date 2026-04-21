package io.github.timurpechenkin.solver.metadata;

import java.time.Instant;
import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.metadata.CaseMetadata;
import io.github.timurpechenkin.domain.presentation.NumberFormat;
import io.github.timurpechenkin.domain.presentation.PresentationSettings;
import io.github.timurpechenkin.time.TimeFormat;

public class SimulationMetadataCollector {
    private String caseName;
    private Instant calculationStartedUtc;
    private TimeFormat timeFormat;
    private NumberFormat numberFormat;

    public SimulationMetadataCollector(SimulationCase simulationCase) {
        CaseMetadata metadata = simulationCase.metadata();
        PresentationSettings presentationSettings = simulationCase.presentation();
        this.calculationStartedUtc = Instant.now();
        this.caseName = metadata.caseName();
        this.timeFormat = presentationSettings.timeFormat();
        this.numberFormat = presentationSettings.numberFormat();
    }

    public SimulationMetadata metadata() {
        Instant calculationEndedUtc = Instant.now();
        return new SimulationMetadata(caseName, calculationStartedUtc, calculationEndedUtc, timeFormat, numberFormat);
    }
}
