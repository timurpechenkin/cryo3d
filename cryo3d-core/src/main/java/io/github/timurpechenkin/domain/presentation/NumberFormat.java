package io.github.timurpechenkin.domain.presentation;

import java.math.RoundingMode;

public record NumberFormat(Integer fractionDigits, RoundingMode roundingMode) {
}