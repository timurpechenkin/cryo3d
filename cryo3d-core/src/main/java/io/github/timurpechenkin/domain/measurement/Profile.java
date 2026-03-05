package io.github.timurpechenkin.domain.measurement;

import io.github.timurpechenkin.domain.grid.Grid2D;
import io.github.timurpechenkin.geometry.Point3D;

public record Profile(
        String name,
        Point3D pointA,
        Point3D pointB,
        Grid2D grid2d,
        int[] cellIndex) {
}