package io.github.timurpechenkin.casefile.resolve;

import static io.github.timurpechenkin.geometry.GeometryScale.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import io.github.timurpechenkin.casefile.dto.recording.PointDto;
import io.github.timurpechenkin.casefile.dto.recording.ProfileDto;
import io.github.timurpechenkin.domain.grid.AxisGrid;
import io.github.timurpechenkin.domain.grid.Grid2D;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.grid.VirtualGrid2D;
import io.github.timurpechenkin.domain.recording.Profile;
import io.github.timurpechenkin.geometry.Axis2D;
import io.github.timurpechenkin.geometry.Axis3D;
import io.github.timurpechenkin.geometry.Point3D;

public class SkewZProfileDiscretizer implements ProfileDiscretizer {
    /**
     * Не используется в данный момент.
     * 
     * Дискретизирует профиль (вертикальную плоскость), проходящий через 3D-сетку.
     *
     * <p>
     * <b>Геометрия профиля</b>:
     * профиль задаётся двумя точками {@code A(xA,yA,zA)} и {@code B(xB,yB,zB)}.
     * В текущей реализации используется вертикальная плоскость, определяемая
     * отрезком {@code Axy–Bxy} в горизонтальной плоскости XY и направлением оси Z
     * сетки.
     * Координаты {@code zA} и {@code zB} не влияют на форму профиля:
     * по высоте берётся вся дискретизация оси Z исходной 3D-сетки.
     *
     * <p>
     * <b>Построение дискретизации</b>:
     * <ul>
     * <li>
     * В плоскости XY выполняется трассировка отрезка {@code Axy–Bxy} по ячейкам
     * структурированной сетки. На каждом шаге выбирается ближайшее следующее
     * пересечение отрезка с ребром
     * сетки по оси X или Y, после чего выполняется переход в соседнюю XY-ячейку
     * (при одновременном
     * пересечении двух рёбер — переход по обеим осям).
     * </li>
     * <li>Одновременно строится ось {@code W} профиля как набор рёбер
     * {@code edgesWScaled} (в SCALE),
     * где {@code edgesWScaled[0] = 0}, {@code edgesWScaled[last] = length(A,B)}.
     * Каждому интервалу {@code [edgesWScaled[p], edgesWScaled[p+1])} соответствует
     * одна XY-ячейка
     * {@code cells[p]}.</li>
     * <li>Формируется 2D-сетка профиля:
     * ось {@code H} совпадает с осью {@code Z} 3D-сетки,
     * ось {@code W} — расстояние вдоль отрезка {@code A→B}.</li>
     * <li>Формируется массив {@code cellIndex} длиной {@code grid2d.cellCount()},
     * который отображает каждую ячейку профиля {@code (w,h)} в линейный индекс
     * ячейки исходной 3D-сетки
     * {@code (px,py,z=h)}.</li>
     * </ul>
     *
     * @param grid3d     исходная 3D-сетка
     * @param profileDto описание профиля (точки A и B)
     * @return профиль с 2D-сеткой и отображением ячеек профиля в ячейки 3D-сетки
     *
     * @throws IllegalArgumentException  если точки A и B совпадают в XY или профиль
     *                                   вырожден
     * @throws IndexOutOfBoundsException если точки A или B выходят за пределы
     *                                   3D-сетки по X/Y
     */
    @Override
    public Profile discretize(Grid3D grid3d, ProfileDto profileDto) {
        String name = profileDto.name();
        Point3D pointA = toPoint3d(profileDto.pointA());
        Point3D pointB = toPoint3d(profileDto.pointB());

        // общая длина профиля (A->B) в SCALE
        double lengthMeters = Math.hypot(pointB.xMeters() - pointA.xMeters(), pointB.yMeters() - pointA.yMeters());
        int lengthScaled = metersToScaled(lengthMeters);

        ProfilePath2D path = tracePathXY(grid3d, pointA, pointB, lengthScaled);
        Grid2D grid2d = buildProfileGrid2D(path, grid3d);
        int[] cellIndex = buildCellIndex(path, grid2d, grid3d);

        return new Profile(name, profileDto.saveStep(), pointA, pointB, grid2d, Axis3D.Z, cellIndex);
    }

