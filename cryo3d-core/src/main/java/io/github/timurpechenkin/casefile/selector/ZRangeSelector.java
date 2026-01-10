package io.github.timurpechenkin.casefile.selector;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Positive;

public record ZRangeSelector(
        @Positive @JsonProperty("zMin") double zMin,
        @Positive @JsonProperty("zMax") double zMax) implements Selector {
}