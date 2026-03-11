package io.github.timurpechenkin.solver;

import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.bc.BoundaryConditionField;
import io.github.timurpechenkin.domain.bc.BoundaryConditionLibrary;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.material.Material;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.temperature.TemperatureField;
import io.github.timurpechenkin.geometry.Axis3D;
import io.github.timurpechenkin.solver.bc.DirichletApplier;

/**
 * Forward Euler explicit heat conduction solver on a tensor (possibly
 * non-uniform) grid.
 *
 * Equation (volumetric heat capacity):
 * C * dT/dt = div( lambda * grad(T) )
 *
 * Implementation uses fluxes through 6 faces with harmonic mean conductivity on
 * interfaces.
 *
 * Boundary conditions:
 * Only FIRST_KIND (Dirichlet) is supported for now (clamped after step).
 */
public final class ExplicitHeatSolver {

    private final int dtSeconds;
    private final DirichletApplier dirichlet = new DirichletApplier();

    public ExplicitHeatSolver(int dtSeconds) {
        if (!(dtSeconds > 0.0)) {
            throw new IllegalArgumentException("dtSeconds must be > 0");
        }
        this.dtSeconds = dtSeconds;
    }

    public TemperatureField advanceOneStep(SimulationCase c, long tSeconds, TemperatureField current) {
        Objects.requireNonNull(c, "c");
        Objects.requireNonNull(current, "current");

        Grid3D grid = c.grid();
        MaterialField matField = c.materialField();
        MaterialLibrary matLib = c.materialLibrary();

        BoundaryConditionField bcField = c.bcField();
        BoundaryConditionLibrary bcLib = c.bcLibrary();

        double[] T = current.temperatureCByCell();
        double[] Tnext = new double[T.length];

        // axis steps in meters (computed from scaled arrays to avoid requiring extra
        // API)
        double[] dx = stepsMeters(grid.stepsScaled(Axis3D.X));
        double[] dy = stepsMeters(grid.stepsScaled(Axis3D.Y));
        double[] dz = stepsMeters(grid.stepsScaled(Axis3D.Z));

        int nx = grid.n(Axis3D.X);
        int ny = grid.n(Axis3D.Y);
        int nz = grid.n(Axis3D.Z);

        int[] matIdxByCell = matField.materialIndexByCell();

        for (int z = 0; z < nz; z++) {
            double dz_k = dz[z];
            for (int y = 0; y < ny; y++) {
                double dy_j = dy[y];
                for (int x = 0; x < nx; x++) {
                    double dx_i = dx[x];

                    int idx = index3D(x, y, z, nx, ny);

                    int matIdx = matIdxByCell[idx];
                    Material mCell = matLib.getByIndex(matIdx);

                    double C = mCell.heatCapacityThawed(); // [J/(m3*K)]
                    double lamCell = mCell.thermalConductivityThawed(); // [W/(m*K)]

                    if (!(C > 0.0)) {
                        throw new IllegalStateException("Non-positive heat capacity at cell idx=" + idx + ": " + C);
                    }
                    if (!(lamCell > 0.0)) {
                        throw new IllegalStateException(
                                "Non-positive conductivity at cell idx=" + idx + ": " + lamCell);
                    }

                    double V = dx_i * dy_j * dz_k;

                    // areas
                    double Ax = dy_j * dz_k;
                    double Ay = dx_i * dz_k;
                    double Az = dx_i * dy_j;

                    double sumFlux = 0.0;

                    // X- neighbor
                    if (x > 0) {
                        int idxL = index3D(x - 1, y, z, nx, ny);
                        sumFlux += fluxBetweenCellsX(idxL, idx, x - 1, x, dx, Ax, matIdxByCell, matLib, T);
                    }
                    // X+ neighbor
                    if (x < nx - 1) {
                        int idxR = index3D(x + 1, y, z, nx, ny);
                        sumFlux += fluxBetweenCellsX(idx, idxR, x, x + 1, dx, Ax, matIdxByCell, matLib, T);
                    }

                    // Y- neighbor
                    if (y > 0) {
                        int idxD = index3D(x, y - 1, z, nx, ny);
                        sumFlux += fluxBetweenCellsY(idxD, idx, y - 1, y, dy, Ay, matIdxByCell, matLib, T);
                    }
                    // Y+ neighbor
                    if (y < ny - 1) {
                        int idxU = index3D(x, y + 1, z, nx, ny);
                        sumFlux += fluxBetweenCellsY(idx, idxU, y, y + 1, dy, Ay, matIdxByCell, matLib, T);
                    }

                    // Z- neighbor
                    if (z > 0) {
                        int idxB = index3D(x, y, z - 1, nx, ny);
                        sumFlux += fluxBetweenCellsZ(idxB, idx, z - 1, z, dz, Az, matIdxByCell, matLib, T);
                    }
                    // Z+ neighbor
                    if (z < nz - 1) {
                        int idxF = index3D(x, y, z + 1, nx, ny);
                        sumFlux += fluxBetweenCellsZ(idx, idxF, z, z + 1, dz, Az, matIdxByCell, matLib, T);
                    }

                    // Forward Euler update
                    Tnext[idx] = T[idx] + (dtSeconds / (C * V)) * sumFlux;
                }
            }
        }

        // Apply Dirichlet on boundary-layer cells after step
        // (At this stage, BC arrays currently cover whole faces with single default;
        // later can support rules)
        if (bcField != null && bcLib != null) {
            dirichlet.apply(grid, bcField, bcLib, Tnext);
        }

        return new TemperatureField(Tnext);
    }

