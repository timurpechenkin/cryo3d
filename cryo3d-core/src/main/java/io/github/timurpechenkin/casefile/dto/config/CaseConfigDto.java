package io.github.timurpechenkin.casefile.dto.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CaseConfigDto(
                @JsonProperty("stepCalculatorKey") String stepCalculatorKey,
                @JsonProperty("materialModelKey") String materialModelKey) {
}