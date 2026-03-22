package io.github.timurpechenkin.domain.time;

import java.time.LocalDateTime;

public record TimeSettings(
                LocalDateTime startDate,
                LocalDateTime endDate,
                long dtSeconds,
                long saveEverySeconds) {

}
