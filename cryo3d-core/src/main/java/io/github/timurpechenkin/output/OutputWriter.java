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
import io.github.timurpechenkin.domain.measurement.Profile;
import io.github.timurpechenkin.domain.temperature.TemperatureField;

public class OutputWriter {
    private final ObjectMapper jsonMapper;
    private final ProfileCsvWriter profileCsvWriter = new ProfileCsvWriter();

    public OutputWriter() {
        this.jsonMapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                // to allow serialization of "empty" POJOs (no properties to serialize)
                // (without this setting, an exception is thrown in those cases)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();
    }

    public void writeSummary(Path outDir, SimulationCase c, String status) throws IOException {
        Files.createDirectories(outDir);

        Summary summary = SummaryCalculator.calculate(c, status);

        Path file = outDir.resolve("summary.json");
        jsonMapper.writeValue(file.toFile(), summary);

        TemperatureField temperatureField = c.temperatureField();
        MaterialField materialField = c.materialField();
        MaterialLibrary materialLibrary = c.materialLibrary();
        for (Profile profile : c.profiles()) {
            profileCsvWriter.writeMaterialGridCsv(outDir, profile, materialField, materialLibrary);
            profileCsvWriter.writeTemperatureGridCsv(outDir, profile, temperatureField);
        }
    }
}
