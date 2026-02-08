package io.github.timurpechenkin.domain.material;

import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.domain.model.Field3D;

public final class MaterialField {
    private final int[] materialIndexByCell;
    private final Field3D field3d;

    public MaterialField(int[] materialIndexByCell, Grid grid) {
        long expected = (long) grid.nx() * grid.ny() * grid.nz();
        if (expected > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Grid too large for field: " + expected);
        }
        if (materialIndexByCell.length != (int) expected) {
            throw new IllegalArgumentException(
                    "materialIndexByCell.length=" + materialIndexByCell.length +
                            " != nx*ny*nz=" + expected);
        }

        this.field3d = new Field3D(grid);
        this.materialIndexByCell = materialIndexByCell;
    }

    public int[] materialIndexByCell() {
        return materialIndexByCell;
    }

    public int materialIndex(int x, int y, int z) {
        return materialIndexByCell()[field3d.index(x, y, z)];
    }
}
