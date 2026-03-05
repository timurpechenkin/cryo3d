package io.github.timurpechenkin.domain.grid;

import java.util.EnumMap;

import io.github.timurpechenkin.geometry.Axis2D;

public class VirtualGrid2D implements Grid2D {
    private final EnumMap<Axis2D, AxisGrid> axesGrids;

    public VirtualGrid2D(EnumMap<Axis2D, AxisGrid> axesGrids) {
        this.axesGrids = axesGrids;
    }

    @Override
    public long cellCount() {
        return (long) n(Axis2D.H) * (long) n(Axis2D.W);
    }

    @Override
    public int n(Axis2D axis2d) {
        return axesGrids.get(axis2d).cells();
    }

    @Override
    public int[] edgesScaled(Axis2D axis2d) {
        return axesGrids.get(axis2d).edgesScaled();
    }

    @Override
    public int[] centersScaled2(Axis2D axis2d) {
        return axesGrids.get(axis2d).centersScaled2();
    }

    @Override
    public int[] stepsScaled(Axis2D axis2d) {
        return axesGrids.get(axis2d).stepsScaled();
    }

    @Override
    public int centerScaled2(Axis2D axis2d, int p) {
        return axesGrids.get(axis2d).centersScaled2()[p];
    }

    @Override
    public int findCellScaled(Axis2D axis2d, int c) {
        int edges[] = axesGrids.get(axis2d).edgesScaled();
        int n = edges.length - 1;
        if (c < edges[0] || c > edges[n])
            throw new IndexOutOfBoundsException("This coordinate is out of bound for this axis");
        if (c == edges[n])
            return n - 1;

        int left = 0, right = n - 1;
        while (left <= right) {
            int mid = (left + right) >>> 1;
            int a = edges[mid];
            int b = edges[mid + 1];
            if (c < a)
                right = mid - 1;
            else if (c >= b)
                left = mid + 1;
            else
                return mid;
        }
        throw new IndexOutOfBoundsException("This coordinate is out of bound for this axis");
    }

    @Override
    public int sizeScaled(Axis2D axis2d) {
        return axesGrids.get(axis2d).sizeScaled();
    }

    @Override
    public int index(int w, int h) {
        if (w < 0 || w >= n(Axis2D.W)) {
            throw new IndexOutOfBoundsException("Column out of range: " + w);
        }
        if (h < 0 || h >= n(Axis2D.H)) {
            throw new IndexOutOfBoundsException("Row out of range: " + h);
        }

        return w + n(Axis2D.W) * h;
    }

    @Override
    public int[] position(int index) {
        if (index < 0 || index >= cellCount()) {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
        }

        int width = n(Axis2D.W);
        int h = index / width;
        int w = index % width;

        return new int[] { w, h };
    }

    @Override
    public boolean contains(int w, int h) {
        return w >= 0 && w < n(Axis2D.W) && h >= 0 && h < n(Axis2D.H);
    }

    @Override
    public int lengthScaled(Axis2D axis2d) {
        return stepsScaled(axis2d)[n(axis2d)];
    }

}
