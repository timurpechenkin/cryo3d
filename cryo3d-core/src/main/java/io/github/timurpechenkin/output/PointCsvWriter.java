package io.github.timurpechenkin.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;

import io.github.timurpechenkin.domain.measurement.SamplePoint;
import io.github.timurpechenkin.solver.result.TemperatureFrame1D;

public class PointCsvWriter {

    public void writeTemperaturePointCsv(Path outDir, SamplePoint samplePoint, TemperatureFrame1D[] temperatureFrames,
            String pointName)
            throws IOException {

        Path file = outDir.resolve(pointName + ".csv");

        try (BufferedWriter w = Csv.writer(file)) {
            w.write("seconds");
            for (TemperatureFrame1D frame : temperatureFrames) {
                w.write(",");
                w.write(Long.toString(frame.seconds()));
            }
            w.newLine();

            w.write("temperature");
            for (TemperatureFrame1D frame : temperatureFrames) {
                w.write(",");
                w.write(Double.toString(frame.temperature()));
            }
        }
    }
}
