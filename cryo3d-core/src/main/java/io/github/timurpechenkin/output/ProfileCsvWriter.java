package io.github.timurpechenkin.output;

import static io.github.timurpechenkin.geometry.GeometryScale.scaled2ToMeters;
import static io.github.timurpechenkin.number.NumberConverter.format;

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
    private static final int MAX_WARNINGS = 20;

    public void writeMaterialGridCsv(
            Path outDir,
            Profile profile,
            int[] materialIndexByCell,
            MaterialLibrary matLib,
            String profileName) throws IOException {

        WriteContext context = new WriteContext(profileName);

        WriteToCsv toCsvFunc = (w, idx3d) -> {
            int matIndex = materialIndexByCell[idx3d];
            String matName = matLib.getById(matIndex).name();
            w.write(",");
            w.write(Csv.esc(matName));
        };

        writeToCsv(outDir, profile, profileName, "h\\w", toCsvFunc, context);
        context.printSummaryIfNeeded();
    }

    public void writeTemperatureGridCsv(
            Path outDir,
            Profile profile,
            double[] temperatureCGrid,
            String profileName,
            NumberFormat numberFormat) throws IOException {

        WriteContext context = new WriteContext(profileName);

        WriteToCsv toCsvFunc = (w, idx3d) -> {
            double t = temperatureCGrid[idx3d];
            w.write(",");
            w.write(safeFormatGridValue(t, numberFormat, profileName, idx3d, context));
        };

        writeToCsv(outDir, profile, profileName, "h\\w", toCsvFunc, context);
        context.printSummaryIfNeeded();
    }

    private void writeToCsv(
            Path outDir,
            Profile profile,
            String profileName,
            String sign,
            WriteToCsv toCsvFunc,
            WriteContext context) throws IOException {

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
                    int idx2d = grid.index(wi, hi);
                    int idx3d = profile.cellIndex()[idx2d];
                    toCsvFunc.write(w, idx3d);
                }

                w.newLine();
            }
        }
    }

    public void writeTemperatureProfileCsv(
            Path outDir,
            Profile profile,
            double[] temperatureCProfile,
            String profileName,
            NumberFormat numberFormat) throws IOException {

        WriteContext context = new WriteContext(profileName);

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
                    int idx2d = grid.index(wi, hi);
                    double t = temperatureCProfile[idx2d];
                    w.write(",");
                    w.write(safeFormatProfileValue(t, numberFormat, profileName, wi, hi, idx2d, context));
                }

                w.newLine();
            }
        }

        context.printSummaryIfNeeded();
    }

    @FunctionalInterface
    private interface WriteToCsv {
        void write(BufferedWriter writer, int idx3d) throws IOException;
    }

    private static String fmt2(double v) {
        return String.format(Locale.ROOT, "%.2f", v);
    }

    private static String safeFormatProfileValue(
            double value,
            NumberFormat numberFormat,
            String profileName,
            int wi,
            int hi,
            int idx2d,
            WriteContext context) {

        if (Double.isNaN(value)) {
            context.warn("NaN in profile '" + profileName
                    + "' at wi=" + wi + ", hi=" + hi + ", idx2d=" + idx2d);
            return "NaN";
        }

        if (Double.isInfinite(value)) {
            context.warn("Infinite value in profile '" + profileName
                    + "' at wi=" + wi + ", hi=" + hi + ", idx2d=" + idx2d
                    + ": " + value);
            return value > 0 ? "Infinity" : "-Infinity";
        }

        return format(value, numberFormat);
    }

    private static String safeFormatGridValue(
            double value,
            NumberFormat numberFormat,
            String profileName,
            int idx3d,
            WriteContext context) {

        if (Double.isNaN(value)) {
            context.warn("NaN in grid '" + profileName + "' at idx3d=" + idx3d);
            return "NaN";
        }

        if (Double.isInfinite(value)) {
            context.warn("Infinite value in grid '" + profileName + "' at idx3d=" + idx3d
                    + ": " + value);
            return value > 0 ? "Infinity" : "-Infinity";
        }

        return format(value, numberFormat);
    }

    private static final class WriteContext {
        private final String profileName;
        private int invalidValueWarnings;
        private int invalidValueCount;

        private WriteContext(String profileName) {
            this.profileName = profileName;
        }

        private void warn(String message) {
            if (invalidValueWarnings < MAX_WARNINGS) {
                System.err.println("WARNING: " + message);
            } else if (invalidValueWarnings == MAX_WARNINGS) {
                System.err.println("WARNING: too many invalid values in '"
                        + profileName + "', suppressing further messages.");
            }

            invalidValueWarnings++;
            invalidValueCount++;
        }

        private void printSummaryIfNeeded() {
            if (invalidValueCount > 0) {
                System.err.println("WARNING: file '" + profileName + ".csv' written with "
                        + invalidValueCount + " invalid numeric value(s).");
            }
        }
    }
}