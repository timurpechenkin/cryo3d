package io.github.timurpechenkin.output.image;

/**
 * Настройки рендера одного PNG-кадра профиля.
 *
 * @param imageWidth      ширина итогового изображения в пикселях
 * @param imageHeight     высота итогового изображения в пикселях
 * @param minTemperatureC минимальная температура цветовой шкалы, °C
 * @param maxTemperatureC максимальная температура цветовой шкалы, °C
 * @param axisWLabel      подпись оси W
 * @param axisHLabel      подпись оси H
 * @param drawGrid        рисовать ли сетку поверх температурного поля
 */
public record ProfileRenderSettings(
        int imageWidth,
        int imageHeight,
        double minTemperatureC,
        double maxTemperatureC,
        String axisWLabel,
        String axisHLabel,
        boolean drawGrid) {

    public ProfileRenderSettings {
        if (imageWidth <= 0) {
            throw new IllegalArgumentException("imageWidth must be > 0");
        }
        if (imageHeight <= 0) {
            throw new IllegalArgumentException("imageHeight must be > 0");
        }
        if (!Double.isFinite(minTemperatureC) || !Double.isFinite(maxTemperatureC)) {
            throw new IllegalArgumentException("Temperature bounds must be finite");
        }
        if (maxTemperatureC <= minTemperatureC) {
            throw new IllegalArgumentException("maxTemperatureC must be > minTemperatureC");
        }
        if (axisWLabel == null || axisWLabel.isBlank()) {
            throw new IllegalArgumentException("axisWLabel must not be blank");
        }
        if (axisHLabel == null || axisHLabel.isBlank()) {
            throw new IllegalArgumentException("axisHLabel must not be blank");
        }
    }

    public static ProfileRenderSettings defaults(double minTemperatureC, double maxTemperatureC) {
        return new ProfileRenderSettings(
                1200,
                800,
                minTemperatureC,
                maxTemperatureC,
                "W, m",
                "H, m",
                true);
    }
}