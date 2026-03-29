package io.github.timurpechenkin.casefile.dto.recording;

import com.fasterxml.jackson.annotation.JsonCreator;

public record PointDto(double x, double y, double z) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PointDto fromArray(double[] a) {
        if (a == null || a.length != 3) {
            throw new IllegalArgumentException("Point3D must be an array of 3 numbers: [x, y, z]");
        }
        return new PointDto(a[0], a[1], a[2]);
    }
}