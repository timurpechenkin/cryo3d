package io.github.timurpechenkin.casefile.geometry;

import com.fasterxml.jackson.annotation.JsonCreator;

public record Point(double x, double y, double z) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Point fromArray(double[] a) {
        if (a == null || a.length != 3) {
            throw new IllegalArgumentException("Point3D must be an array of 3 numbers: [x, y, z]");
        }
        return new Point(a[0], a[1], a[2]);
    }
}