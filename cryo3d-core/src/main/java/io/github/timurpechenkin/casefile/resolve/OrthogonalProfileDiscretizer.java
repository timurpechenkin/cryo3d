package io.github.timurpechenkin.casefile.resolve;

import static io.github.timurpechenkin.geometry.GeometryScale.*;

import java.util.EnumMap;
import java.util.Objects;

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

/**
 * Дискретизатор ортогонального профиля.
 *
 * <p>
 * Профиль задаётся отрезком от точки A до точки B и осью {@code axisParallel}.
 * На их основе строится плоскость профиля:
 * <ul>
 * <li>ось {@code W} направлена вдоль отрезка A-B;</li>
 * <li>ось {@code H} параллельна оси {@code axisParallel};</li>
 * <li>третья ось 3D-сетки является фиксированной и задаёт положение плоскости
 * в пространстве.</li>
 * </ul>
 *
 * <p>
 * Поддерживается только ортогональный случай, когда отрезок A-B параллелен
 * одной из осей 3D-сетки и не параллелен {@code axisParallel}.
 *
 * <p>
 * Дискретизация профиля строится следующим образом:
 * <ul>
 * <li>дискретизация оси {@code W} наследуется от той оси 3D-сетки, вдоль
 * которой направлен отрезок A-B;</li>
 * <li>дискретизация оси {@code H} совпадает с дискретизацией оси
 * {@code axisParallel};</li>
 * <li>для каждой ячейки 2D-сетки профиля определяется индекс ячейки 3D-сетки,
 * объём которой пересекается плоскостью профиля.</li>
 * </ul>
 *
 * <p>
 * Выбор позиции ячейки по фиксированной оси выполняется по той же
 * полуинтервальной конвенции, что и в
 * {@link Grid3D#findCellScaled(Axis3D, int)}:
 * ячейка с позицией {@code p} занимает интервал
 * [{@code edge[p]}, {@code edge[p+1]}).
 *
 * <p>
 * В результирующем массиве {@code cellIndex} индексы хранятся в порядке
 * обхода 2D-сетки профиля по формуле:
 * {@code index2d = w + width * h}.
 */
public class OrthogonalProfileDiscretizer implements ProfileDiscretizer {

    @Override
    public Profile discretize(Grid3D grid3d, ProfileDto profileDto) {
        Objects.requireNonNull(grid3d, "grid3d");
        Objects.requireNonNull(profileDto, "profileDto");
        Objects.requireNonNull(profileDto.pointA(), "profileDto.pointA()");
        Objects.requireNonNull(profileDto.pointB(), "profileDto.pointB()");

        String name = profileDto.name();
        int saveStep = profileDto.saveStep();
        Point3D pointA = toPoint3d(profileDto.pointA());
        Point3D pointB = toPoint3d(profileDto.pointB());
        Axis3D axisParallel = profileDto.axisParallel();

        Objects.requireNonNull(name, "profileDto.name()");
        Objects.requireNonNull(axisParallel, "profileDto.axisParallel()");

        validatePoints(pointA, pointB, axisParallel);

        Axis3D axisW = resolveAxisW(pointA, pointB, axisParallel);
        Axis3D axisFixed = resolveAxisFixed(axisW, axisParallel);

        int fixedCoordinateScaled = coordinateScaled(pointA, axisFixed);
        int fixedPosition = grid3d.findCellScaled(axisFixed, fixedCoordinateScaled);

        int[] wPositions = buildWPositions(grid3d, pointA, pointB, axisW);
        AxisGrid wAxisGrid = buildWAxisGrid(grid3d.axis(axisW), wPositions);

        AxisGrid hAxisGrid = grid3d.axis(axisParallel);
        Grid2D grid2d = buildGrid2d(wAxisGrid, hAxisGrid);

        int[] cellIndex = buildCellIndex(grid3d, grid2d, wPositions, axisW, axisParallel, axisFixed, fixedPosition);

        return new Profile(
                name,
                saveStep,
                pointA,
                pointB,
                grid2d,
                axisParallel,
                cellIndex);
    }

    private Point3D toPoint3d(PointDto dto) {
        int xScaled = metersToScaled(dto.x());
        int yScaled = metersToScaled(dto.y());
        int zScaled = metersToScaled(dto.z());
        return new Point3D(xScaled, yScaled, zScaled);
    }

