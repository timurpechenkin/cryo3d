package io.github.timurpechenkin.domain.measurement;

import io.github.timurpechenkin.geometry.Point3D;

public record Profile(
        String name,
        Point3D pointA,
        Point3D pointB) {
}