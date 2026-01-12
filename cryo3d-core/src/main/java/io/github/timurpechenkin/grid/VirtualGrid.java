package io.github.timurpechenkin.grid;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static io.github.timurpechenkin.Constants.SCALE;

import io.github.timurpechenkin.casefile.dto.grid.GridSettings;
import io.github.timurpechenkin.casefile.dto.grid.Segment;
import io.github.timurpechenkin.geometry.Axis;

public class VirtualGrid {
    private final EnumMap<Axis, AxisGrid> axesGrids;

    public VirtualGrid(EnumMap<Axis, AxisGrid> axesGrids) {
        this.axesGrids = axesGrids;
    }

    public static VirtualGrid from(GridSettings grid) {
        Map<Axis, List<Segment>> axesSegments = grid.axesSegments();

        EnumMap<Axis, AxisGrid> axesGrids = new EnumMap<>(Axis.class);
        for (Axis axis : Axis.values()) {
            List<Segment> segments = axesSegments.get(axis);
            axesGrids.put(axis, buildAxisGrid(axis, segments));
        }
        return new VirtualGrid(axesGrids);
    }

    private static AxisGrid buildAxisGrid(Axis axis, List<Segment> segments) {
        if (segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException("No segments for axis " + axis);
        }

        // 1) Считаем количество ячеек строго в scaled-int
        int cells = 0;
        for (Segment s : segments) {
            cells += segmentCellsScaled(s, axis);
        }

        int[] edges = new int[cells + 1];
        int[] steps = new int[cells];
        int[] centers = new int[cells];

        // 2) Заполняем строго в int, без накопления double
        int idx = 0;

        // Стартовая граница
        edges[0] = toScaled(segments.get(0).from());

        // Защита стыковки сегментов (на всякий случай)
        int expectedFrom = edges[0];

        for (int si = 0; si < segments.size(); si++) {
            Segment s = segments.get(si);

            int from = toScaled(s.from());
            int to = toScaled(s.to());
            int step = toScaled(s.step());

            if (from != expectedFrom) {
                throw new IllegalArgumentException(
                        "Segments are not contiguous for axis " + axis +
                                ": expected from=" + fromMeters(expectedFrom) +
                                " but got from=" + fromMeters(from));
            }

            int n = (to - from) / step;

            for (int i = 0; i < n; i++) {
                int left = edges[idx];
                int right = left + step;

                steps[idx] = step;
                centers[idx] = left + step / 2; // центр в scaled
                edges[idx + 1] = right;
                idx++;
            }

            // Жёстко фиксируем границу на "to"
            edges[idx] = to;
            expectedFrom = to;
        }

        if (idx != cells) {
            throw new IllegalStateException("Filled " + idx + " cells but expected " + cells);
        }

        return new AxisGrid(edges, centers, steps);
    }

    private static int segmentCellsScaled(Segment s, Axis axis) {
        int from = toScaled(s.from());
        int to = toScaled(s.to());
        int step = toScaled(s.step());

        if (step <= 0) {
            throw new IllegalArgumentException("Non-positive step for axis " + axis + ": " + s.step());
        }
        int len = to - from;
        if (len <= 0) {
            throw new IllegalArgumentException(
                    "Non-positive segment length for axis " + axis + ": from=" + s.from() + " to=" + s.to());
        }
        if (len % step != 0) {
            throw new IllegalArgumentException(
                    "Segment is not divisible by step for axis " + axis +
                            ": (to-from)=" + fromMeters(len) + " step=" + fromMeters(step) +
                            " (scaled len=" + len + ", step=" + step + ")");
        }
        return len / step;
    }

    // ----- Доступ к осям -----

    public AxisGrid x() {
        return axesGrids.get(Axis.X);
    }

    public AxisGrid y() {
        return axesGrids.get(Axis.Y);
    }

    public AxisGrid z() {
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

    // Объём

    public double cellVolumeMeters3(int i, int j, int k) {
        return x().stepMeters(i) * y().stepMeters(j) * z().stepMeters(k);
    }

    // Поиск индекса ячейки

    public int findCellX(double xMeters) {
        return findCellScaled(x().edgesScaled(), toScaled(xMeters));
    }

    public int findCellY(double yMeters) {
        return findCellScaled(y().edgesScaled(), toScaled(yMeters));
    }

    public int findCellZ(double zMeters) {
        return findCellScaled(z().edgesScaled(), toScaled(zMeters));
    }

    // ----- ВНУТРЕННИЙ API (scaled int) -----

    // Центр i-ой ячейки

    public int centerXScaled(int i) {
        return x().centersScaled()[i];
    }

    public int centerYScaled(int j) {
        return y().centersScaled()[j];
    }

    public int centerZScaled(int k) {
        return z().centersScaled()[k];
    }

    // Объём

    public int cellVolumeScaled3(int i, int j, int k) {
        long v = (long) x().stepsScaled()[i] * y().stepsScaled()[j] * z().stepsScaled()[k];
        if (v > Integer.MAX_VALUE) {
            throw new ArithmeticException("Scaled volume overflow; use meters^3 or long");
        }
        return (int) v;
    }

    // Поиск индекса ячейки

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

    // ----- КОНВЕРТАЦИЯ -----

    private static int toScaled(double meters) {
        return (int) Math.round(meters * SCALE);
    }

    private static double fromMeters(int scaled) {
        return scaled / (double) SCALE;
    }
}
