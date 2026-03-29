package io.github.timurpechenkin.casefile.dto.recording;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;

public record SamplePointDto(
                @JsonProperty("name") String name,
                @Min(1) @JsonProperty("saveStep") int saveStep,
                @JsonProperty("point") PointDto point) {

}
