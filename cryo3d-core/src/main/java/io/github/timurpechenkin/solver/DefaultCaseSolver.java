package io.github.timurpechenkin.solver;

import java.time.Duration;
import java.util.Objects;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.time.TimeSettings;
import io.github.timurpechenkin.solver.calculator.StepCalculator;
import io.github.timurpechenkin.solver.context.CaseContext;
import io.github.timurpechenkin.solver.context.CaseContextFactory;
import io.github.timurpechenkin.solver.info.SimulationDefinition;
import io.github.timurpechenkin.solver.info.SimulationDefinitionCollector;
import io.github.timurpechenkin.solver.metadata.SimulationMetadataCollector;
import io.github.timurpechenkin.solver.metadata.SimulationMetadata;
import io.github.timurpechenkin.solver.recording.RecordingAccumulator;
import io.github.timurpechenkin.solver.recording.RecordingResult;

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
 * {@link RecordingAccumulator};</li>
 * <li>формирует итоговый {@link RecordingResult}.</li>
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
    public SimulationResult solve(SimulationCase simulationCase) {
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

        long totalSeconds = Duration.between(time.startDate(), time.endDate()).getSeconds();
        long stepsLong = totalSeconds / time.dtSeconds();
        if (stepsLong <= 0 || stepsLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("steps must be > 0 and < Integer.MAX_VALUE");
        }
        int steps = (int) stepsLong;
        long dtSeconds = time.dtSeconds();

        RecordingAccumulator accumulator = new RecordingAccumulator(simulationCase, steps);
        SimulationMetadataCollector metadataCollector = new SimulationMetadataCollector(simulationCase);

        accumulator.recordStep(0, 0, context.currentTemperatureByCell());

        for (int step = 1; step <= steps; step++) {
            long currentTimeSeconds = step * dtSeconds;
            calculator.calculateStep(context, dtSeconds, currentTimeSeconds);
            accumulator.recordStep(step, currentTimeSeconds, context.currentTemperatureByCell());
        }

        SimulationDefinitionCollector definitionCollector = new SimulationDefinitionCollector(simulationCase);
        SimulationDefinition definition = definitionCollector.definition();
        RecordingResult recording = accumulator.build();
        SimulationMetadata metadata = metadataCollector.metadata();
        return new SimulationResult(metadata, definition, recording);
    }
}