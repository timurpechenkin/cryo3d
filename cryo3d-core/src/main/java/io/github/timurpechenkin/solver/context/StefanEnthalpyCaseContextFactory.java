package io.github.timurpechenkin.solver.context;

import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationModel;

/**
 * Фабрика энтальпийного runtime-состояния для решения задачи Стефана.
 */
public final class StefanEnthalpyCaseContextFactory implements CaseContextFactory {

    @Override
    public CaseContext create(SimulationModel model) {
        Objects.requireNonNull(model, "model");
        return new StefanEnthalpyCaseContext(model);
    }
}