    private ProfilePath2D tracePathXY(Grid3D grid3d, Point3D a, Point3D b, int lengthScaled) {
        int ax = a.xScaled();
        int ay = a.yScaled();
        int bx = b.xScaled();
        int by = b.yScaled();

        if (ax == bx && ay == by) {
            throw new IllegalArgumentException("Profile A==B in XY");
        }

        // Стартовая и конечная ячейки (внутри сетки; findCellScaled бросит, если вне)
        int px = grid3d.findCellScaled(Axis3D.X, ax);
        int py = grid3d.findCellScaled(Axis3D.Y, ay);
        // int pxEnd = grid3d.findCellScaled(Axis3D.X, bx);
        // int pyEnd = grid3d.findCellScaled(Axis3D.Y, by);

        int[] xEdges = grid3d.edgesScaled(Axis3D.X); // SCALE
        int[] yEdges = grid3d.edgesScaled(Axis3D.Y); // SCALE

        int dx = bx - ax;
        int dy = by - ay;

        // Направление движения по осям (по ячейкам)
        int stepX = Integer.compare(dx, 0); // -1,0,+1
        int stepY = Integer.compare(dy, 0);

        double t = 0.0;

        // Списки результатов
        List<Integer> edgesW = new ArrayList<>();
        List<CellXY> cells = new ArrayList<>();

        // первое ребро профиля всегда в начале отрезка
        edgesW.add(0);

        // Защита от бесконечного цикла (если что-то пойдёт совсем не так)
        int safety = (grid3d.n(Axis3D.X) + grid3d.n(Axis3D.Y) + 10) * 4;

        while (true) {
            if (safety-- <= 0) {
                throw new IllegalStateException("Profile traversal safety limit exceeded");
            }

            // Следующее пересечение по X:
            double tNextX = Double.POSITIVE_INFINITY;
            if (stepX != 0) {
                // Позиция ребра, которое мы пересечём следующим:
                // если идём вправо, то правое ребро текущей ячейки: e = px+1
                // если идём влево, то левое ребро текущей ячейки: e = px
                int eX = (stepX > 0) ? (px + 1) : px;
                int xEdge = xEdges[eX];
                tNextX = (double) (xEdge - ax) / (double) dx;
            }

            // Следующее пересечение по Y:
            double tNextY = Double.POSITIVE_INFINITY;
            if (stepY != 0) {
                int eY = (stepY > 0) ? (py + 1) : py;
                int yEdge = yEdges[eY];
                tNextY = (double) (yEdge - ay) / (double) dy;
            }

            // Берём ближайшее событие
            double tNext = Math.min(tNextX, tNextY);

            // Численная устойчивость
            if (!(tNext > t)) {
                // если из-за округления получили "не вперёд", двигаемся чуть-чуть
                tNext = Math.nextUp(t);
            }

            // Ограничиваемся [0,1]
            if (tNext > 1.0)
                tNext = 1.0;

            // Текущая ячейка соответствует интервалу [t, tNext)
            cells.add(new CellXY(px, py));

            // Добавляем ребро по W (в SCALE)
            int wEdge = (int) Math.round(tNext * (double) lengthScaled);

            // Обеспечим строгий рост ребер: edges[p+1] > edges[p]
            int last = edgesW.get(edgesW.size() - 1);
            if (wEdge <= last) {
                wEdge = last + 1;
            }
            if (wEdge > lengthScaled) {
                wEdge = lengthScaled;
            }
            edgesW.add(wEdge);

            // Если дошли до конца отрезка — выходим
            if (tNext >= 1.0 || wEdge >= lengthScaled) {
                break;
            }

            // Переходим в следующую ячейку: по X, по Y или по обеим (если угол)
            boolean crossX = Math.abs(tNext - tNextX) <= 1e-15;
            boolean crossY = Math.abs(tNext - tNextY) <= 1e-15;

            if (crossX)
                px += stepX;
            if (crossY)
                py += stepY;

            t = tNext;
        }

        // Заменяем последний edge и cell ровно до lengthScaled (если вдруг из-за
        // clamp/роста
        // не попали)
        int lastEdge = edgesW.get(edgesW.size() - 1);
        if (lastEdge != lengthScaled) {
            if (lastEdge > lengthScaled) {
                // крайне редко (из-за last+1), но на всякий случай
                edgesW.set(edgesW.size() - 1, lengthScaled);
            } else {
                edgesW.add(lengthScaled);
            }
        }

        // Приводим к массивам
        int[] edgesArr = edgesW.stream().mapToInt(Integer::intValue).toArray();
        CellXY[] cellsArr = cells.toArray(CellXY[]::new);

        // Инвариант: edges = cells + 1
        if (edgesArr.length != cellsArr.length + 1) {
            throw new IllegalStateException("Traversal invariant broken: edges.length = " + edgesArr.length
                    + ", cells.length = " + cellsArr.length);
        }

        return new ProfilePath2D(edgesArr, cellsArr);
    }

