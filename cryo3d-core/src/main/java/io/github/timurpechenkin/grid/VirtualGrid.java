package io.github.timurpechenkin.grid;

import java.util.EnumMap;

import static io.github.timurpechenkin.geometry.GeometryScale.*;
import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.geometry.Axis;

public class VirtualGrid implements Grid {
    private final EnumMap<Axis, AxisGrid> axesGrids;

    public VirtualGrid(EnumMap<Axis, AxisGrid> axesGrids) {
        this.axesGrids = axesGrids;
    }

    // ----- Доступ к осям -----

    private AxisGrid x() {
        return axesGrids.get(Axis.X);
    }

    private AxisGrid y() {
        return axesGrids.get(Axis.Y);
    }

    private AxisGrid z() {
        return axesGrids.get(Axis.Z);
    }

    public int nx() {
        return x().cells();
    }

    public int ny() {
        return y().cells();
    }

    public int nz() {
        return z().cells();
    }

    public long cellCount() {
        return (long) nx() * ny() * nz();
    }

    // ----- ВНЕШНИЙ API (метры double) -----

    // Центр i-ой ячейки

    public double centerXMeters(int i) {
        return x().centerMeters(i);
    }

    public double centerYMeters(int j) {
        return y().centerMeters(j);
    }

    public double centerZMeters(int k) {
        return z().centerMeters(k);
    }

    // Объём ячейки по индексам

    public double cellVolumeMeters3(int i, int j, int k) {
        return x().stepMeters(i) * y().stepMeters(j) * z().stepMeters(k);
    }

    // Поиск индекса ячейки по координате

    public int findCellX(double xMeters) {
        return findCellScaled(x().edgesScaled(), toScaled(xMeters));
    }

    public int findCellY(double yMeters) {
        return findCellScaled(y().edgesScaled(), toScaled(yMeters));
    }

    public int findCellZ(double zMeters) {
        return findCellScaled(z().edgesScaled(), toScaled(zMeters));
    }

    // Длинна осей

    public double sizeMetersX() {
        return toMeters(edgesScaledX()[nx()] - edgesScaledX()[0]);
    }

    public double sizeMetersY() {
        return toMeters(edgesScaledY()[ny()] - edgesScaledY()[0]);
    }

    public double sizeMetersZ() {
        return toMeters(edgesScaledZ()[nz()] - edgesScaledZ()[0]);
    }

    // ----- ВНУТРЕННИЙ API (scaled int) -----

    // Центр i-ой ячейки

    public int centerXScaled2(int i) {
        return x().centersScaled2()[i];
    }

    public int centerYScaled2(int j) {
        return y().centersScaled2()[j];
    }

    public int centerZScaled2(int k) {
        return z().centersScaled2()[k];
    }

    // Объём ячейки по индексам

    public long cellVolumeScaled3(int i, int j, int k) {
        long v = (long) x().stepsScaled()[i] * y().stepsScaled()[j] * z().stepsScaled()[k];
        return v;
    }

    // Поиск индекса ячейки по координате*SCALE

    public int findCellXScaled(int x) {
        return findCellScaled(x().edgesScaled(), x);
    }

    public int findCellYScaled(int y) {
        return findCellScaled(y().edgesScaled(), y);
    }

    public int findCellZScaled(int z) {
        return findCellScaled(z().edgesScaled(), z);
    }

    private static int findCellScaled(int[] edges, int target) {
        int n = edges.length - 1;
        if (target < edges[0] || target > edges[n])
            return -1;
        if (target == edges[n])
            return n - 1;

        int left = 0, right = n - 1;
        while (left <= right) {
            int mid = (left + right) >>> 1;
            int a = edges[mid];
            int b = edges[mid + 1];
            if (target < a)
                right = mid - 1;
            else if (target >= b)
                left = mid + 1;
            else
                return mid;
        }
        return -1;
    }

    // Масивы координат*SCALE ячеек по осям

    // Для Х

    /**
     * Координаты ребер ячеек (edges.length = cells + 1).
     * edges[i] - левый край i-й ячейки, edges[i+1] - правый. Умножены на SCALE.
     */
    public int[] edgesScaledX() {
        return x().edgesScaled();
    }

    /** Центры ячеек (centers.length = cells). Умножены на SCALE*2. */
    public int[] centersScaled2X() {
        return x().centersScaled2();
    }

    /** Длины ячеек (steps.length = cells). Умножены на SCALE. */
    public int[] stepsScaledX() {
        return x().stepsScaled();
    }

    // Для Y

    /**
     * Координаты ребер ячеек (edges.length = cells + 1).
     * edges[i] - левый край i-й ячейки, edges[i+1] - правый. Умножены на SCALE.
     */
    public int[] edgesScaledY() {
        return y().edgesScaled();
    }

    /** Центры ячеек (centers.length = cells). Умножены на SCALE*2. */
    public int[] centersScaled2Y() {
        return y().centersScaled2();
    }

    /** Длины ячеек (steps.length = cells). Умножены на SCALE. */
    public int[] stepsScaledY() {
        return y().stepsScaled();
    }

    // Для Z

    /**
     * Координаты ребер ячеек (edges.length = cells + 1).
     * edges[i] - левый край i-й ячейки, edges[i+1] - правый. Умножены на SCALE.
     */
    public int[] edgesScaledZ() {
        return z().edgesScaled();
    }

    /** Центры ячеек (centers.length = cells). Умножены на SCALE*2. */
    public int[] centersScaled2Z() {
        return z().centersScaled2();
    }

    /** Длины ячеек (steps.length = cells). Умножены на SCALE. */
    public int[] stepsScaledZ() {
        return z().stepsScaled();
    }

    // Длинна осей

    public int sizeScaledX() {
        return edgesScaledX()[nx()] - edgesScaledX()[0];
    }

    public int sizeScaledY() {
        return edgesScaledY()[ny()] - edgesScaledY()[0];
    }

    public int sizeScaledZ() {
        return edgesScaledZ()[nz()] - edgesScaledZ()[0];
    }
}
