package io.github.timurpechenkin.casefile.dto.grid;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.geometry.Axis;
import jakarta.validation.Valid;

public record GridSettings(
        @Valid @JsonProperty("axes") Map<Axis, List<Segment>> axesSegments) {
}