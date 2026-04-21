package io.github.timurpechenkin.output.image;

import java.awt.Color;

/**
 * Простая непрерывная цветовая шкала температуры.
 *
 * <p>
 * Шкала задана набором опорных цветов и линейной интерполяцией между ними.
 * Минимум -> тёмно-синий, максимум -> тёмно-красный.
 */
public final class TemperatureColorScale {

    private static final Color[] STOPS = new Color[] {
            new Color(49, 54, 149), // dark blue
            new Color(69, 117, 180), // blue
            new Color(116, 173, 209), // light blue
            new Color(171, 217, 233), // pale cyan
            new Color(255, 255, 191), // pale yellow
            new Color(253, 174, 97), // orange
            new Color(244, 109, 67), // orange-red
            new Color(215, 48, 39), // red
            new Color(165, 0, 38) // dark red
    };

    public Color color(double value, double min, double max) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return Color.GRAY;
        }

        if (value <= min) {
            return STOPS[0];
        }
        if (value >= max) {
            return STOPS[STOPS.length - 1];
        }

        double t = (value - min) / (max - min);
        double scaled = t * (STOPS.length - 1);

        int leftIndex = (int) Math.floor(scaled);
        int rightIndex = Math.min(leftIndex + 1, STOPS.length - 1);
        double localT = scaled - leftIndex;

        return interpolate(STOPS[leftIndex], STOPS[rightIndex], localT);
    }

    private Color interpolate(Color a, Color b, double t) {
        int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bl);
    }
}