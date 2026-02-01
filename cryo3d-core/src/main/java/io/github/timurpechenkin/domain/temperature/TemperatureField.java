package io.github.timurpechenkin.domain.temperature;

import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.domain.model.AbstractField3D;

public final class TemperatureField extends AbstractField3D {
    private final double[] temperatureCByCell;

    public TemperatureField(double[] temperatureCByCell, Grid grid) {
        super(grid);
        this.temperatureCByCell = temperatureCByCell;

        long expected = (long) nx() * ny() * nz();
        if (expected > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Grid too large for field: " + expected);
        }
        if (temperatureCByCell.length != (int) expected) {
            throw new IllegalArgumentException(
                    "temperatureCByCell.length=" + temperatureCByCell.length +
                            " != nx*ny*nz=" + expected);
        }
    }

    public double[] temperatureCByCell() {
        return temperatureCByCell;
    }

    public double temperatureC(int x, int y, int z) {
        return temperatureCByCell()[index(x, y, z)];
    }
}
