package io.github.timurpechenkin.solver.info;

import java.time.Duration;
import java.time.Instant;

public record SolverRunInfo(
        Instant startedUtc,
        Instant endedUtc,
        long stepCount) {

    public Duration duration() {
        return Duration.between(startedUtc, endedUtc);
    }
}