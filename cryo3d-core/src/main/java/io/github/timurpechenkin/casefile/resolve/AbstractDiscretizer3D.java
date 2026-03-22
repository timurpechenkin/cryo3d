package io.github.timurpechenkin.casefile.resolve;

import java.util.List;
import java.util.Objects;

import io.github.timurpechenkin.casefile.dto.common.Field;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.model.Library;
import io.github.timurpechenkin.geometry.Axis3D;

public class AbstractDiscretizer3D<T> {
    private Compiler<T> compiler = new Compiler<>();

    protected final int[] discretizeToIndex(Grid3D grid, Field<String> field, Library<T> lib) {
        Objects.requireNonNull(grid, "grid");
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(lib, "lib");
        Objects.requireNonNull(field.defaultValue(), "field.defaultValue");

        int nx = grid.n(Axis3D.X);
        int ny = grid.n(Axis3D.Y);
        int nz = grid.n(Axis3D.Z);

        long cellCount = grid.cellCount();
        if (cellCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Grid too large for int[] field: cellCount=" + cellCount);
        }

        int[] indexArr = new int[(int) cellCount];

        // default item id -> defaultIndex
        int defaultIndex = lib.idOf(field.defaultValue());

        // compile rules once
        List<CompiledRule> compiledRules = compiler.compileRules(field.rules(), lib);

        // fill
        int idx = 0;
        for (int k = 0; k < nz; k++) {
            int cz2 = grid.centerScaled2(Axis3D.Z, k);
            for (int j = 0; j < ny; j++) {
                int cy2 = grid.centerScaled2(Axis3D.Y, j);
                for (int i = 0; i < nx; i++) {
                    int cx2 = grid.centerScaled2(Axis3D.X, i);
                    indexArr[idx++] = pick(defaultIndex, compiledRules, cx2, cy2, cz2);
                }
            }
        }

        return indexArr;
    }

    private int pick(int defaultIndex, List<CompiledRule> rules, int cx2, int cy2, int cz2) {
        int v = defaultIndex;
        for (CompiledRule r : rules) {
            if (r.selector().contains(cx2, cy2, cz2)) {
                v = r.itemIndex();
            }
        }
        return v;
    }
}
