package io.github.timurpechenkin.solver.progress;

public interface SimulationProgressListener {
    void onStart(int totalSteps);

    void onProgress(SimulationProgress progress);

    void onFinish();

    void onError(Exception ex);

    SimulationProgressListener NO_OP = new SimulationProgressListener() {
        @Override
        public void onStart(int totalSteps) {
        }

        @Override
        public void onProgress(SimulationProgress progress) {
        }

        @Override
        public void onFinish() {
        }

        @Override
        public void onError(Exception ex) {
        }
    };
}