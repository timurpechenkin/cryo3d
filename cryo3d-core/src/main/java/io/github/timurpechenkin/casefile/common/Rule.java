package io.github.timurpechenkin.casefile.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.casefile.selector.Selector;

public record Rule<T>(
                @JsonProperty("name") String name,
                @JsonProperty("selector") Selector selector,
                @JsonProperty("value") T value) {
}