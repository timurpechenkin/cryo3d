package io.github.timurpechenkin.casefile;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;

public record TimeSettings(
                @Min(1) @JsonProperty("dtSeconds") long dtSeconds,
                @Min(1) @JsonProperty("saveEverySeconds") long saveEverySeconds,
                @Min(1) @JsonProperty("totalSeconds") long totalSeconds) {
}
