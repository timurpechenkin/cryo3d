package io.github.timurpechenkin.app;

import java.nio.file.Path;
import java.util.List;

import io.github.timurpechenkin.casefile.validation.ValidationError;
import io.github.timurpechenkin.domain.SimulationCase;

public record SimulationPreparationReport(
        PreparedSimulationCase preparedCase,
        PreparationStatus status,
        List<ValidationError> validationErrors,
        String errorMessage) {

    public static SimulationPreparationReport validationFailed(Path casePath, List<ValidationError> errors) {
        return new SimulationPreparationReport(
                new PreparedSimulationCase(casePath, null),
                PreparationStatus.VALIDATION_FAILED,
                List.copyOf(errors),
                null);
    }

    public static SimulationPreparationReport success(Path casePath, SimulationCase simulationCase) {
        return new SimulationPreparationReport(
                new PreparedSimulationCase(casePath, simulationCase),
                PreparationStatus.SUCCESS,
                List.of(),
                null);
    }

    public static SimulationPreparationReport failed(Path casePath, Exception ex) {
        return new SimulationPreparationReport(
                new PreparedSimulationCase(casePath, null),
                PreparationStatus.FAILED,
                List.of(),
                ex.getMessage());
    }

}
