package io.github.timurpechenkin.output;

import static io.github.timurpechenkin.number.NumberConverter.format;
import static io.github.timurpechenkin.time.TimeConverter.format;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;

import io.github.timurpechenkin.domain.config.NumberFormat;
import io.github.timurpechenkin.solver.recording.TemperatureFrame1D;
import io.github.timurpechenkin.time.TimeFormat;

public class PointCsvWriter {
    private static final int MAX_WARNINGS = 20;

    public void writeTemperaturePointCsv(
            Path outDir,
            TemperatureFrame1D[] temperatureFrames,
            String pointName,
            TimeFormat timeFormat,
            NumberFormat numberFormat) throws IOException {

        WriteContext context = new WriteContext(pointName);
        Path file = outDir.resolve(pointName + ".csv");

        try (BufferedWriter w = Csv.writer(file)) {
            w.write(timeFormat.name());
            for (TemperatureFrame1D frame : temperatureFrames) {
                w.write(",");
                w.write(format(frame.seconds(), timeFormat));
            }
            w.newLine();

            w.write("temperature");
            for (int i = 0; i < temperatureFrames.length; i++) {
                TemperatureFrame1D frame = temperatureFrames[i];
                w.write(",");
                w.write(safeFormatTemperature(
                        frame.temperature(),
                        numberFormat,
                        pointName,
                        i,
                        frame.seconds(),
                        context));
            }
            w.newLine();
        }

        context.printSummaryIfNeeded();
    }

    private static String safeFormatTemperature(
            double value,
            NumberFormat numberFormat,
            String pointName,
            int frameIndex,
            long seconds,
            WriteContext context) {

        if (Double.isNaN(value)) {
            context.warn("NaN in point '" + pointName
                    + "' at frameIndex=" + frameIndex
                    + ", seconds=" + seconds);
            return "NaN";
        }

        if (Double.isInfinite(value)) {
            context.warn("Infinite value in point '" + pointName
                    + "' at frameIndex=" + frameIndex
                    + ", seconds=" + seconds
                    + ": " + value);
            return value > 0 ? "Infinity" : "-Infinity";
        }

        return format(value, numberFormat);
    }

    private static final class WriteContext {
        private final String pointName;
        private int invalidValueWarnings;
        private int invalidValueCount;

        private WriteContext(String pointName) {
            this.pointName = pointName;
        }

        private void warn(String message) {
            if (invalidValueWarnings < MAX_WARNINGS) {
                System.err.println("WARNING: " + message);
            } else if (invalidValueWarnings == MAX_WARNINGS) {
                System.err.println("WARNING: too many invalid values in '"
                        + pointName + "', suppressing further messages.");
            }

            invalidValueWarnings++;
            invalidValueCount++;
        }

        private void printSummaryIfNeeded() {
            if (invalidValueCount > 0) {
                System.err.println("WARNING: file '" + pointName + ".csv' written with "
                        + invalidValueCount + " invalid numeric value(s).");
            }
        }
    }
}