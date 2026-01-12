package io.github.timurpechenkin.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import io.github.timurpechenkin.casefile.dto.SimulationCase;
import io.github.timurpechenkin.grid.VirtualGrid;
import io.github.timurpechenkin.output.Summary.GridInfo;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

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

        VirtualGrid grid = VirtualGrid.from(c.grid());

        GridInfo gridInfo = new GridInfo(grid.cellCount(),
                grid.x().sizeMeters(),
                grid.y().sizeMeters(), grid.z().sizeMeters());
        Summary summary = new Summary(
                c.caseName(),
                Instant.now(),
                status,
                c.time(),
                c.grid(),
                gridInfo);

        Path file = outDir.resolve("summary.json");
        jsonMapper.writeValue(file.toFile(), summary);
    }
}
