package io.github.timurpechenkin.solver;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.temperature.TemperatureField;

public interface HeatSolver {
    /**
     * Advances temperature by one dt step.
     * 
     * @param c       runtime case (grid, fields, libraries, BCs)
     * @param t       current time in seconds (can be used later; for now optional)
     * @param current current temperature field
     * @return next temperature field (new instance)
     */
    TemperatureField advanceOneStep(SimulationCase c, long t, TemperatureField current);
}
