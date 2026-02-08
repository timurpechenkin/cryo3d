package io.github.timurpechenkin.geometry;

import static io.github.timurpechenkin.geometry.GeometryScale.*;

public record Point3D(int xScaled, int yScaled, int zScaled) {
    public double xMeters() {
        return scaledToMeters(xScaled);
    }

    public double yMeters() {
        return scaledToMeters(yScaled);
    }

    public double zMeters() {
        return scaledToMeters(zScaled);
    }
}
