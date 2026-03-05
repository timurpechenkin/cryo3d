package io.github.timurpechenkin.domain.grid;

import java.util.EnumMap;
import java.util.Objects;

import io.github.timurpechenkin.geometry.Axis2D;
import io.github.timurpechenkin.geometry.Axis3D;
import io.github.timurpechenkin.geometry.Face;

public class VirtualGrid3D implements Grid3D {
    private final EnumMap<Axis3D, AxisGrid> axesGrids;

    /**
     * Кэш 2D-представлений граней 3D-сетки.
     *
     * <p>
     * Важно: Grid2D описывает только дискретизацию плоскости (оси и ячейки),
     * но не фиксирует координату “третьей” оси (X/Y/Z), т.е. не различает MIN/MAX
     * по геометрическому положению — только по ориентации плоскости.
     */
    private final EnumMap<Face, Grid2D> faceGrids;

    public VirtualGrid3D(EnumMap<Axis3D, AxisGrid> axesGrids) {
        this.axesGrids = Objects.requireNonNull(axesGrids, "axesGrids");

        AxisGrid x = requireAxis(Axis3D.X);
        AxisGrid y = requireAxis(Axis3D.Y);
        AxisGrid z = requireAxis(Axis3D.Z);

        // Face X_* — плоскость YZ
        EnumMap<Axis2D, AxisGrid> faceYZ = new EnumMap<>(Axis2D.class);
        faceYZ.put(Axis2D.W, y); // W ~ горизонталь плоскости (Y)
        faceYZ.put(Axis2D.H, z); // H ~ вертикаль плоскости (Z)

        // Face Y_* — плоскость XZ
        EnumMap<Axis2D, AxisGrid> faceXZ = new EnumMap<>(Axis2D.class);
        faceXZ.put(Axis2D.W, x); // X
        faceXZ.put(Axis2D.H, z); // Z

        // Face Z_* — плоскость XY
        EnumMap<Axis2D, AxisGrid> faceXY = new EnumMap<>(Axis2D.class);
        faceXY.put(Axis2D.W, x); // X
        faceXY.put(Axis2D.H, y); // Y

        Grid2D yz = new VirtualGrid2D(faceYZ);
        Grid2D xz = new VirtualGrid2D(faceXZ);
        Grid2D xy = new VirtualGrid2D(faceXY);

        EnumMap<Face, Grid2D> fg = new EnumMap<>(Face.class);
        fg.put(Face.X_MIN, yz);
        fg.put(Face.X_MAX, yz);
        fg.put(Face.Y_MIN, xz);
        fg.put(Face.Y_MAX, xz);
        fg.put(Face.Z_MIN, xy);
        fg.put(Face.Z_MAX, xy);

        this.faceGrids = fg;
    }

    private AxisGrid requireAxis(Axis3D axis) {
        AxisGrid g = axesGrids.get(axis);
        if (g == null) {
            throw new IllegalArgumentException("Missing axis grid: " + axis);
        }
        return g;
    }

    @Override
    public int n(Axis3D axis3d) {
        return axesGrids.get(axis3d).cells();
    }

    @Override
    public int[] edgesScaled(Axis3D axis3d) {
        return axesGrids.get(axis3d).edgesScaled();
    }

    @Override
    public int[] centersScaled2(Axis3D axis3d) {
        return axesGrids.get(axis3d).centersScaled2();
    }

    @Override
    public int[] stepsScaled(Axis3D axis3d) {
        return axesGrids.get(axis3d).stepsScaled();
    }

    @Override
    public int centerScaled2(Axis3D axis3d, int p) {
        return axesGrids.get(axis3d).centersScaled2()[p];
    }

    @Override
    public int findCellScaled(Axis3D axis3d, int c) {
        int[] edges = axesGrids.get(axis3d).edgesScaled();
        int n = edges.length - 1;

        if (c < edges[0] || c > edges[n]) {
            throw new IndexOutOfBoundsException("Coordinate out of axis bounds: c=" + c);
        }
        if (c == edges[n]) {
            return n - 1;
        }

        int left = 0, right = n - 1;
        while (left <= right) {
            int mid = (left + right) >>> 1;
            int a = edges[mid];
            int b = edges[mid + 1];

            if (c < a) {
                right = mid - 1;
            } else if (c >= b) {
                left = mid + 1;
            } else {
                return mid; // p
            }
        }

        // Теоретически недостижимо при строго возрастающих edges и предыдущих
        // проверках.
        throw new IndexOutOfBoundsException("Coordinate out of axis bounds: c=" + c);
    }

    @Override
    public int sizeScaled(Axis3D axis3d) {
        return axesGrids.get(axis3d).sizeScaled();
    }

    @Override
    public long cellCount() {
        return (long) n(Axis3D.X) * (long) n(Axis3D.Y) * (long) n(Axis3D.Z);
    }

    @Override
    public long cellVolumeScaled3(int x, int y, int z) {
        if (!contains(x, y, z)) {
            throw new IndexOutOfBoundsException(
                    "Cell position out of range: x=" + x + ", y=" + y + ", z=" + z);
        }

        return (long) axesGrids.get(Axis3D.X).stepsScaled()[x]
                * (long) axesGrids.get(Axis3D.Y).stepsScaled()[y]
                * (long) axesGrids.get(Axis3D.Z).stepsScaled()[z];
    }

    @Override
    public int index(int x, int y, int z) {
        final int nx = n(Axis3D.X);
        final int ny = n(Axis3D.Y);
        final int nz = n(Axis3D.Z);

        if (x < 0 || x >= nx)
            throw new IndexOutOfBoundsException("X out of range: " + x);
        if (y < 0 || y >= ny)
            throw new IndexOutOfBoundsException("Y out of range: " + y);
        if (z < 0 || z >= nz)
            throw new IndexOutOfBoundsException("Z out of range: " + z);

        long idx = (long) x + (long) nx * ((long) y + (long) ny * (long) z);

        // Если idx не помещается в int, всё равно нельзя адресовать обычные int[] поля.
        if (idx > Integer.MAX_VALUE) {
            throw new IndexOutOfBoundsException("Linear index does not fit into int: i=" + idx);
        }
        return (int) idx;
    }

    @Override
    public int[] position(int index) {
        final long count = cellCount();
        if (index < 0 || (long) index >= count) {
            throw new IndexOutOfBoundsException("Index out of range: i=" + index);
        }

        final int nx = n(Axis3D.X);
        final int ny = n(Axis3D.Y);

        final long plane = (long) nx * (long) ny; // число ячеек в XY-слое

        int z = (int) ((long) index / plane);
        int rem = (int) ((long) index - plane * (long) z);

        int y = rem / nx;
        int x = rem % nx;

        return new int[] { x, y, z };
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return x >= 0 && x < n(Axis3D.X)
                && y >= 0 && y < n(Axis3D.Y)
                && z >= 0 && z < n(Axis3D.Z);
    }

    @Override
    public Grid2D faceGrid(Face face) {
        return faceGrids.get(face);
    }

    @Override
    public AxisGrid axis(Axis3D axis3d) {
        return axesGrids.get(axis3d);
    }
}