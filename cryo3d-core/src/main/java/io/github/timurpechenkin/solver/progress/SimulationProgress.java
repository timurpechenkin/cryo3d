package io.github.timurpechenkin.solver.progress;

public record SimulationProgress(
        int currentStep,
        int totalSteps,
        long currentTimeSeconds,
        double percent) {
}