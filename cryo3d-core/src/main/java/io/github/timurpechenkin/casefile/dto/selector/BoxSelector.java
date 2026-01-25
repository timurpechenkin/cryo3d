package io.github.timurpechenkin.casefile.dto.selector;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BoxSelector(
                @JsonProperty("min") double[] min,
                @JsonProperty("max") double[] max) implements Selector {

        public double minXMeters() {
                return min[0];
        }

        public double minYMeters() {
                return min[1];
        }

        public double minZMeters() {
                return min[2];
        }

        public double maxXMeters() {
                return max[0];
        }

        public double maxYMeters() {
                return max[1];
        }

        public double maxZMeters() {
                return max[2];
        }
}