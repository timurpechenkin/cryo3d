package io.github.timurpechenkin.domain;

import io.github.timurpechenkin.domain.metadata.CaseMetadata;
import io.github.timurpechenkin.domain.presentation.PresentationSettings;
import io.github.timurpechenkin.domain.recording.RecordingSettings;
import io.github.timurpechenkin.domain.solver.SolverSettings;

public record SimulationCase(
        CaseMetadata metadata,
        SimulationModel model,
        SolverSettings solver,
        RecordingSettings recording,
        PresentationSettings presentation) {
}