package io.github.timurpechenkin.domain.recording;

import io.github.timurpechenkin.domain.grid.Grid2D;
import io.github.timurpechenkin.geometry.Point3D;

public record Profile(
                String name,
                int saveStep,
                Point3D pointA,
                Point3D pointB,
                Grid2D grid2d,
                int[] cellIndex) implements Recordable {
}