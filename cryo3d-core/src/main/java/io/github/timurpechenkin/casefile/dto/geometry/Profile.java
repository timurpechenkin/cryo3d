package io.github.timurpechenkin.casefile.dto.geometry;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Profile(
                @NotBlank @JsonProperty("name") String name,
                @Size(min = 2) @Valid @JsonProperty("points") List<Point> points) {
}
