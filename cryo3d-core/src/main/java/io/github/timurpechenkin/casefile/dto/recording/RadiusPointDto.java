package io.github.timurpechenkin.casefile.dto.recording;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RadiusPointDto(
                @JsonProperty("z") double z,
                @JsonProperty("radius") double radius) {
}