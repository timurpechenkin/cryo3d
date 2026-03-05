package io.github.timurpechenkin.solver.bc;

import java.util.Objects;

import io.github.timurpechenkin.domain.bc.BoundaryCondition;
import io.github.timurpechenkin.domain.bc.BoundaryConditionField;
import io.github.timurpechenkin.domain.bc.BoundaryConditionLibrary;
import io.github.timurpechenkin.domain.bc.BoundaryConditionType;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.geometry.Axis3D;
import io.github.timurpechenkin.geometry.Face;

/**
 * Applies FIRST_KIND (Dirichlet) boundary conditions by clamping boundary-layer
 * cell temperatures.
 *
 * Convention (w fastest on each face):
 * X_*: w=Y, h=Z, idx=y + ny*z, applied to cell x=0 or x=nx-1
 * Y_*: w=X, h=Z, idx=x + nx*z, applied to cell y=0 or y=ny-1
 * Z_*: w=X, h=Y, idx=x + nx*y, applied to cell z=0 or z=nz-1
 */
public final class DirichletApplier {

    public void apply(Grid3D grid,
            BoundaryConditionField bcField,
            BoundaryConditionLibrary bcLib,
            double[] temperatureByCell) {
        Objects.requireNonNull(grid, "grid");
        Objects.requireNonNull(bcField, "bcField");
        Objects.requireNonNull(bcLib, "bcLib");
        Objects.requireNonNull(temperatureByCell, "temperatureByCell");

        applyFaceX(grid, bcField, bcLib, temperatureByCell, Face.X_MIN, 0);
        applyFaceX(grid, bcField, bcLib, temperatureByCell, Face.X_MAX, grid.n(Axis3D.X) - 1);

        applyFaceY(grid, bcField, bcLib, temperatureByCell, Face.Y_MIN, 0);
        applyFaceY(grid, bcField, bcLib, temperatureByCell, Face.Y_MAX, grid.n(Axis3D.Y) - 1);

        applyFaceZ(grid, bcField, bcLib, temperatureByCell, Face.Z_MIN, 0);
        applyFaceZ(grid, bcField, bcLib, temperatureByCell, Face.Z_MAX, grid.n(Axis3D.Z) - 1);
    }

    private void applyFaceX(Grid3D grid,
            BoundaryConditionField bcField,
            BoundaryConditionLibrary bcLib,
            double[] T,
            Face face,
            int xCell) {
        // face array: w=Y (0..ny-1), h=Z (0..nz-1)
        int ny = grid.n(Axis3D.Y);
        int nz = grid.n(Axis3D.Z);

        for (int z = 0; z < nz; z++) {
            for (int y = 0; y < ny; y++) {
                double tbc = dirichletTemp(face, bcField, bcLib, y, z, grid);
                int idx = index3D(xCell, y, z, grid.n(Axis3D.X), ny);
                T[idx] = tbc;
            }
        }
    }

    private void applyFaceY(Grid3D grid,
            BoundaryConditionField bcField,
            BoundaryConditionLibrary bcLib,
            double[] T,
            Face face,
            int yCell) {
        // face array: w=X (0..nx-1), h=Z (0..nz-1)
        int nx = grid.n(Axis3D.X);
        int nz = grid.n(Axis3D.Z);

        for (int z = 0; z < nz; z++) {
            for (int x = 0; x < nx; x++) {
                double tbc = dirichletTemp(face, bcField, bcLib, x, z, grid);
                int idx = index3D(x, yCell, z, nx, grid.n(Axis3D.Y));
                T[idx] = tbc;
            }
        }
    }

    private void applyFaceZ(Grid3D grid,
            BoundaryConditionField bcField,
            BoundaryConditionLibrary bcLib,
            double[] T,
            Face face,
            int zCell) {
        // face array: w=X (0..nx-1), h=Y (0..ny-1)
        int nx = grid.n(Axis3D.X);
        int ny = grid.n(Axis3D.Y);

        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                double tbc = dirichletTemp(face, bcField, bcLib, x, y, grid);
                int idx = index3D(x, y, zCell, nx, ny);
                T[idx] = tbc;
            }
        }
    }

    private double dirichletTemp(Face face,
            BoundaryConditionField bcField,
            BoundaryConditionLibrary bcLib,
            int w,
            int h, Grid3D grid3d) {

        int bcIndex = grid3d.faceGrid(face).index(w, h);
        BoundaryCondition bc = bcLib.getByIndex(bcIndex);

        if (bc.type() != BoundaryConditionType.FIRST_KIND) {
            throw new UnsupportedOperationException(
                    "Only FIRST_KIND supported for now. Face=" + face + ", bc=" + bc.name() + ", type=" + bc.type());
        }
        if (bc.temperature() == null) {
            throw new IllegalStateException(
                    "FIRST_KIND requires temperature. Face=" + face + ", bc=" + bc.name());
        }
        return bc.temperature();
    }

    /** index = x + nx * (y + ny * z) */
    private static int index3D(int x, int y, int z, int nx, int ny) {
        return x + nx * (y + ny * z);
    }
}
