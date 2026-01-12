package io.github.timurpechenkin.casefile.dto.material;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.casefile.dto.common.Field;

public record Materials(
                @JsonProperty("definitions") Map<String, MaterialDefinition> definitions,
                @JsonProperty("field") Field<String> field) {
}