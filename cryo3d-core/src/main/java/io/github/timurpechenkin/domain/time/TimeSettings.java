package io.github.timurpechenkin.domain.time;

public record TimeSettings(
        long dtSeconds,
        long saveEverySeconds,
        long totalSeconds) {

}
