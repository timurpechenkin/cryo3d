package io.github.timurpechenkin.domain.measurement;

public record ProfileGrid(int wCellsCount, int hCellsCount, int[] cellIndex,
        double[] wCentersMeters, double[] hCentersMeters) {

}
