package io.github.timurpechenkin.domain.presentation;

import io.github.timurpechenkin.time.TimeFormat;

public record CaseConfig(
        String stepCalculatorKey,
        String materialModelKey,
        TimeFormat timeFormat,
        NumberFormat numberFormat) {
}
