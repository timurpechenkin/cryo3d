package io.github.timurpechenkin.casefile.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.casefile.dto.bc.BoundaryConditions;
import io.github.timurpechenkin.casefile.dto.geometry.Profile;
import io.github.timurpechenkin.casefile.dto.grid.GridSettings;
import io.github.timurpechenkin.casefile.dto.material.Materials;
import io.github.timurpechenkin.casefile.dto.temperature.Temperature;
import io.github.timurpechenkin.casefile.dto.time.TimeSettings;
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