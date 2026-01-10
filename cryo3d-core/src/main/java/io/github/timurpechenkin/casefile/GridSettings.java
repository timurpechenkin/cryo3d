package io.github.timurpechenkin.casefile;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record GridSettings(
        @Max(10000) @Min(2) @JsonProperty("nx") int nx,
        @Max(10000) @Min(2) @JsonProperty("ny") int ny,
        @Max(10000) @Min(2) @JsonProperty("nz") int nz,
        @Positive @JsonProperty("dxMeters") double dxMeters,
        @Positive @JsonProperty("dyMeters") double dyMeters,
        @Positive @JsonProperty("dzMeters") double dzMeters) {
}