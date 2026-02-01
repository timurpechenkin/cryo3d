package io.github.timurpechenkin.casefile.resolve;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

import io.github.timurpechenkin.casefile.dto.common.Field;
import io.github.timurpechenkin.domain.bc.BoundaryCondition;
import io.github.timurpechenkin.domain.bc.BoundaryConditionField;
import io.github.timurpechenkin.domain.bc.BoundaryConditionLibrary;
import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.geometry.Face;

public final class BoundaryConditionDiscretizer {
    private final Compiler<BoundaryCondition> compiler = new Compiler<>();

    public BoundaryConditionField discretize(Grid grid,
            EnumMap<Face, Field<String>> faces,
            BoundaryConditionLibrary lib) {
        Objects.requireNonNull(grid, "grid");
        Objects.requireNonNull(faces, "faces");
        Objects.requireNonNull(lib, "lib");

        EnumMap<Face, int[]> result = new EnumMap<>(Face.class);

        for (Face face : Face.values()) {
            Field<String> field = faces.get(face);
            if (field == null) {
                throw new IllegalArgumentException("Missing bc field for face: " + face);
            }
            result.put(face, discretizeFace(grid, face, field, lib));
        }

        return new BoundaryConditionField(result, grid);
    }

    private int[] discretizeFace(Grid grid, Face face, Field<String> field, BoundaryConditionLibrary lib) {
        Objects.requireNonNull(field.defaultValue(), "bc field.defaultValue");

        int nx = grid.nx();
        int ny = grid.ny();
        int nz = grid.nz();

        List<CompiledRule> rules = compiler.compileRules(field.rules(), lib);

        return switch (face) {
            case X_MIN -> {
                int x2 = grid.edgesScaledX()[0] * 2;
                yield discretizeX(grid, field, lib, rules, x2);
            }
            case X_MAX -> {
                int x2 = grid.edgesScaledX()[nx] * 2;
                yield discretizeX(grid, field, lib, rules, x2);
            }
            case Y_MIN -> {
                int y2 = grid.edgesScaledY()[0] * 2;
                yield discretizeY(grid, field, lib, rules, y2);
            }
            case Y_MAX -> {
                int y2 = grid.edgesScaledY()[ny] * 2;
                yield discretizeY(grid, field, lib, rules, y2);
            }
            case Z_MIN -> {
                int z2 = grid.edgesScaledZ()[0] * 2;
                yield discretizeZ(grid, field, lib, rules, z2);
            }
            case Z_MAX -> {
                int z2 = grid.edgesScaledZ()[nz] * 2;
                yield discretizeZ(grid, field, lib, rules, z2);
            }
        };
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

    // Convention:
    // X_*: w=Y, h=Z, width=ny, height=nz, idx=y + ny*z
    // Y_*: w=X, h=Z, width=nx, height=nz, idx=x + nx*z
    // Z_*: w=X, h=Y, width=nx, height=ny, idx=x + nx*y
    private int[] discretizeZ(Grid grid,
            Field<String> field,
            BoundaryConditionLibrary lib,
            List<CompiledRule> rules, int z2) {

        int nx = grid.nx();
        int ny = grid.ny();

        int[] arr = new int[nx * ny];
        int defaultIndex = lib.indexOf(field.defaultValue());

        for (int y = 0; y < ny; y++) {
            int y2 = grid.centersScaled2Y()[y];
            for (int x = 0; x < nx; x++) {
                int x2 = grid.centersScaled2X()[x];
                int idx = x + nx * y;
                arr[idx] = pick(defaultIndex, rules, x2, y2, z2);
            }
        }
        return arr;
    }

    private int[] discretizeX(Grid grid,
            Field<String> field,
            BoundaryConditionLibrary lib,
            List<CompiledRule> rules, int x2) {

        int nz = grid.nz();
        int ny = grid.ny();

        int[] arr = new int[ny * nz];
        int defaultIndex = lib.indexOf(field.defaultValue());

        for (int z = 0; z < nz; z++) {
            int z2 = grid.centersScaled2Z()[z];
            for (int y = 0; y < ny; y++) {
                int y2 = grid.centersScaled2Y()[y];
                int idx = y + ny * z;
                arr[idx] = pick(defaultIndex, rules, x2, y2, z2);
            }
        }
        return arr;
    }

    private int[] discretizeY(Grid grid,
            Field<String> field,
            BoundaryConditionLibrary lib,
            List<CompiledRule> rules, int y2) {

        int nx = grid.nx();
        int nz = grid.nz();

        int[] arr = new int[nx * nz];
        int defaultIndex = lib.indexOf(field.defaultValue());

        for (int z = 0; z < nz; z++) {
            int z2 = grid.centersScaled2Z()[z];
            for (int x = 0; x < nx; x++) {
                int x2 = grid.centersScaled2X()[x];
                int idx = x + nx * z;
                arr[idx] = pick(defaultIndex, rules, x2, y2, z2);
            }
        }
        return arr;
    }
}
