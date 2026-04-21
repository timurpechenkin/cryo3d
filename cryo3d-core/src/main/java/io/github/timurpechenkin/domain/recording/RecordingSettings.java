package io.github.timurpechenkin.domain.recording;

import java.util.List;

public record RecordingSettings(
                List<Profile> profiles,
                List<SamplePoint> samplePoints) {
}