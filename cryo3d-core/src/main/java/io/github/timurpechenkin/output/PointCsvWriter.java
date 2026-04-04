package io.github.timurpechenkin.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;

import io.github.timurpechenkin.domain.config.NumberFormat;
import io.github.timurpechenkin.domain.recording.SamplePoint;
import io.github.timurpechenkin.solver.recording.TemperatureFrame1D;
import io.github.timurpechenkin.time.TimeFormat;

import static io.github.timurpechenkin.time.TimeConverter.*;
import static io.github.timurpechenkin.number.NumberConverter.*;

public class PointCsvWriter {

    public void writeTemperaturePointCsv(Path outDir, SamplePoint samplePoint, TemperatureFrame1D[] temperatureFrames,
            String pointName, TimeFormat timeFormat, NumberFormat numberFormat)
            throws IOException {

        Path file = outDir.resolve(pointName + ".csv");

        try (BufferedWriter w = Csv.writer(file)) {
            w.write(timeFormat.name());
            for (TemperatureFrame1D frame : temperatureFrames) {
                w.write(",");
                w.write(format(frame.seconds(), timeFormat));
            }
            w.newLine();

            w.write("temperature");
            for (TemperatureFrame1D frame : temperatureFrames) {
                w.write(",");
                w.write(format(frame.temperature(), numberFormat));
            }
        }
    }
}
