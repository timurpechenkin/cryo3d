package io.github.timurpechenkin.geometry;

public class GeometryScale {
    private static final int SCALE = 100;

    public static int toScaled(double meters) {
        return (int) Math.round(meters * SCALE);
    }

    public static int toScaled2(double meters) {
        return (int) Math.round(meters * SCALE * 2);
    }

    public static double toMeters(int scaled) {
        return scaled / (double) SCALE;
    }

    public static double toMeters2(int scaled) {
        return scaled / (double) (SCALE * 2);
    }
}
