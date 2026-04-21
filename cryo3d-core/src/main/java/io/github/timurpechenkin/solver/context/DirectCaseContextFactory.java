package io.github.timurpechenkin.solver.context;

import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationModel;

/**
 * Фабрика базовой реализации {@link DirectCaseContext}.
 *
 * <p>
 * Создаёт начальное runtime-состояние расчёта
 * с прямым доступом к данным {@link SimulationModel}
 * и бинарной схемой выбора талых/мёрзлых свойств материала.
 */
public final class DirectCaseContextFactory implements CaseContextFactory {

    /**
     * Создаёт начальное runtime-состояние расчёта
     * в виде {@link DirectCaseContext}.
     *
     * @param model расчётный случай
     * @return готовый к использованию контекст
     * @throws NullPointerException  если {@code simulationCase == null}
     * @throws IllegalStateException если данные расчётного случая неконсистентны
     */
    @Override
    public CaseContext create(SimulationModel model) {
        Objects.requireNonNull(model, "model");
        return new DirectCaseContext(model);
    }
}