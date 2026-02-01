package io.github.timurpechenkin.output;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

import io.github.timurpechenkin.domain.time.TimeSettings;
import io.github.timurpechenkin.geometry.Face;

public record Summary(
                String caseName,
                Instant startedAtUtc,
                String status,
                TimeSettings time,
                GridStats virtualGrid,
                MaterialStats materialStats,
                TemperatureStats temperatureStats,
                BoundaryConditionStatus bcStatus) {

        public record GridStats(
                        long cellCount,
                        double sizeX,
                        double sizeY,
                        double sizeZ) {
        }

        public record MaterialStats(
                        long totalCells,
                        Map<String, Long> countsByName) {
        }

        public record TemperatureStats(
                        long totalCells,
                        double min,
                        double max,
                        double avg,
                        Map<Double, Long> countsByValueRounded2) {
        }

        public record BoundaryConditionStatus(EnumMap<Face, FaceBC> faceBC) {
        }

        public record FaceBC(long totalCells,
                        Map<String, Long> countsByName) {
        }

}
