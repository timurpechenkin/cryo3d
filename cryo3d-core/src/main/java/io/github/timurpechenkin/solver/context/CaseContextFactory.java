package io.github.timurpechenkin.solver.context;

import io.github.timurpechenkin.domain.SimulationCase;

/**
 * Фабрика начального runtime-состояния расчёта.
 *
 * <p>
 * Фабрика создаёт и инициализирует конкретную реализацию
 * {@link CaseContext} на основе {@link SimulationCase}.
 *
 * <p>
 * Эта абстракция позволяет:
 * <ul>
 * <li>скрыть конкретный тип контекста от солвера;</li>
 * <li>выбирать разные реализации контекста
 * для разных моделей грунта или фазового перехода;</li>
 * <li>централизовать создание начального состояния расчёта.</li>
 * </ul>
 */
public interface CaseContextFactory {

    /**
     * Создаёт начальное runtime-состояние расчёта.
     *
     * @param simulationCase расчётный случай
     * @return готовый к использованию контекст расчёта
     */
    CaseContext create(SimulationCase simulationCase);
}