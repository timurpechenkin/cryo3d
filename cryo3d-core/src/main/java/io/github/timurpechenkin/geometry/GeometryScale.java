package io.github.timurpechenkin.geometry;

public class GeometryScale {
    private static final int SCALE = 100;

    public static int metersToScaled(double meters) {
        return (int) Math.round(meters * SCALE);
    }

    public static int metersToScaled2(double meters) {
        return (int) Math.round(meters * SCALE * 2);
    }

    public static int scaledToScaled2(int scaled) {
        return scaled * 2;
    }

    public static int scaled2ToScaled(int scaled2) {
        if (scaled2 % 2 == 0) {
            return scaled2 / 2;
        }
        throw new IllegalArgumentException("The result of the division is not an integer");
    }

    public static double scaledToMeters(int scaled) {
        return scaled / (double) SCALE;
    }

    public static double scaled2ToMeters(int scaled) {
        return scaled / (double) (SCALE * 2);
    }
}
