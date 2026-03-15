package io.github.timurpechenkin.output;

import static io.github.timurpechenkin.geometry.GeometryScale.scaledToMeters;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.bc.BoundaryCondition;
import io.github.timurpechenkin.domain.bc.BoundaryConditionField;
import io.github.timurpechenkin.domain.bc.BoundaryConditionLibrary;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.material.Material;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.temperature.TemperatureField;
import io.github.timurpechenkin.geometry.Axis3D;
import io.github.timurpechenkin.geometry.Face;
import io.github.timurpechenkin.output.Summary.BoundaryConditionStatus;
import io.github.timurpechenkin.output.Summary.FaceBC;
import io.github.timurpechenkin.output.Summary.GridStats;
import io.github.timurpechenkin.output.Summary.MaterialStats;
import io.github.timurpechenkin.output.Summary.TemperatureStats;

public final class SummaryCalculator {

    private SummaryCalculator() {
    }

    public static Summary calculate(SimulationCase c) {
        GridStats gridStats = gridStats(c.grid());
        MaterialStats materialStats = materialStats(c.materialField(), c.materialLibrary());
        TemperatureStats temperatureStats = temperatureStats(c.temperatureField());
        BoundaryConditionStatus bcStatus = bcStatus(c.bcField(), c.bcLibrary());

        Summary summary = new Summary(
                c.caseName(),
                Instant.now(),
                c.time(),
                gridStats, materialStats,
                temperatureStats,
                bcStatus);

        return summary;
    }

    private static GridStats gridStats(Grid3D grid) {
        GridStats gridStats = new GridStats(
                grid.cellCount(),
                scaledToMeters(grid.sizeScaled(Axis3D.X)),
                scaledToMeters(grid.sizeScaled(Axis3D.Y)),
                scaledToMeters(grid.sizeScaled(Axis3D.Z)));
        return gridStats;
    }

    private static MaterialStats materialStats(MaterialField field, MaterialLibrary lib) {
        int[] matArr = field.materialIndexByCell();
        long total = matArr.length;

        long[] counts = new long[lib.size()];
        for (int idx : matArr) {
            if (idx < 0 || idx >= counts.length) {
                throw new IllegalStateException("Material index out of range: " + idx);
            }
            counts[idx]++;
        }

        // counts by name
        Map<String, Long> byName = new LinkedHashMap<>();
        for (int i = 0; i < counts.length; i++) {
            long c = counts[i];
            Material m = lib.getByIndex(i);
            byName.put(m.name(), c);
        }

        return new MaterialStats(total, byName);
    }

    private static TemperatureStats temperatureStats(TemperatureField field) {
        double[] tempArr = field.temperatureCByCell();
        long total = tempArr.length;
        if (total == 0) {
            return new TemperatureStats(0, Double.NaN, Double.NaN, Double.NaN, Map.of());
        }

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0.0;

        // частоты по округлению до 0.01°C
        Map<Integer, Long> countsScaled = new HashMap<>();

        for (double t : tempArr) {
            if (t < min)
                min = t;
            if (t > max)
                max = t;
            sum += t;

            int key = (int) Math.round(t * 100.0); // 0.01°C
            countsScaled.merge(key, 1L, Long::sum);
        }

        double avg = sum / total;

        // перевод ключей обратно в double
        Map<Double, Long> countsRounded = countsScaled.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        e -> e.getKey() / 100.0,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));

        return new TemperatureStats(total, min, max, avg, countsRounded);
    }

    private static BoundaryConditionStatus bcStatus(BoundaryConditionField field, BoundaryConditionLibrary library) {
        EnumMap<Face, FaceBC> bcByFace = new EnumMap<>(Face.class);
        for (Face face : Face.values()) {
            int[] bcArr = field.raw(face);
            long total = bcArr.length;
            long[] counts = new long[library.size()];
            for (int idx : bcArr) {
                if (idx < 0 || idx >= counts.length) {
                    throw new IllegalStateException("Material index out of range: " + idx);
                }
                counts[idx]++;
            }

            // counts by name
            Map<String, Long> byName = new LinkedHashMap<>();
            for (int i = 0; i < counts.length; i++) {
                long c = counts[i];
                BoundaryCondition m = library.getByIndex(i);
                byName.put(m.name(), c);
            }

            bcByFace.put(face, new FaceBC(total, byName));
        }
        return new BoundaryConditionStatus(bcByFace);
    }
}
