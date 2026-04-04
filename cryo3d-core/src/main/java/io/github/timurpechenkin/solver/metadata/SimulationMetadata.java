package io.github.timurpechenkin.solver.metadata;

import java.time.Instant;

import io.github.timurpechenkin.domain.config.NumberFormat;
import io.github.timurpechenkin.time.TimeFormat;

public record SimulationMetadata(
        String caseName,
        Instant calculationStartedUtc,
        Instant calculationEndedUtc,
        TimeFormat timeFormat,
        NumberFormat numberFormat) {
}