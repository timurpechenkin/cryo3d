package io.github.timurpechenkin.solver.context;

import io.github.timurpechenkin.domain.SimulationCase;

/**
 * Фабрика solver-ориентированных контекстов расчёта.
 *
 * <p>
 * Позволяет централизованно создавать и инициализировать
 * конкретную реализацию {@link CaseContext}.
 *
 * <p>
 * Это удобно, если в проекте существует несколько реализаций контекста:
 * например, прямой доступ к данным задачи, контекст с продвинутой моделью
 * фазового перехода или контекст со специальной логикой вычисления
 * эффективных свойств грунта.
 */
public interface CaseContextFactory {

    /**
     * Создаёт и инициализирует контекст расчёта.
     *
     * @param simulationCase расчётный случай
     * @return готовый к использованию контекст
     */
    CaseContext create(SimulationCase simulationCase);
}