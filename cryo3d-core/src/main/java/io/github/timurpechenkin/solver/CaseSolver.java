package io.github.timurpechenkin.solver;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.solver.recording.RecordingResult;

/**
 * Выполняет полный расчёт задачи теплопереноса.
 *
 * <p>
 * {@code CaseSolver} управляет всем процессом моделирования:
 * от начального состояния до формирования итогового результата.
 *
 * <p>
 * В рамках расчёта:
 * <ul>
 * <li>на основе {@link SimulationCase} создаётся начальное
 * runtime-состояние системы (см. {@code CaseContext});</li>
 * <li>выполняется временной цикл с заданным шагом;</li>
 * <li>на каждом шаге применяется численная схема
 * (см. {@code StepCalculator});</li>
 * <li>состояние системы последовательно обновляется;</li>
 * <li>результаты сохраняются и агрегируются в {@link RecordingResult}.</li>
 * </ul>
 *
 * <p>
 * Архитектурно:
 * <ul>
 * <li>{@code CaseSolver} отвечает за организацию расчёта и управление
 * временем;</li>
 * <li>{@code CaseContext} представляет состояние среды и её свойства;</li>
 * <li>{@code StepCalculator} определяет численную схему одного шага.</li>
 * </ul>
 */
public interface CaseSolver {

    /**
     * Выполняет полный расчёт задачи.
     *
     * @param simulationCase расчётный случай
     * @return результат расчёта, содержащий историю состояний системы
     * @throws NullPointerException     если {@code simulationCase == null}
     * @throws IllegalArgumentException если параметры задачи некорректны
     * @throws IllegalStateException    если расчёт не может быть выполнен
     *                                  из-за несогласованности данных
     */
    SimulationResult solve(SimulationCase simulationCase);
}