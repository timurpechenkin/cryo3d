package io.github.timurpechenkin.casefile.dto.selector;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.timurpechenkin.casefile.dto.recording.RadiusPointDto;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** только ось Z */
public record RevolutionSelector(
        @JsonProperty("centerX") double centerX,
        @JsonProperty("centerY") double centerY,
        @JsonProperty("radiusPoints") @NotNull List<RadiusPointDto> radiusPoints) implements Selector {
}