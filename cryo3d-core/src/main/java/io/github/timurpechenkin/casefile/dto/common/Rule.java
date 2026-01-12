package io.github.timurpechenkin.casefile.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.casefile.dto.selector.Selector;

public record Rule<T>(
        @JsonProperty("name") String name,
        @JsonProperty("selector") Selector selector,
        @JsonProperty("value") T value) {
}