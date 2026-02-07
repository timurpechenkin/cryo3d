package io.github.timurpechenkin.casefile.dto.measurement;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SamplePointDto(
                @JsonProperty("name") String name,
                @JsonProperty("point") PointDto point) {

}
