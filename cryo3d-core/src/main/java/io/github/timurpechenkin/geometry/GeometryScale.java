package io.github.timurpechenkin.geometry;

public class GeometryScale {
    private static final int SCALED = 100;

    public static int metersToScaled(double meters) {
        return (int) Math.round(meters * SCALED);
    }

    public static int metersToScaled2(double meters) {
        return (int) Math.round(meters * SCALED * 2);
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
        return scaled / (double) SCALED;
    }

    public static double scaled2ToMeters(int scaled) {
        return scaled / (double) (SCALED * 2);
    }

    public static double[] scaledArrToMetersArr(int[] scaled) {
        double[] meters = new double[scaled.length];
        for (int i = 0; i < scaled.length; i++) {
            meters[i] = scaledToMeters(scaled[i]);
        }
        return meters;
    }
}
