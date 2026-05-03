package io.github.timurpechenkin.app;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import io.github.timurpechenkin.casefile.CaseLoader;
import io.github.timurpechenkin.casefile.CaseResolver;
import io.github.timurpechenkin.casefile.CaseValidator;
import io.github.timurpechenkin.casefile.dto.SimulationCaseDto;
import io.github.timurpechenkin.casefile.validation.ValidationResult;
import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.metadata.CaseMetadata;
import io.github.timurpechenkin.domain.presentation.NumberFormat;
import io.github.timurpechenkin.output.OutputWriter;
import io.github.timurpechenkin.progress.ConsoleProgressListener;
import io.github.timurpechenkin.solver.CaseSolverFactory;
import io.github.timurpechenkin.solver.SimulationResult;
import io.github.timurpechenkin.solver.recording.SimulationRecording;
import io.github.timurpechenkin.time.TimeFormat;

public final class DefaultSimulationRunService implements SimulationRunService {

    private final CaseLoader loader = new CaseLoader();
    private final CaseValidator validator = new CaseValidator();
    private final CaseResolver resolver = new CaseResolver();
    private final CaseSolverFactory solverFactory = new CaseSolverFactory();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final ConsoleProgressListener consoleProgressListener = new ConsoleProgressListener();
    private final int targetProgressUpdates = 10000;

    @Override
    public SimulationRunReport run(PreparedSimulationCase preparedCase, Path outDir) {
        Objects.requireNonNull(preparedCase, "preparedCase");
        Objects.requireNonNull(outDir, "outDir");

        SimulationCase simulationCase = preparedCase.simulationCase();
        Path casePath = preparedCase.path();

        try {
            CaseMetadata metadata = simulationCase.metadata();

            OutputWriter writer = new OutputWriter(outDir);
            String caseName = metadata.caseName() + "_" + formatter.format(LocalDateTime.now());

            writer.writeSummary(simulationCase, caseName);

            SimulationResult result = solverFactory
                    .create(simulationCase, consoleProgressListener, targetProgressUpdates).solve();

            SimulationRecording recording = result.recording();
            TimeFormat timeFormat = result.metadata().timeFormat();
            NumberFormat numberFormat = result.metadata().numberFormat();

            writer.writeResult(recording, caseName, timeFormat, numberFormat);

            return SimulationRunReport.success(casePath, metadata.caseName(), caseName);

        } catch (Exception ex) {
            return SimulationRunReport.failed(casePath, ex);
        }
    }

    @Override
    public SimulationPreparationReport prepare(Path casePath) {
        try {
            SimulationCaseDto caseDto = loader.load(casePath);
            ValidationResult validation = validator.validate(caseDto);
            if (!validation.isOk()) {
                return SimulationPreparationReport.validationFailed(casePath, validation.errors());
            }
            SimulationCase simulationCase = resolver.resolve(caseDto);
            return SimulationPreparationReport.success(casePath, simulationCase);

        } catch (Exception ex) {
            return SimulationPreparationReport.failed(casePath, ex);
        }
    }
}