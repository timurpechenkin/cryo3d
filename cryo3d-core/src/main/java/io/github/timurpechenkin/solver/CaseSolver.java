package io.github.timurpechenkin.solver;

import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.time.TimeSettings;
import io.github.timurpechenkin.solver.calculator.StepCalculator;
import io.github.timurpechenkin.solver.context.CaseContext;
import io.github.timurpechenkin.solver.result.CaseResult;
import io.github.timurpechenkin.solver.result.CaseResultAccumulator;

/**
 * Выполняет полный расчёт задачи теплопереноса.
 *
 * <p>
 * Класс управляет временным циклом:
 * <ol>
 * <li>подготавливает solver-ориентированный контекст задачи;</li>
 * <li>инициализирует температурное поле;</li>
 * <li>на каждом шаге вызывает {@link StepCalculator};</li>
 * <li>сохраняет историю результатов через {@link CaseResultAccumulator};</li>
 * <li>возвращает итоговый {@link CaseResult}.</li>
 * </ol>
 *
 * <p>
 * Численная схема одного шага не зашита в этот класс и передаётся
 * через интерфейс {@link StepCalculator}.
 */
public final class CaseSolver {

    private final StepCalculator calculator;
    private final CaseContext context;

    public CaseSolver(StepCalculator calculator, CaseContext context) {
        this.calculator = Objects.requireNonNull(calculator, "calculator");
        this.context = Objects.requireNonNull(context, "context");
    }

    public CaseResult calculate(SimulationCase simulationCase) {
        Objects.requireNonNull(simulationCase, "simulationCase");

        context.createFrom(simulationCase);
        TimeSettings time = simulationCase.time();

        long cellCountLong = context.grid().cellCount();
        if (cellCountLong > Integer.MAX_VALUE) {
            throw new IllegalStateException("Grid is too large for int[]/double[] solver arrays: " + cellCountLong);
        }
        int cellCount = (int) cellCountLong;

        if (time.dtSeconds() <= 0) {
            throw new IllegalArgumentException("TimeSettings.dtSeconds must be > 0");
        }

        if (time.totalSeconds() % time.dtSeconds() != 0) {
            throw new IllegalArgumentException("totalSeconds must be a multiple of dtSeconds");
        }
        if (time.saveEverySeconds() % time.dtSeconds() != 0) {
            throw new IllegalArgumentException("saveEverySeconds must be a multiple of dtSeconds");
        }

        long stepsLong = time.totalSeconds() / time.dtSeconds();
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

        double[] currentTemperatureCByCell = new double[cellCount];
        double[] nextTemperatureCByCell = new double[cellCount];

        for (int i = 0; i < cellCount; i++) {
            currentTemperatureCByCell[i] = context.temperatureC(i);
        }

        CaseResultAccumulator accumulator = new CaseResultAccumulator(simulationCase, savedSteps);

        accumulator.recordStep(0, 0, currentTemperatureCByCell);

        long dtSeconds = time.dtSeconds();
        int saveIndex = 1;

        for (int step = 1; step <= steps; step++) {
            double currentTimeSeconds = (double) step * dtSeconds;

            calculator.calculateStep(
                    context,
                    currentTemperatureCByCell,
                    nextTemperatureCByCell,
                    dtSeconds);

            if (isSaveStep(step, saveStep)) {
                accumulator.recordStep(saveIndex, currentTimeSeconds, nextTemperatureCByCell);
                saveIndex++;
            }

            double[] tmp = currentTemperatureCByCell;
            currentTemperatureCByCell = nextTemperatureCByCell;
            nextTemperatureCByCell = tmp;
        }

        return accumulator.build();
    }

    private boolean isSaveStep(int step, int saveStep) {
        return step % saveStep == 0;
    }
}