    private Grid2D buildProfileGrid2D(ProfilePath2D path, Grid3D grid3d) {
        EnumMap<Axis2D, AxisGrid> axes = new EnumMap<>(Axis2D.class);

        // H = Z ось 3D-сетки (вертикальное направление профиля)
        axes.put(Axis2D.H, grid3d.axis(Axis3D.Z));

        // точки пересечения границ ячеек по осям X и Y линией профиля
        int[] edgesScaled = path.edgesWScaled();
        int n = edgesScaled.length - 1;
        int[] stepsScaled = new int[n];
        int[] centersScaled2 = new int[n];

        // шаги отрезков = разница между w координатами правого и левого ребр,
        // удвоенный центр = сумма w координаты правого и левого ребра
        for (int p = 0; p < n; p++) {
            int left = edgesScaled[p];
            int right = edgesScaled[p + 1];
            stepsScaled[p] = right - left;
            centersScaled2[p] = right + left; // SCALED2
        }

        AxisGrid axisW = new AxisGrid(edgesScaled, centersScaled2, stepsScaled);
        axes.put(Axis2D.W, axisW);

        return new VirtualGrid2D(axes);
    }

    private int[] buildCellIndex(ProfilePath2D path, Grid2D grid2d, Grid3D grid3d) {
        int nWidth = grid2d.n(Axis2D.W);
        int nHeight = grid2d.n(Axis2D.H);

        // Заполняем массив индексов ячеек по контракту idx = w + width*h
        CellXY[] cells = path.cells();
        if (cells.length != nWidth) {
            throw new IllegalStateException("Profile width mismatch: cells.length != grid2d.n(W)");
        }

        int[] cellIndex = new int[nWidth * nHeight];
        for (int h = 0; h < nHeight; h++) {
            for (int w = 0; w < nWidth; w++) {
                CellXY c = cells[w];
                int idx3d = grid3d.index(c.px(), c.py(), h);
                int idx2d = grid2d.index(w, h);
                cellIndex[idx2d] = idx3d;
            }
        }
        return cellIndex;
    }

    private Point3D toPoint3d(PointDto dto) {
        return new Point3D(metersToScaled(dto.x()), metersToScaled(dto.y()), metersToScaled(dto.z()));
    }

    private record ProfilePath2D(
            int[] edgesWScaled, // длина N+1, edgesWScaled[0]=0, edgesWScaled[N]=lengthScaled
            CellXY[] cells // длина N, cells[p] соответствует [edges[p], edges[p+1])
    ) {
    }

    private record CellXY(int px, int py) {

        @Override
        public String toString() {
            return "(" + px + ", " + py + ")";
        }

    }
}
