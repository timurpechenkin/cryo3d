package io.github.timurpechenkin.solver.metadata;

import java.time.Instant;

public record SimulationMetadata(
        String caseName,
        Instant calculationStartedUtc,
        Instant calculationEndedUtc) {
}