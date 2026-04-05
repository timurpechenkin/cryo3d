package io.github.timurpechenkin.progress;

import io.github.timurpechenkin.solver.progress.SimulationProgress;
import io.github.timurpechenkin.solver.progress.SimulationProgressListener;

public class ConsoleProgressListener implements SimulationProgressListener {

    @Override
    public void onStart(int totalSteps) {
        System.out.println("Simulation started. Total steps: " + totalSteps);
    }

    @Override
    public void onProgress(SimulationProgress progress) {
        int percent = (int) progress.percent();
        System.out.print("\rProgress: " + percent + "% (" +
                progress.currentStep() + "/" + progress.totalSteps() + ")");
    }

    @Override
    public void onFinish() {
        System.out.println("\rProgress: 100% - completed");
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("\nSimulation failed: " + ex.getMessage());
    }
}