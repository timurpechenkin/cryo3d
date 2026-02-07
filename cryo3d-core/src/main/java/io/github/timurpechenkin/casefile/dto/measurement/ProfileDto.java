package io.github.timurpechenkin.casefile.dto.measurement;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record ProfileDto(
        @NotBlank @JsonProperty("name") String name,
        @Valid @JsonProperty("pointA") PointDto pointA,
        @Valid @JsonProperty("pointB") PointDto pointB) {
}
