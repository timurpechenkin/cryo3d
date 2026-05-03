package io.github.timurpechenkin.app;

import java.nio.file.Path;

public record SimulationRunReport(
        Path casePath,
        RunStatus status,
        String caseName,
        String outputCaseName,
        String errorMessage) {

    public static SimulationRunReport success(Path casePath, String caseName, String outputCaseName) {
        return new SimulationRunReport(
                casePath,
                RunStatus.SUCCESS,
                caseName,
                outputCaseName,
                null);
    }

    public static SimulationRunReport failed(Path casePath, Exception ex) {
        return new SimulationRunReport(
                casePath,
                RunStatus.FAILED,
                null,
                null,
                ex.getMessage());
    }
}