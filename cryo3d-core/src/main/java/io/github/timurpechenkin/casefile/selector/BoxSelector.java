package io.github.timurpechenkin.casefile.selector;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BoxSelector(
                @JsonProperty("min") double[] min,
                @JsonProperty("max") double[] max) implements Selector {
}