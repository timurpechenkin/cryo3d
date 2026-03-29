package io.github.timurpechenkin.domain.recording;

import io.github.timurpechenkin.geometry.Point3D;

public record SamplePoint(
        String name,
        int saveStep,
        Point3D point,
        int cellIndex) implements Recordable {

}
