package io.github.timurpechenkin.casefile.dto.time;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;

public record TimeSettingsDto(
        @JsonProperty("startDate") LocalDateTime startDate,
        @JsonProperty("endDate") LocalDateTime endDate,
        @Min(1) @JsonProperty("dtSeconds") long dtSeconds) {
}
