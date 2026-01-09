package io.github.timurpechenkin.casefile;

import java.io.IOException;
import java.nio.file.Path;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

public final class CaseLoader {
    private final ObjectMapper mapper;

    public CaseLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    public SimulationCase load(Path path) throws IOException {
        return mapper.readValue(path.toFile(), SimulationCase.class);
    }
}
