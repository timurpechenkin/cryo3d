package io.github.timurpechenkin.number;

import java.math.BigDecimal;

import io.github.timurpechenkin.domain.config.NumberFormat;

public final class NumberConverter {

    public static String format(double value, NumberFormat numberFormat) {
        BigDecimal bd = BigDecimal.valueOf(value)
                .setScale(numberFormat.fractionDigits(), numberFormat.roundingMode());
        return bd.toPlainString();
    }
}
