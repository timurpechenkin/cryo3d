package io.github.timurpechenkin.geometry;

import java.util.List;

public record Profile(
        String name,
        List<Point3D> points) {
}