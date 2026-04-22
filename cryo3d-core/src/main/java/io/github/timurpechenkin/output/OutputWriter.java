package io.github.timurpechenkin.output;

import static io.github.timurpechenkin.time.TimeConverter.format;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.presentation.NumberFormat;
import io.github.timurpechenkin.domain.presentation.PresentationSettings;
import io.github.timurpechenkin.domain.recording.Profile;
import io.github.timurpechenkin.domain.recording.SamplePoint;
import io.github.timurpechenkin.domain.temperature.TemperatureField;
import io.github.timurpechenkin.solver.recording.ProfileSeries;
import io.github.timurpechenkin.solver.recording.RecordingResult;
import io.github.timurpechenkin.solver.recording.SamplePointSeries;
import io.github.timurpechenkin.solver.recording.TemperatureFrame2D;
import io.github.timurpechenkin.time.TimeFormat;
import io.github.timurpechenkin.output.csv.PointCsvWriter;
import io.github.timurpechenkin.output.csv.ProfileCsvWriter;
import io.github.timurpechenkin.output.image.*;

public class OutputWriter {
    private final Path outDir;
    private final ObjectMapper jsonMapper;
    private final ProfilePngWriter profilePngWriter = new ProfilePngWriter();
    private final ProfileCsvWriter profileCsvWriter = new ProfileCsvWriter();
    private final PointCsvWriter pointCsvWriter = new PointCsvWriter();

    public OutputWriter(Path outDir) {
        this.outDir = outDir;
        this.jsonMapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                // to allow serialization of "empty" POJOs (no properties to serialize)
                // (without this setting, an exception is thrown in those cases)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();
    }

    public void writeSummary(SimulationCase c, String caseName) throws IOException {
        Path startDir = outDir.resolve(caseName).resolve("definition");
        Files.createDirectories(startDir);

        SimulationSummary summary = SummaryCalculator.calculate(c);

        Path file = startDir.resolve("summary.json");
        jsonMapper.writeValue(file.toFile(), summary);

        TemperatureField temperatureField = c.model().temperatureSetup().field();
        MaterialField materialField = c.model().materialSetup().field();
        MaterialLibrary materialLibrary = c.model().materialSetup().library();
        PresentationSettings presentationSettings = c.presentation();
        for (Profile profile : c.recording().profiles()) {
            profileCsvWriter.writeMaterialGridCsv(startDir, profile, materialField.materialIdByCell(),
                    materialLibrary, profile.name() + "_material_0");
            profileCsvWriter.writeTemperatureGridCsv(startDir, profile, temperatureField.temperatureCByCell(),
                    profile.name() + "_temperature_0", presentationSettings.numberFormat());
        }
    }

    public void writeResult(RecordingResult result, String caseName, TimeFormat timeFormat, NumberFormat numberFormat)
            throws IOException {
        Path resultDir = outDir.resolve(caseName).resolve("result");

        // Запись температур по профилям в CSV и PNG
        ProfileRenderSettings settings = ProfileRenderSettings.defaults(-10.0, 10.0);
        Path profileDir = resultDir.resolve("profiles");
        for (ProfileSeries profileSeries : result.profileSeries()) {
            Profile profile = profileSeries.profile();
            Path specialProfileDir = profileDir.resolve(profile.name());
            for (TemperatureFrame2D temperatureFrame : profileSeries.temperatureFrames()) {
                double[] temperatureCByCell = temperatureFrame.temperatureCByCell();
                String time = format(temperatureFrame.seconds(), timeFormat);
                String fileName = profile.name() + "_temperature_" + time;
                profileCsvWriter.writeTemperature(
                        specialProfileDir,
                        profile,
                        temperatureCByCell,
                        fileName,
                        numberFormat);
                profilePngWriter.writeTemperature(
                        specialProfileDir,
                        profile,
                        temperatureFrame,
                        fileName,
                        numberFormat,
                        settings);
            }
        }

        // Запись данных температур по точкам в CSV
        Path pointDir = resultDir.resolve("points");
        for (SamplePointSeries samplePointSeries : result.pointSeries()) {
            SamplePoint samplePoint = samplePointSeries.samplePoint();
            pointCsvWriter.writeTemperature(
                    pointDir,
                    samplePointSeries.temperatureFrames(),
                    samplePoint.name() + "_temperature",
                    timeFormat,
                    numberFormat);
        }
    }
}
