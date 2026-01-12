package io.github.timurpechenkin.output;

import java.time.Instant;

import io.github.timurpechenkin.casefile.dto.grid.GridSettings;
import io.github.timurpechenkin.casefile.dto.time.TimeSettings;

public record Summary(
        String caseName,
        Instant startedAtUtc,
        String status,
        TimeSettings time,
        GridSettings grid,
        GridInfo virtualGrid) {

    public record GridInfo(
            long cellCount,
            double sizeX,
            double sizeY,
            double sizeZ) {
    }
}
