package io.github.timurpechenkin.casefile.dto.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record NumberFormatDto(
        @Min(0) @Max(10) Integer fractionDigits,
        @NotBlank String roundingMode) {

}
