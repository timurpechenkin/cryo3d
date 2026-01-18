package io.github.timurpechenkin.output;

import java.time.Instant;

import io.github.timurpechenkin.casefile.dto.grid.GridSpecDto;
import io.github.timurpechenkin.casefile.dto.time.TimeSettingsDto;

public record Summary(
                String caseName,
                Instant startedAtUtc,
                String status,
                TimeSettingsDto time,
                GridSpecDto grid,
                GridInfo virtualGrid) {

        public record GridInfo(
                        long cellCount,
                        double sizeX,
                        double sizeY,
                        double sizeZ) {
        }
}
