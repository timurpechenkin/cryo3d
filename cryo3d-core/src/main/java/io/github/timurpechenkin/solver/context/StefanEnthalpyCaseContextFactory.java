package io.github.timurpechenkin.solver.context;

import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationCase;

/**
 * Фабрика энтальпийного runtime-состояния для решения задачи Стефана.
 */
public final class StefanEnthalpyCaseContextFactory implements CaseContextFactory {

    @Override
    public CaseContext create(SimulationCase simulationCase) {
        Objects.requireNonNull(simulationCase, "simulationCase");
        return new StefanEnthalpyCaseContext(simulationCase);
    }
}