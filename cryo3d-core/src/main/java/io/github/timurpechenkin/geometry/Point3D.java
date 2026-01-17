package io.github.timurpechenkin.geometry;

public record Point3D(int x, int y, int z) {
    public double xMeters(int scale) {
        return x / (double) scale;
    }

    public double yMeters(int scale) {
        return y / (double) scale;
    }

    public double zMeters(int scale) {
        return z / (double) scale;
    }
}
