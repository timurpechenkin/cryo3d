package io.github.timurpechenkin.domain.config;

import java.math.RoundingMode;

public record NumberFormat(Integer fractionDigits, RoundingMode roundingMode) {
}