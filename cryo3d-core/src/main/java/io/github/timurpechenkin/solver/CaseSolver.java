package io.github.timurpechenkin.solver;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.solver.result.CaseResult;

/**
 * Выполняет полный расчёт задачи теплопереноса.
 *
 */
public interface CaseSolver {

    CaseResult solve(SimulationCase simulationCase);
}