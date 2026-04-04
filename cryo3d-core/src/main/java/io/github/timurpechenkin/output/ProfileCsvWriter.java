package io.github.timurpechenkin.output;

import static io.github.timurpechenkin.number.NumberConverter.*;
import static io.github.timurpechenkin.geometry.GeometryScale.scaled2ToMeters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import io.github.timurpechenkin.domain.config.NumberFormat;
import io.github.timurpechenkin.domain.grid.Grid2D;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.recording.Profile;
import io.github.timurpechenkin.geometry.Axis2D;

public final class ProfileCsvWriter {

    public void writeMaterialGridCsv(Path outDir, Profile profile,
            int[] materialIndexByCell, MaterialLibrary matLib, String profileName) throws IOException {
        WriteToCsv toCsvFunc = (w, idx3d) -> {
            int matIndex = materialIndexByCell[idx3d];
            String matName = matLib.getById(matIndex).name();
            w.write(",");
            w.write(Csv.esc(matName));
        };

        writeToCsv(outDir, profile, profileName, "h\\w", toCsvFunc);
    }

    public void writeTemperatureGridCsv(Path outDir, Profile profile, double[] temperatureCGrid, String profileName,
            NumberFormat numberFormat)
            throws IOException {
        WriteToCsv toCsvFunc = (w, idx3d) -> {
            double t = temperatureCGrid[idx3d];
            w.write(",");
            w.write(format(t, numberFormat));
        };

        writeToCsv(outDir, profile, profileName, "h\\w", toCsvFunc);
    }

    private void writeToCsv(Path outDir, Profile profile, String profileName, String sign, WriteToCsv toCsvFunc)
            throws IOException {
        Grid2D grid = profile.grid2d();
        int nWidth = grid.n(Axis2D.W);
        int nHeight = grid.n(Axis2D.H);

        Files.createDirectories(outDir);
        Path file = outDir.resolve(profileName + ".csv");

        try (BufferedWriter w = Csv.writer(file)) {
            w.write(sign);
            for (int wi = 0; wi < nWidth; wi++) {
                double wMeters = scaled2ToMeters(grid.centerScaled2(Axis2D.W, wi));
                w.write(",");
                w.write(fmt2(wMeters));
            }
            w.newLine();

            for (int hi = 0; hi < nHeight; hi++) {
                double hMeters = scaled2ToMeters(grid.centerScaled2(Axis2D.H, hi));
                w.write(fmt2(hMeters));
                for (int wi = 0; wi < nWidth; wi++) {
                    int idx2d = profile.grid2d().index(wi, hi);
                    int idx3d = profile.cellIndex()[idx2d];
                    toCsvFunc.write(w, idx3d);
                }
                w.newLine();
            }
        }
    }

    public void writeTemperatureProfileCsv(Path outDir, Profile profile, double[] temperatureCProfile,
            String profileName, NumberFormat numberFormat) throws IOException {
        String sign = "h\\w";
        Grid2D grid = profile.grid2d();
        int nWidth = grid.n(Axis2D.W);
        int nHeight = grid.n(Axis2D.H);

        Files.createDirectories(outDir);
        Path file = outDir.resolve(profileName + ".csv");

        try (BufferedWriter w = Csv.writer(file)) {
            w.write(sign);
            for (int wi = 0; wi < nWidth; wi++) {
                double wMeters = scaled2ToMeters(grid.centerScaled2(Axis2D.W, wi));
                w.write(",");
                w.write(fmt2(wMeters));
            }
            w.newLine();

            for (int hi = 0; hi < nHeight; hi++) {
                double hMeters = scaled2ToMeters(grid.centerScaled2(Axis2D.H, hi));
                w.write(fmt2(hMeters));
                for (int wi = 0; wi < nWidth; wi++) {
                    int idx2d = profile.grid2d().index(wi, hi);
                    double t = temperatureCProfile[idx2d];
                    w.write(",");
                    w.write(format(t, numberFormat));
                }
                w.newLine();
            }
        }
    }

    @FunctionalInterface
    private interface WriteToCsv {
        void write(BufferedWriter writer, int idx3d) throws IOException;

    }

    private static String fmt2(double v) {
        return String.format(Locale.ROOT, "%.2f", v);
    }
}
