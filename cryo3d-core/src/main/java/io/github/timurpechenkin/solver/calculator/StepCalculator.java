package io.github.timurpechenkin.solver.calculator;

import io.github.timurpechenkin.solver.context.CaseContext;

/**
 * Выполняет один шаг расчёта температурного поля.
 *
 * <p>
 * Реализация отвечает только за численную схему перехода
 * от состояния {@code currentTemperatureCByCell} к состоянию
 * {@code nextTemperatureCByCell} на шаге времени {@code dtSeconds}.
 *
 * <p>
 * Массивы температур должны иметь длину, равную числу ячеек сетки.
 * Реализация не должна изменять массив {@code currentTemperatureCByCell};
 * результат записывается в {@code nextTemperatureCByCell}.
 */
public interface StepCalculator {

    /**
     * Выполняет один шаг расчёта.
     *
     * @param context                   solver-ориентированный доступ к данным
     *                                  задачи
     * @param currentTemperatureCByCell температура на текущем шаге, °C
     * @param nextTemperatureCByCell    сюда записывается температура следующего
     *                                  шага, °C
     * @param dtSeconds                 шаг по времени, сек
     */
    void calculateStep(
            CaseContext context,
            double[] currentTemperatureCByCell,
            double[] nextTemperatureCByCell,
            long dtSeconds);
}