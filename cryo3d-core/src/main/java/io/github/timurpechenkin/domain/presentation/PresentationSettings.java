package io.github.timurpechenkin.domain.presentation;

import io.github.timurpechenkin.time.TimeFormat;

public record PresentationSettings(
                TimeFormat timeFormat,
                NumberFormat numberFormat) {
}