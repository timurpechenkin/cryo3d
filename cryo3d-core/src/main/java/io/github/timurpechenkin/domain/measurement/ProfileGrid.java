package io.github.timurpechenkin.domain.measurement;

public record ProfileGrid(String name, int wCount, int hCount, double wStepMeters, double hStepMeters, int[] cellIndex,
                double[] wMeters, double[] hMeters) {

}
