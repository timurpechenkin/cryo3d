package io.github.timurpechenkin.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import io.github.timurpechenkin.domain.SimulationCase;

public class OutputWriter {
    private final ObjectMapper jsonMapper;

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
    }
}
