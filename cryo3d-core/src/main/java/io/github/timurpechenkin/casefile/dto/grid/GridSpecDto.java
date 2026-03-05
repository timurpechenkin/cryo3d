package io.github.timurpechenkin.casefile.dto.grid;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.geometry.Axis3D;
import jakarta.validation.Valid;

public record GridSpecDto(
        @Valid @JsonProperty("axes") Map<Axis3D, List<Segment>> axesSegments) {
}