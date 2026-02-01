package io.github.timurpechenkin.domain.model;

import io.github.timurpechenkin.domain.grid.Grid;

public abstract class AbstractField3D {
    private final int nx, ny, nz;

    public AbstractField3D(Grid grid) {
        nx = grid.nx();
        ny = grid.ny();
        nz = grid.nz();
    }

    public int nx() {
        return nx;
    }

    public int ny() {
        return ny;
    }

    public int nz() {
        return nz;
    }

    public int index(int x, int y, int z) {
        if (x < 0 || x >= nx)
            throw new IndexOutOfBoundsException("x out of range: " + x);
        if (y < 0 || y >= ny)
            throw new IndexOutOfBoundsException("y out of range: " + y);
        if (z < 0 || z >= nz)
            throw new IndexOutOfBoundsException("z out of range: " + z);
        return x + nx * (y + ny * z);
    }
}
