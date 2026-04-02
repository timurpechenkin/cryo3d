package io.github.timurpechenkin.casefile.dto.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.time.TimeFormat;

public record CaseConfigDto(
                @JsonProperty("stepCalculatorKey") String stepCalculatorKey,
                @JsonProperty("materialModelKey") String materialModelKey,
                @JsonProperty("timeFormat") TimeFormat timeFormat) {
}