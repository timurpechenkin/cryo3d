package io.github.timurpechenkin.casefile.dto.recording;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.geometry.Axis3D;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileDto(
                @NotBlank @JsonProperty("name") String name,
                @Min(1) @JsonProperty("saveStep") int saveStep,
                @Valid @JsonProperty("pointA") PointDto pointA,
                @Valid @JsonProperty("pointB") PointDto pointB,
                @NotNull @JsonProperty("axisParallel") Axis3D axisParallel) {
}
