package io.github.timurpechenkin.casefile.material;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MaterialDefinition(
                @JsonProperty("thermalConductivityThawed") double thermalConductivityThawed,
                @JsonProperty("thermalConductivityFrozen") double thermalConductivityFrozen,
                @JsonProperty("heatCapacityThawed") double heatCapacityThawed,
                @JsonProperty("heatCapacityFrozen") double heatCapacityFrozen,
                @JsonProperty("phaseTransitionsHeat") double phaseTransitionsHeat,
                @JsonProperty("freezingTemperature") double freezingTemperature) {
}