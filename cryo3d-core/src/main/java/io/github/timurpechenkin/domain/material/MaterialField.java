package io.github.timurpechenkin.domain.material;

import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.domain.model.AbstractField3D;

public final class MaterialField extends AbstractField3D {
    private final int[] materialIndexByCell;

    public MaterialField(int[] materialIndexByCell, Grid grid) {
        super(grid);
        this.materialIndexByCell = materialIndexByCell;

        long expected = (long) nx() * ny() * nz();
        if (expected > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Grid too large for field: " + expected);
        }
        if (materialIndexByCell.length != (int) expected) {
            throw new IllegalArgumentException(
                    "materialIndexByCell.length=" + materialIndexByCell.length +
                            " != nx*ny*nz=" + expected);
        }
    }

    public int[] materialIndexByCell() {
        return materialIndexByCell;
    }

    public int materialIndex(int x, int y, int z) {
        return materialIndexByCell()[index(x, y, z)];
    }
}
