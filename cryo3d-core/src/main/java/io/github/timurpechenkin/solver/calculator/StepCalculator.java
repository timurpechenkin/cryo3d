package io.github.timurpechenkin.solver.calculator;

import io.github.timurpechenkin.solver.context.CaseContext;

/**
 * Численная схема одного временного шага расчёта.
 *
 * <p>
 * {@code StepCalculator} определяет, как изменяется температурное поле
 * за один шаг по времени при заданных теплофизических свойствах среды.
 *
 * <p>
 * В архитектуре расчёта роли разделены следующим образом:
 * <ul>
 * <li>{@code CaseContext} предоставляет текущее состояние системы
 * (температуру) и эффективные теплофизические параметры
 * при этом состоянии;</li>
 * <li>{@code StepCalculator} использует эти данные и вычисляет
 * новое температурное поле на следующем временном шаге.</li>
 * </ul>
 *
 * <p>
 * Реализация обычно:
 * <ul>
 * <li>считывает текущее температурное поле через
 * {@link CaseContext#currentTemperatureByCell()};</li>
 * <li>вычисляет новое состояние системы;</li>
 * <li>записывает результат обратно в контекст через
 * {@link CaseContext#setNewTemperature(double[])}.</li>
 * </ul>
 *
 * <p>
 * Конкретная реализация может использовать любую численную схему:
 * явную, неявную, итерационную и т.д.
 */
public interface StepCalculator {

    /**
     * Выполняет один шаг расчёта.
     *
     * <p>
     * Метод должен:
     * <ul>
     * <li>использовать текущее состояние из {@link CaseContext};</li>
     * <li>вычислить новое состояние за время {@code dtSeconds};</li>
     * <li>записать результат обратно в контекст.</li>
     * </ul>
     *
     * @param context   текущее runtime-состояние расчёта
     * @param dtSeconds шаг по времени, сек
     */
    void calculateStep(
            CaseContext context,
            long dtSeconds);
}