    /**
     * Проверяет геометрию профиля.
     *
     * <p>
     * Допустимый профиль должен удовлетворять следующим условиям:
     * <ul>
     * <li>точки A и B не совпадают;</li>
     * <li>отрезок A-B должен быть параллелен одной из осей 3D-сетки;</li>
     * <li>отрезок A-B не должен быть параллелен {@code axisParallel};</li>
     * <li>координата точек A и B по оси {@code axisParallel} должна совпадать.</li>
     * </ul>
     */
    private void validatePoints(Point3D pointA, Point3D pointB, Axis3D axisParallel) {
        boolean sameX = pointA.xScaled() == pointB.xScaled();
        boolean sameY = pointA.yScaled() == pointB.yScaled();
        boolean sameZ = pointA.zScaled() == pointB.zScaled();

        if (sameX && sameY && sameZ) {
            throw new IllegalArgumentException("Profile points A and B must not coincide");
        }

        int differentAxes = 0;
        if (!sameX)
            differentAxes++;
        if (!sameY)
            differentAxes++;
        if (!sameZ)
            differentAxes++;

        if (differentAxes != 1) {
            throw new IllegalArgumentException(
                    "Profile segment A-B must be parallel to exactly one 3D axis");
        }

        if (coordinateScaled(pointA, axisParallel) != coordinateScaled(pointB, axisParallel)) {
            throw new IllegalArgumentException(
                    "Profile points A and B must have the same coordinate on axisParallel");
        }

        Axis3D axisW = resolveAxisWUnchecked(pointA, pointB);
        if (axisW == axisParallel) {
            throw new IllegalArgumentException(
                    "Profile segment A-B must not be parallel to axisParallel");
        }
    }

    /**
     * Определяет ось {@code W}.
     *
     * <p>
     * Ось {@code W} совпадает с той осью 3D-сетки, по которой координаты точек
     * A и B различаются.
     */
    private Axis3D resolveAxisW(Point3D pointA, Point3D pointB, Axis3D axisParallel) {
        Axis3D axisW = resolveAxisWUnchecked(pointA, pointB);
        if (axisW == axisParallel) {
            throw new IllegalArgumentException(
                    "Profile segment A-B must not be parallel to axisParallel");
        }
        return axisW;
    }

    /**
     * Определяет ось, по которой координаты точек A и B различаются.
     *
     * <p>
     * Метод предполагает, что точки A и B уже прошли базовую валидацию.
     */
    private Axis3D resolveAxisWUnchecked(Point3D pointA, Point3D pointB) {
        if (pointA.xScaled() != pointB.xScaled()) {
            return Axis3D.X;
        }
        if (pointA.yScaled() != pointB.yScaled()) {
            return Axis3D.Y;
        }
        if (pointA.zScaled() != pointB.zScaled()) {
            return Axis3D.Z;
        }
        throw new IllegalArgumentException("Profile points A and B must not coincide");
    }

    /**
     * Определяет фиксированную ось плоскости профиля.
     *
     * <p>
     * Плоскость профиля задаётся двумя осями:
     * <ul>
     * <li>{@code W} — вдоль отрезка A-B;</li>
     * <li>{@code H} — вдоль {@code axisParallel}.</li>
     * </ul>
     *
     * <p>
     * Третья ось является фиксированной: по ней плоскость имеет постоянную
     * координату.
     */
    private Axis3D resolveAxisFixed(Axis3D axisW, Axis3D axisParallel) {
        for (Axis3D axis : Axis3D.values()) {
            if (axis != axisW && axis != axisParallel) {
                return axis;
            }
        }
        throw new IllegalStateException(
                "Could not resolve fixed axis for axisW=" + axisW + ", axisParallel=" + axisParallel);
    }

    /**
     * Строит позиции ячеек по оси {@code W}.
     *
     * <p>
     * Порядок позиций всегда соответствует направлению от точки A к точке B.
     * Это означает, что при движении от A к B в сторону убывания координаты
     * позиции также будут перечислены в обратном порядке.
     *
     * <p>
     * Выбор позиции ячейки по координате выполняется через
     * {@link Grid3D#findCellScaled(Axis3D, int)}, то есть с полуинтервальной
     * конвенцией:
     * ячейка с позицией {@code p} занимает интервал
     * [{@code edge[p]}, {@code edge[p+1]}).
     */
    private int[] buildWPositions(Grid3D grid3d, Point3D pointA, Point3D pointB, Axis3D axisW) {
        int coordinateA = coordinateScaled(pointA, axisW);
        int coordinateB = coordinateScaled(pointB, axisW);

        int positionA = grid3d.findCellScaled(axisW, coordinateA);
        int positionB = grid3d.findCellScaled(axisW, coordinateB);

        int count = Math.abs(positionB - positionA) + 1;
        int[] positions = new int[count];

        int step = Integer.compare(positionB, positionA);
        if (step == 0) {
            positions[0] = positionA;
            return positions;
        }

        int p = positionA;
        for (int i = 0; i < count; i++) {
            positions[i] = p;
            p += step;
        }

        return positions;
    }

