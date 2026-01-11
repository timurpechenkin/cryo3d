package io.github.timurpechenkin.casefile.grid;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

public record GridSettings(
                @Valid @JsonProperty("axes") Map<Axis, List<Segment>> axesSegments) {
}