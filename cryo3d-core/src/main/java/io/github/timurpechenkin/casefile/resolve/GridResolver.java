package io.github.timurpechenkin.casefile.resolve;

import static io.github.timurpechenkin.geometry.GeometryScale.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import io.github.timurpechenkin.casefile.dto.grid.GridSpecDto;
import io.github.timurpechenkin.casefile.dto.grid.Segment;
import io.github.timurpechenkin.geometry.Axis;
import io.github.timurpechenkin.grid.AxisGrid;
import io.github.timurpechenkin.grid.VirtualGrid;

public class GridResolver {

    public static VirtualGrid virtualGridFrom(GridSpecDto grid) {
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
        edges[0] = metersToScaled(segments.get(0).from());

        // Защита стыковки сегментов (на всякий случай)
        int expectedFrom = edges[0];

        for (int si = 0; si < segments.size(); si++) {
            Segment s = segments.get(si);

            int from = metersToScaled(s.from());
            int to = metersToScaled(s.to());
            int step = metersToScaled(s.step());

            if (from != expectedFrom) {
                throw new IllegalArgumentException(
                        "Segments are not contiguous for axis " + axis +
                                ": expected from=" + scaledToMeters(expectedFrom) +
                                " but got from=" + scaledToMeters(from));
            }

            int n = (to - from) / step;

            for (int i = 0; i < n; i++) {
                int left = edges[idx];
                int right = left + step;

                steps[idx] = step;
                centers[idx] = 2 * left + step;
                edges[idx + 1] = right;
                idx++;
            }

            expectedFrom = to;
        }

        if (idx != cells) {
            throw new IllegalStateException("Filled " + idx + " cells but expected " + cells);
        }

        return new AxisGrid(edges, centers, steps);
    }

    private static int segmentCellsScaled(Segment s, Axis axis) {
        int from = metersToScaled(s.from());
        int to = metersToScaled(s.to());
        int step = metersToScaled(s.step());

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
                            ": (to-from)=" + scaledToMeters(len) + " step=" + scaledToMeters(step) +
                            " (scaled len=" + len + ", step=" + step + ")");
        }
        return len / step;
    }
}
