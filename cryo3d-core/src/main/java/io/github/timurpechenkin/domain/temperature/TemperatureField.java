package io.github.timurpechenkin.domain.temperature;

import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.domain.model.Field3D;

public final class TemperatureField {
    private final double[] temperatureCByCell;
    private final Field3D field3d;

    public TemperatureField(double[] temperatureCByCell, Grid grid) {
        long expected = (long) grid.nx() * grid.ny() * grid.nz();
        if (expected > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Grid too large for field: " + expected);
        }
        if (temperatureCByCell.length != (int) expected) {
            throw new IllegalArgumentException(
                    "temperatureCByCell.length=" + temperatureCByCell.length +
                            " != nx*ny*nz=" + expected);
        }

        this.temperatureCByCell = temperatureCByCell;
        this.field3d = new Field3D(grid);
    }

    public double[] temperatureCByCell() {
        return temperatureCByCell;
    }

    public double temperatureC(int x, int y, int z) {
        return temperatureCByCell()[field3d.index(x, y, z)];
    }
}
