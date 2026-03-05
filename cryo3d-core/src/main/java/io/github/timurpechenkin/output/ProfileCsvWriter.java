package io.github.timurpechenkin.output;

import static io.github.timurpechenkin.geometry.GeometryScale.scaled2ToMeters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;

import io.github.timurpechenkin.domain.grid.Grid2D;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.measurement.Profile;
import io.github.timurpechenkin.domain.temperature.TemperatureField;
import io.github.timurpechenkin.geometry.Axis2D;

public final class ProfileCsvWriter {

    public void writeMaterialGridCsv(Path outDir, Profile profile,
            MaterialField matField, MaterialLibrary matLib) throws IOException {
        WriteToCsv toCsvFunc = (w, idx3d) -> {
            int matIndex = matField.materialIndexByCell()[idx3d];
            String matName = matLib.getByIndex(matIndex).name();
            w.write(",");
            w.write(Csv.esc(matName));
        };

        writeToCsv(outDir, profile, "materials", "h\\w", toCsvFunc);
    }

    public void writeTemperatureGridCsv(Path outDir, Profile profile,
            TemperatureField tempField) throws IOException {
        WriteToCsv toCsvFunc = (w, idx3d) -> {
            double t = tempField.temperatureCByCell()[idx3d];
            w.write(",");
            w.write(Double.toString(t));
        };

        writeToCsv(outDir, profile, "temperature0", "h\\w", toCsvFunc);
    }

    private void writeToCsv(Path outDir, Profile profile, String type, String sign, WriteToCsv toCsvFunc)
            throws IOException {
        Grid2D pg = profile.grid2d();
        int nWidth = pg.n(Axis2D.W);
        int nHeight = pg.n(Axis2D.H);

        Path file = outDir.resolve("profiles")
                .resolve("profile_" + safe(profile.name()) + "_" + type + ".csv");

        try (BufferedWriter w = Csv.writer(file)) {
            w.write(sign);
            for (int wi = 0; wi < nWidth; wi++) {
                double wMeters = scaled2ToMeters(pg.centerScaled2(Axis2D.W, wi));
                w.write(",");
                w.write(Double.toString(wMeters));
            }
            w.newLine();

            for (int hi = 0; hi < nHeight; hi++) {
                double hMeters = scaled2ToMeters(pg.centerScaled2(Axis2D.H, hi));
                w.write(Double.toString(hMeters));
                for (int wi = 0; wi < nWidth; wi++) {
                    int idx2d = profile.grid2d().index(wi, hi);
                    int idx3d = profile.cellIndex()[idx2d];
                    toCsvFunc.write(w, idx3d);
                }
                w.newLine();
            }
        }
    }

    private static String safe(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]+", "_");
    }

    @FunctionalInterface
    private interface WriteToCsv {
        void write(BufferedWriter writer, int idx3d) throws IOException;

    }
}
