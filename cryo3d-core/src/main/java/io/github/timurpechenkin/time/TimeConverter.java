package io.github.timurpechenkin.time;

public class TimeConverter {
    public static String format(long seconds, TimeFormat format) {
        return switch (format) {
            case SECONDS -> String.valueOf(seconds);
            case MINUTES, HOURS -> String.format("%.1f", seconds / (format == TimeFormat.MINUTES ? 60.0 : 3600.0));
            case DAYS, MONTHS, YEARS -> String.format("%.2f",
                    seconds / switch (format) {
                        case DAYS -> 86400.0;
                        case MONTHS -> 86400.0 * 30.44;
                        case YEARS -> 86400.0 * 365.25;
                        default -> 1;
                    });
        };
    }
}