    // ---------- Flux helpers ----------

    /**
     * Flux contribution across X interface between leftCell and rightCell:
     * q = lambda_face * A * (T_right - T_left) / d
     * where d is distance between cell centers along X.
     */
    private static double fluxBetweenCellsX(int idxLeft, int idxRight,
            int iLeft, int iRight,
            double[] dx,
            double area,
            int[] matIdxByCell,
            MaterialLibrary matLib,
            double[] T) {
        double lamL = matLib.getByIndex(matIdxByCell[idxLeft]).thermalConductivityThawed();
        double lamR = matLib.getByIndex(matIdxByCell[idxRight]).thermalConductivityThawed();
        double lamFace = harmonicMean(lamL, lamR);

        double d = 0.5 * dx[iLeft] + 0.5 * dx[iRight];
        return lamFace * area * (T[idxRight] - T[idxLeft]) / d;
    }

    private static double fluxBetweenCellsY(int idxDown, int idxUp,
            int jDown, int jUp,
            double[] dy,
            double area,
            int[] matIdxByCell,
            MaterialLibrary matLib,
            double[] T) {
        double lamD = matLib.getByIndex(matIdxByCell[idxDown]).thermalConductivityThawed();
        double lamU = matLib.getByIndex(matIdxByCell[idxUp]).thermalConductivityThawed();
        double lamFace = harmonicMean(lamD, lamU);

        double d = 0.5 * dy[jDown] + 0.5 * dy[jUp];
        return lamFace * area * (T[idxUp] - T[idxDown]) / d;
    }

    private static double fluxBetweenCellsZ(int idxBack, int idxFront,
            int kBack, int kFront,
            double[] dz,
            double area,
            int[] matIdxByCell,
            MaterialLibrary matLib,
            double[] T) {
        double lamB = matLib.getByIndex(matIdxByCell[idxBack]).thermalConductivityThawed();
        double lamF = matLib.getByIndex(matIdxByCell[idxFront]).thermalConductivityThawed();
        double lamFace = harmonicMean(lamB, lamF);

        double d = 0.5 * dz[kBack] + 0.5 * dz[kFront];
        return lamFace * area * (T[idxFront] - T[idxBack]) / d;
    }

    private static double harmonicMean(double a, double b) {
        if (!(a > 0.0) || !(b > 0.0)) {
            throw new IllegalArgumentException("Non-positive values for harmonic mean: a=" + a + ", b=" + b);
        }
        return (2.0 * a * b) / (a + b);
    }

    private static double[] stepsMeters(int[] stepsScaled) {
        // stepsScaled are meters*SCALE; SCALE=100 in GeometryScale; but we avoid
        // depending on it directly here.
        // If you prefer, expose GeometryScale.toMeters(...) and use it. For now assume
        // SCALE=100.
        final double INV_SCALED = 1.0 / 100.0;
        double[] out = new double[stepsScaled.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = stepsScaled[i] * INV_SCALED;
        }
        return out;
    }

    /** index = x + nx * (y + ny * z) */
    private static int index3D(int x, int y, int z, int nx, int ny) {
        return x + nx * (y + ny * z);
    }
}
