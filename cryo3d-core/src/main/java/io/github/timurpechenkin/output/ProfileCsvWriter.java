package io.github.timurpechenkin.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;

import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.measurement.Profile;
import io.github.timurpechenkin.domain.measurement.ProfileGrid;
import io.github.timurpechenkin.domain.temperature.TemperatureField;

public final class ProfileCsvWriter {

    public void writeMaterialGridCsv(Path outDir, Profile profile,
            MaterialField matField, MaterialLibrary matLib) throws IOException {
        WriteToCsv toCsvFunc = (w, idx3d, wi, hi, pg) -> {
            int matIndex = matField.materialIndexByCell()[idx3d];
            String matName = matLib.getByIndex(matIndex).name();
            w.write(",");
            w.write(Csv.esc(matName));
        };

        writeToCsv(outDir, profile, "materials", "h\\w", toCsvFunc);
    }

    public void writeTemperatureGridCsv(Path outDir, Profile profile,
            TemperatureField tempField) throws IOException {
        WriteToCsv toCsvFunc = (w, idx3d, wi, hi, pg) -> {
            double t = tempField.temperatureCByCell()[idx3d];
            w.write(",");
            w.write(Double.toString(t));
        };

        writeToCsv(outDir, profile, "temperature0", "h\\w", toCsvFunc);
    }

    public void writeSamplesCsv(Path outDir, Profile profile,
            MaterialField matField, MaterialLibrary matLib,
            TemperatureField tempField) throws IOException {

        WriteToCsv toCsvFunc = (w, idx3d, wi, hi, pg) -> {
            int matIndex = matField.materialIndexByCell()[idx3d];
            String matName = matLib.getByIndex(matIndex).name();
            double t = tempField.temperatureCByCell()[idx3d];

            w.write(Integer.toString(wi));
            w.write(",");
            w.write(Integer.toString(hi));
            w.write(",");
            w.write(Double.toString(pg.wCentersMeters()[wi]));
            w.write(",");
            w.write(Double.toString(pg.hCentersMeters()[hi]));
            w.write(",");
            w.write(Integer.toString(idx3d));
            w.write(",");
            w.write(Csv.esc(matName));
            w.write(",");
            w.write(Double.toString(t));
            w.newLine();
        };

        writeToCsv(outDir, profile, "samples",
                "wIndex,hIndex,wMeters,hMeters,cellIndex,material,temperatureC", toCsvFunc);
    }

    private void writeToCsv(Path outDir, Profile profile, String name, String sign, WriteToCsv toCsvFunc)
            throws IOException {
        int width = profile.field2d().width();
        int height = profile.field2d().height();
        ProfileGrid pg = profile.grid();
        Path file = outDir.resolve("profiles")
                .resolve("profile_" + safe(profile.name()) + "_" + name + ".csv");

        try (BufferedWriter w = Csv.writer(file)) {
            w.write(sign);
            for (int wi = 0; wi < width; wi++) {
                w.write(",");
                w.write(Double.toString(pg.wCentersMeters()[wi]));
            }
            w.newLine();

            for (int hi = 0; hi < height; hi++) {
                w.write(Double.toString(pg.hCentersMeters()[hi]));
                for (int wi = 0; wi < width; wi++) {
                    int idx2d = profile.field2d().index(wi, hi);
                    int idx3d = profile.cellIndex()[idx2d];
                    toCsvFunc.write(w, idx3d, wi, hi, pg);
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
        void write(BufferedWriter writer, int idx3d, int wi, int hi, ProfileGrid pg) throws IOException;

    }
}
