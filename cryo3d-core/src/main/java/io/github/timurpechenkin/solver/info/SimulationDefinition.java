package io.github.timurpechenkin.solver.info;

import java.time.LocalDateTime;

public record SimulationDefinition(
                LocalDateTime startDate,
                LocalDateTime endDate,
                long dtSeconds,
                long cellCount,
                double sizeXMeters,
                double sizeYMeters,
                double sizeZMeters) {

}
