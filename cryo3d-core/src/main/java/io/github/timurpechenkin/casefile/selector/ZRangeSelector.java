package io.github.timurpechenkin.casefile.selector;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ZRangeSelector(
                @JsonProperty("zMin") double zMin,
                @JsonProperty("zMax") double zMax) implements Selector {
}