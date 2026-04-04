package io.github.timurpechenkin.casefile.dto.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.time.TimeFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CaseConfigDto(
        @NotBlank @JsonProperty("stepCalculatorKey") String stepCalculatorKey,
        @NotBlank @JsonProperty("materialModelKey") String materialModelKey,
        @NotNull @JsonProperty("timeFormat") TimeFormat timeFormat,
        @NotNull @JsonProperty("numberFormat") @Valid NumberFormatDto numberFormat) {
}