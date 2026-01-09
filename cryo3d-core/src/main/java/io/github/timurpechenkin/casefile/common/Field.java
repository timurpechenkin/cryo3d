package io.github.timurpechenkin.casefile.common;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Field<T>(
                @JsonProperty("default") T defaultValue,
                @JsonProperty("rules") List<Rule<T>> rules) {
}