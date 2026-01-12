package io.github.timurpechenkin.casefile.dto.common;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

public record Field<T>(
                @Valid @JsonProperty("default") T defaultValue,
                @Valid @JsonProperty("rules") List<Rule<T>> rules) {
}