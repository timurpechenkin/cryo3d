package io.github.timurpechenkin.casefile.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.timurpechenkin.casefile.dto.bc.BoundaryConditionSpecDto;
import io.github.timurpechenkin.casefile.dto.geometry.ProfileDto;
import io.github.timurpechenkin.casefile.dto.geometry.SamplePointDto;
import io.github.timurpechenkin.casefile.dto.grid.GridSpecDto;
import io.github.timurpechenkin.casefile.dto.material.MaterialSpecDto;
import io.github.timurpechenkin.casefile.dto.temperature.TemperatureSpecDto;
import io.github.timurpechenkin.casefile.dto.time.TimeSettingsDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SimulationCaseDto(
        @NotBlank @JsonProperty("caseName") String caseName,
        @NotNull @Valid @JsonProperty("time") TimeSettingsDto time,
        @NotNull @Valid @JsonProperty("grid") GridSpecDto grid,
        @NotNull @Valid @JsonProperty("boundaryConditions") BoundaryConditionSpecDto boundaryConditions,
        @NotNull @Valid @JsonProperty("materials") MaterialSpecDto materials,
        @NotNull @Valid @JsonProperty("temperature") TemperatureSpecDto temperature,
        @NotNull @Valid @JsonProperty("profiles") List<ProfileDto> profiles,
        @NotNull @Valid @JsonProperty("samplePoints") List<SamplePointDto> samplePoints) {
}