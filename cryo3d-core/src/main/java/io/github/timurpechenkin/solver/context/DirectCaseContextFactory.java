package io.github.timurpechenkin.solver.context;

import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationCase;

/**
 * Фабрика базового {@link DirectCaseContext}.
 */
public final class DirectCaseContextFactory implements CaseContextFactory {

    @Override
    public CaseContext create(SimulationCase simulationCase) {
        Objects.requireNonNull(simulationCase, "simulationCase");

        DirectCaseContext context = new DirectCaseContext(simulationCase);
        return context;
    }
}