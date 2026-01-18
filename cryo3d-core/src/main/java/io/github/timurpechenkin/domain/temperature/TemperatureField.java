package io.github.timurpechenkin.domain.temperature;

public final class TemperatureField {
    private final double[] temperatureCByCell;

    public TemperatureField(double[] temperatureCByCell) {
        this.temperatureCByCell = temperatureCByCell;
    }

    public static TemperatureField empty() {
        return new TemperatureField(new double[0]);
    }

    public double[] temperatureCByCell() {
        return temperatureCByCell;
    }
}
