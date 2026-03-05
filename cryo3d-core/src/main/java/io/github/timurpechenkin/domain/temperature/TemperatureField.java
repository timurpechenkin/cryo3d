package io.github.timurpechenkin.domain.temperature;

public final class TemperatureField {
    private final double[] temperatureCByCell;

    public TemperatureField(double[] temperatureCByCell) {
        this.temperatureCByCell = temperatureCByCell;
    }

    public double[] temperatureCByCell() {
        return temperatureCByCell;
    }
}
