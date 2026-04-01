package io.github.timurpechenkin.solver.info;

import java.time.LocalDateTime;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.time.TimeSettings;
import io.github.timurpechenkin.geometry.Axis3D;

public class SimulationDefinitionCollector {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private long dtSeconds;
    private long cellCount;
    private double sizeXMeters;
    private double sizeYMeters;
    private double sizeZMeters;

    public SimulationDefinitionCollector(SimulationCase simulationCase) {
        TimeSettings time = simulationCase.time();
        this.dtSeconds = time.dtSeconds();
        this.startDate = time.startDate();
        this.endDate = time.endDate();

        Grid3D grid = simulationCase.grid();
        this.cellCount = grid.cellCount();
        this.sizeXMeters = grid.axis(Axis3D.X).sizeMeters();
        this.sizeYMeters = grid.axis(Axis3D.Y).sizeMeters();
        this.sizeZMeters = grid.axis(Axis3D.Z).sizeMeters();
    }

    public SimulationDefinition definition() {
        return new SimulationDefinition(startDate, endDate, dtSeconds, cellCount, sizeXMeters, sizeYMeters,
                sizeZMeters);
    }
}
