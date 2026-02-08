package io.github.timurpechenkin.casefile.resolve;

import static io.github.timurpechenkin.geometry.GeometryScale.*;

import java.util.ArrayList;
import java.util.List;

import io.github.timurpechenkin.casefile.dto.measurement.PointDto;
import io.github.timurpechenkin.casefile.dto.measurement.ProfileDto;
import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.domain.measurement.Profile;
import io.github.timurpechenkin.domain.measurement.ProfileGrid;
import io.github.timurpechenkin.domain.model.Field2D;
import io.github.timurpechenkin.domain.model.Field3D;
import io.github.timurpechenkin.geometry.Point3D;

public class ProfileDiscretizer {
    public Profile discretize(Grid grid, ProfileDto profileDto, Field3D field3d) {
        String name = profileDto.name();
        Point3D pointA = toPoint3d(profileDto.pointA());
        Point3D pointB = toPoint3d(profileDto.pointB());

        List<Cell2D> path = getPath(grid, field3d, pointA, pointB);
        Field2D field2d = getField2d(path, grid);
        int[] cellIndex = getCellIndex(path, field3d, field2d);
        ProfileGrid profileGrid = discretizProfileGrid(path, field2d, grid);

        return new Profile(name, pointA, pointB, profileGrid, field2d, cellIndex);
    }

    private List<Cell2D> getPath(Grid grid, Field3D field3d, Point3D pointA, Point3D pointB) {
        if (pointA.xScaled() - pointB.xScaled() == 0 && pointA.yScaled() - pointB.yScaled() == 0) {
            throw new IllegalArgumentException("Profile A==B");
        }

        double ax = pointA.xMeters();
        double ay = pointA.yMeters();
        double bx = pointB.xMeters();
        double by = pointB.yMeters();

        double dx = bx - ax;
        double dy = by - ay;

        boolean xMain = Math.abs(dx) > Math.abs(dy);

        List<Cell2D> path = new ArrayList<>();

        if (xMain) {
            int ix0 = grid.findCellX(Math.min(ax, bx));
            int ix1 = grid.findCellX(Math.max(ax, bx));

            if (ix0 < 0 || ix1 < 0)
                throw new IllegalArgumentException("ix0 < 0 || ix1 < 0");

            for (int ix = ix0; ix <= ix1; ix++) {
                double x = grid.centerXMeters(ix);
                double t = (x - ax) / dx;

                if (t < 0 || t > 1)
                    continue;

                double y = ay + t * dy;
                int iy = grid.findCellY(y);

                if (iy < 0)
                    continue;

                Cell2D lastCell = path.isEmpty() ? null : path.get(path.size() - 1);
                if (lastCell == null || lastCell.ix() != ix || lastCell.iy() != iy) {
                    double w = t * Math.hypot(dx, dy);
                    path.add(new Cell2D(ix, iy, w));
                }
            }
        } else {
            int iy0 = grid.findCellY(Math.min(ay, by));
            int iy1 = grid.findCellY(Math.max(ay, by));

            if (iy0 < 0 || iy1 < 0)
                throw new IllegalArgumentException("iy0 < 0 || iy1 < 0");

            for (int iy = iy0; iy <= iy1; iy++) {
                double y = grid.centerYMeters(iy);

                double t = (y - ay) / dy;
                if (t < 0 || t > 1)
                    continue;

                double x = ax + t * dx;

                int ix = grid.findCellX(x);
                if (ix < 0)
                    continue;

                Cell2D lastCell = path.isEmpty() ? null : path.get(path.size() - 1);
                if (lastCell == null || lastCell.ix() != ix || lastCell.iy() != iy) {
                    double w = t * Math.hypot(dx, dy);
                    path.add(new Cell2D(ix, iy, w));
                }
            }
        }
        return path;
    }

    private Field2D getField2d(List<Cell2D> path, Grid grid) {
        int width = path.size();
        int heigth = grid.nz();
        return new Field2D(width, heigth);
    }

    private int[] getCellIndex(List<Cell2D> path, Field3D field3d, Field2D field2d) {
        int width = field2d.width();
        int heigth = field2d.height();

        // Заполняем массив индексов ячеек по контракту idx = w + width*h
        int[] cellIndex = new int[width * heigth];
        for (int h = 0; h < heigth; h++) {
            for (int w = 0; w < width; w++) {
                Cell2D cell2d = path.get(w);
                int idx3d = field3d.index(cell2d.ix(), cell2d.iy(), h);
                int idx2d = field2d.index(w, h);
                cellIndex[idx2d] = idx3d;
            }
        }
        return cellIndex;
    }

    private ProfileGrid discretizProfileGrid(List<Cell2D> path, Field2D field2d, Grid grid) {
        int width = field2d.width();
        int heigth = field2d.height();

        double[] wMeters = new double[width];
        double[] hMeters = new double[heigth];
        for (int w = 0; w < width; w++) {
            wMeters[w] = path.get(w).wMeters();
        }
        for (int h = 0; h < heigth; h++) {
            hMeters[h] = grid.centerZMeters(h);
        }

        return new ProfileGrid(wMeters, hMeters);
    }

    private Point3D toPoint3d(PointDto dto) {
        return new Point3D(metersToScaled(dto.x()), metersToScaled(dto.y()), metersToScaled(dto.z()));
    }

    private record Cell2D(int ix, int iy,
            double wMeters) {
    }
}
