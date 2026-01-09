package io.github.timurpechenkin.casefile;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.casefile.bc.BoundaryConditions;
import io.github.timurpechenkin.casefile.geometry.Profile;
import io.github.timurpechenkin.casefile.material.Materials;
import io.github.timurpechenkin.casefile.temperature.Temperature;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SimulationCase(
        @NotBlank @JsonProperty("caseName") String caseName,
        @NotNull @Valid @JsonProperty("time") TimeSettings time,
        @NotNull @Valid @JsonProperty("grid") GridSettings grid,
        @NotNull @Valid @JsonProperty("boundaryConditions") BoundaryConditions boundaryConditions,
        @NotNull @Valid @JsonProperty("materials") Materials materials,
        @NotNull @Valid @JsonProperty("temperature") Temperature temperature,
        @NotNull @Valid @JsonProperty("profiles") List<Profile> profiles) {
}