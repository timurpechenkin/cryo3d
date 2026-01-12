package io.github.timurpechenkin.casefile.dto.grid;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record Segment(
                @PositiveOrZero @JsonProperty("from") double from,
                @PositiveOrZero @JsonProperty("to") double to,
                @Positive @JsonProperty("step") double step) {
}
