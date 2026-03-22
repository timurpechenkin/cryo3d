package io.github.timurpechenkin.solver;

import java.time.Duration;
import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.time.TimeSettings;
import io.github.timurpechenkin.solver.calculator.StepCalculator;
import io.github.timurpechenkin.solver.context.CaseContext;
import io.github.timurpechenkin.solver.context.CaseContextFactory;
import io.github.timurpechenkin.solver.result.CaseResult;
import io.github.timurpechenkin.solver.result.CaseResultAccumulator;

/**
 * Базовая реализация {@link CaseSolver}.
 *
 * <p>
 * Класс управляет полным жизненным циклом расчёта:
 * <ol>
 * <li>создаёт начальное runtime-состояние расчёта через
 * {@link CaseContextFactory};</li>
 * <li>проверяет и интерпретирует временные параметры задачи;</li>
 * <li>выполняет временной цикл с постоянным шагом времени;</li>
 * <li>на каждом шаге вызывает {@link StepCalculator};</li>
 * <li>сохраняет выбранные состояния системы через
 * {@link CaseResultAccumulator};</li>
 * <li>формирует итоговый {@link CaseResult}.</li>
 * </ol>
 *
 * <p>
 * Ответственность класса ограничена организацией расчёта.
 * Сам {@code DefaultCaseSolver} не содержит численной схемы шага
 * и не определяет, как зависят свойства материала от температуры:
 * <ul>
 * <li>{@link StepCalculator} отвечает за вычисление одного временного
 * шага;</li>
 * <li>{@link CaseContext} предоставляет текущее состояние среды
 * и эффективные теплофизические свойства при текущей температуре.</li>
 * </ul>
 *
 * <p>
 * В данной реализации:
 * <ul>
 * <li>используется постоянный шаг времени {@code dtSeconds};</li>
 * <li>сохраняется начальное состояние на времени {@code t = 0};</li>
 * <li>далее сохраняются только шаги, кратные {@code saveEverySeconds}.</li>
 * </ul>
 *
 * <p>
 * Если последнее время расчёта не кратно интервалу сохранения,
 * финальное состояние в результат автоматически не добавляется.
 */
public final class DefaultCaseSolver implements CaseSolver {
    private final StepCalculator calculator;
    private final CaseContextFactory contextFactory;

    public DefaultCaseSolver(StepCalculator calculator, CaseContextFactory contextFactory) {
        this.calculator = Objects.requireNonNull(calculator, "calculator");
        this.contextFactory = Objects.requireNonNull(contextFactory, "contextFactory");
    }

    @Override
    public CaseResult solve(SimulationCase simulationCase) {
        Objects.requireNonNull(simulationCase, "simulationCase");

        CaseContext context = contextFactory.create(simulationCase);
        TimeSettings time = simulationCase.time();

        long cellCountLong = context.grid().cellCount();
        if (cellCountLong > Integer.MAX_VALUE) {
            throw new IllegalStateException("Grid is too large for int[]/double[] solver arrays: " + cellCountLong);
        }

        if (time.dtSeconds() <= 0) {
            throw new IllegalArgumentException("TimeSettings.dtSeconds must be > 0");
        }
        if (time.saveEverySeconds() % time.dtSeconds() != 0) {
            throw new IllegalArgumentException("saveEverySeconds must be a multiple of dtSeconds");
        }

        long totalSeconds = Duration.between(time.startDate(), time.endDate()).getSeconds();
        long stepsLong = totalSeconds / time.dtSeconds();
        if (stepsLong <= 0 || stepsLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("steps must be > 0 and < Integer.MAX_VALUE");
        }
        int steps = (int) stepsLong;

        long saveStepLong = time.saveEverySeconds() / time.dtSeconds();
        if (saveStepLong <= 0 || saveStepLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("save steps must be > 0 and < Integer.MAX_VALUE");
        }
        int saveStep = (int) saveStepLong;
        int savedSteps = steps / saveStep + 1;

        CaseResultAccumulator accumulator = new CaseResultAccumulator(simulationCase, savedSteps);
        accumulator.recordStep(0, 0, context.currentTemperatureByCell());

        long dtSeconds = time.dtSeconds();
        int saveIndex = 1;

        for (int step = 1; step <= steps; step++) {
            long currentTimeSeconds = step * dtSeconds;

            calculator.calculateStep(context, dtSeconds, currentTimeSeconds);

            if (isSaveStep(step, saveStep)) {
                accumulator.recordStep(saveIndex, currentTimeSeconds, context.currentTemperatureByCell());
                saveIndex++;
            }
        }

        return accumulator.build();
    }

    private boolean isSaveStep(int step, int saveStep) {
        return step % saveStep == 0;
    }
}