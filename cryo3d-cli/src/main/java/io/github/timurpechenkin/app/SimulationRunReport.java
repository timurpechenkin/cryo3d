package io.github.timurpechenkin.app;

import java.nio.file.Path;
import java.util.List;

import io.github.timurpechenkin.casefile.validation.ValidationError;

public record SimulationRunReport(
        Path casePath,
        RunStatus status,
        String caseName,
        String outputCaseName,
        List<ValidationError> validationErrors,
        String errorMessage) {

    public static SimulationRunReport success(Path casePath, String caseName, String outputCaseName) {
        return new SimulationRunReport(
                casePath,
                RunStatus.SUCCESS,
                caseName,
                outputCaseName,
                List.of(),
                null);
    }

    public static SimulationRunReport validationFailed(Path casePath, List<ValidationError> errors) {
        return new SimulationRunReport(
                casePath,
                RunStatus.VALIDATION_FAILED,
                null,
                null,
                List.copyOf(errors),
                null);
    }

    public static SimulationRunReport failed(Path casePath, Exception ex) {
        return new SimulationRunReport(
                casePath,
                RunStatus.FAILED,
                null,
                null,
                List.of(),
                ex.getMessage());
    }
}