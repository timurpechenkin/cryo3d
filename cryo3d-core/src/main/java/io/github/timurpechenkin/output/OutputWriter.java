package io.github.timurpechenkin.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.recording.Profile;
import io.github.timurpechenkin.domain.recording.SamplePoint;
import io.github.timurpechenkin.domain.temperature.TemperatureField;
import io.github.timurpechenkin.solver.recording.ProfileSeries;
import io.github.timurpechenkin.solver.recording.RecordingResult;
import io.github.timurpechenkin.solver.recording.SamplePointSeries;
import io.github.timurpechenkin.solver.recording.TemperatureFrame2D;

public class OutputWriter {
    private final Path outDir;
    private final ObjectMapper jsonMapper;
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
        Path startDir = outDir.resolve(caseName).resolve("start");
        Files.createDirectories(startDir);

        Summary summary = SummaryCalculator.calculate(c);

        Path file = startDir.resolve("summary.json");
        jsonMapper.writeValue(file.toFile(), summary);

        TemperatureField temperatureField = c.temperatureField();
        MaterialField materialField = c.materialField();
        MaterialLibrary materialLibrary = c.materialLibrary();
        for (Profile profile : c.profiles()) {
            profileCsvWriter.writeMaterialGridCsv(startDir, profile, materialField.materialIdByCell(),
                    materialLibrary, profile.name() + "_material_0");
            profileCsvWriter.writeTemperatureGridCsv(startDir, profile, temperatureField.temperatureCByCell(),
                    profile.name() + "_temperature_0");
        }
    }

    public void writeResult(RecordingResult result, String caseName) throws IOException {
        Path resultDir = outDir.resolve(caseName).resolve("result");

        // Запись данных температур по профилям в csv
        Path profileDir = resultDir.resolve("profiles");
        for (ProfileSeries profileSeries : result.profileSeries()) {
            Profile profile = profileSeries.profile();
            Path specialProfileDir = profileDir.resolve(profile.name());
            for (TemperatureFrame2D temperatureFrames : profileSeries.temperatureFrames()) {
                double[] temperatureCByCell = temperatureFrames.temperatureCByCell();
                long seconds = temperatureFrames.seconds();
                profileCsvWriter.writeTemperatureProfileCsv(specialProfileDir, profile, temperatureCByCell,
                        profile.name() + "_temperature_" + seconds);
            }
        }

        // Запись данных температур по точкам в csv
        Path pointDir = resultDir.resolve("points");
        for (SamplePointSeries samplePointSeries : result.pointSeries()) {
            SamplePoint samplePoint = samplePointSeries.samplePoint();
            pointCsvWriter.writeTemperaturePointCsv(pointDir, samplePoint, samplePointSeries.temperatureFrames(),
                    samplePoint.name() + "_temperature");
        }

    }
}
