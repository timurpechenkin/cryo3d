package io.github.timurpechenkin.solver.context;

import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationCase;

/**
 * Фабрика базовой реализации {@link DirectCaseContext}.
 *
 * <p>
 * Создаёт начальное runtime-состояние расчёта
 * с прямым доступом к данным {@link SimulationCase}
 * и бинарной схемой выбора талых/мёрзлых свойств материала.
 */
public final class DirectCaseContextFactory implements CaseContextFactory {

    /**
     * Создаёт начальное runtime-состояние расчёта
     * в виде {@link DirectCaseContext}.
     *
     * @param simulationCase расчётный случай
     * @return готовый к использованию контекст
     * @throws NullPointerException  если {@code simulationCase == null}
     * @throws IllegalStateException если данные расчётного случая неконсистентны
     */
    @Override
    public CaseContext create(SimulationCase simulationCase) {
        Objects.requireNonNull(simulationCase, "simulationCase");

        DirectCaseContext context = new DirectCaseContext(simulationCase);
        return context;
    }
}