    /**
     * Строит 2D-сетку профиля.
     *
     * <p>
     * Ось {@code W} задаётся локальной координатой вдоль отрезка A-B.
     * Её шаги полностью соответствуют шагам ячеек исходной 3D-сетки,
     * через которые проходит отрезок A-B, и перечислены в порядке от A к B.
     *
     * <p>
     * Ось {@code H} совпадает с осью {@code axisParallel} исходной 3D-сетки.
     */
    private Grid2D buildGrid2d(AxisGrid wAxisGrid, AxisGrid hAxisGrid) {
        EnumMap<Axis2D, AxisGrid> axes = new EnumMap<>(Axis2D.class);
        axes.put(Axis2D.W, wAxisGrid);
        axes.put(Axis2D.H, hAxisGrid);
        return new VirtualGrid2D(axes);
    }

    /**
     * Строит ось {@code W} профиля.
     *
     * <p>
     * Координата по оси {@code W} является локальной координатой в плоскости
     * профиля. Начало оси находится в точке {@code A} в смысле направления
     * обхода ячеек от A к B.
     *
     * <p>
     * При этом дискретизация оси {@code W} полностью наследует длины ячеек
     * исходной 3D-оси, вдоль которой проходит отрезок A-B.
     */
    private AxisGrid buildWAxisGrid(AxisGrid sourceAxisGrid, int[] wPositions) {
        int cells = wPositions.length;

        int[] stepsScaled = new int[cells];
        int[] edgesScaled = new int[cells + 1];
        int[] centersScaled2 = new int[cells];

        edgesScaled[0] = 0;

        for (int p = 0; p < cells; p++) {
            int sourcePosition = wPositions[p];
            int stepScaled = sourceAxisGrid.stepsScaled()[sourcePosition];

            stepsScaled[p] = stepScaled;
            edgesScaled[p + 1] = edgesScaled[p] + stepScaled;
            centersScaled2[p] = edgesScaled[p] * 2 + stepScaled;
        }

        return new AxisGrid(edgesScaled, centersScaled2, stepsScaled);
    }

    /**
     * Строит массив индексов 3D-ячеек, принадлежащих плоскости профиля.
     *
     * <p>
     * Индексы 2D-сетки профиля вычисляются по формуле:
     * {@code index2d = w + width * h}.
     *
     * <p>
     * Значение {@code cellIndex[index2d]} содержит индекс ячейки 3D-сетки,
     * вычисленный по формуле:
     * {@code index3d = x + nx * (y + ny * z)}.
     *
     * <p>
     * По фиксированной оси всегда выбирается одна позиция ячейки, найденная по
     * координате плоскости через {@link Grid3D#findCellScaled(Axis3D, int)}.
     */
    private int[] buildCellIndex(
            Grid3D grid3d,
            Grid2D grid2d,
            int[] wPositions,
            Axis3D axisW,
            Axis3D axisH,
            Axis3D axisFixed,
            int fixedPosition) {

        int nW = grid2d.n(Axis2D.W);
        int nH = grid2d.n(Axis2D.H);

        int[] cellIndex = new int[nW * nH];

        for (int h = 0; h < nH; h++) {
            for (int w = 0; w < nW; w++) {
                int positionW = wPositions[w];
                int positionH = h;

                int x = axisPosition(Axis3D.X, axisW, positionW, axisH, positionH, axisFixed, fixedPosition);
                int y = axisPosition(Axis3D.Y, axisW, positionW, axisH, positionH, axisFixed, fixedPosition);
                int z = axisPosition(Axis3D.Z, axisW, positionW, axisH, positionH, axisFixed, fixedPosition);

                int index2d = grid2d.index(w, h);
                cellIndex[index2d] = grid3d.index(x, y, z);
            }
        }

        return cellIndex;
    }

    /**
     * Возвращает позицию ячейки по указанной оси 3D-сетки.
     *
     * @param axis          ось, для которой требуется определить позицию
     * @param axisW         ось {@code W} профиля
     * @param positionW     позиция ячейки по оси {@code W}
     * @param axisH         ось {@code H} профиля
     * @param positionH     позиция ячейки по оси {@code H}
     * @param axisFixed     фиксированная ось плоскости профиля
     * @param fixedPosition позиция ячейки по фиксированной оси
     */
    private int axisPosition(
            Axis3D axis,
            Axis3D axisW,
            int positionW,
            Axis3D axisH,
            int positionH,
            Axis3D axisFixed,
            int fixedPosition) {

        if (axis == axisW) {
            return positionW;
        }
        if (axis == axisH) {
            return positionH;
        }
        if (axis == axisFixed) {
            return fixedPosition;
        }
        throw new IllegalStateException("Unknown axis mapping: " + axis);
    }

    /**
     * Возвращает координату точки по выбранной оси.
     */
    private int coordinateScaled(Point3D point, Axis3D axis) {
        return switch (axis) {
            case X -> point.xScaled();
            case Y -> point.yScaled();
            case Z -> point.zScaled();
        };
    }
}