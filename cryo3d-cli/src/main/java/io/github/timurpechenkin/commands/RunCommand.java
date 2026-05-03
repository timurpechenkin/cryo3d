package io.github.timurpechenkin.commands;

import java.nio.file.Path;

import io.github.timurpechenkin.app.DefaultSimulationRunService;
import io.github.timurpechenkin.app.PreparationStatus;
import io.github.timurpechenkin.app.RunStatus;
import io.github.timurpechenkin.app.SimulationPreparationReport;
import io.github.timurpechenkin.app.SimulationRunReport;
import io.github.timurpechenkin.app.SimulationRunService;
import io.github.timurpechenkin.app.PreparedSimulationCase;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "run", description = "Run simulation (alpha stub): validates case and writes results/summary.json")
public class RunCommand implements Runnable {

    @Option(names = { "-c", "--case" }, required = true, description = "Path to case.yaml")
    private Path casePath;

    @Option(names = { "-o", "--out" }, required = true, description = "Output directory")
    private Path outDir;

    @Override
    public void run() {
        SimulationRunService runService = new DefaultSimulationRunService();

        SimulationPreparationReport preparationReport = runService.prepare(casePath);
        if (preparationReport.status() == PreparationStatus.VALIDATION_FAILED) {
            System.out.println("ERROR: case validation is invalid:");
            preparationReport.validationErrors()
                    .forEach(error -> System.out.println("- " + error.path() + ": " + error.message()));
            System.exit(2);
            return;
        }
        if (preparationReport.status() == PreparationStatus.FAILED) {
            System.out.println("ERROR: case preparation is failed:");
            System.out.println("- " + preparationReport.errorMessage());
            System.exit(2);
            return;
        }

        PreparedSimulationCase preparedSimulationCase = preparationReport.preparedCase();
        SimulationRunReport report = runService.run(preparedSimulationCase, outDir);

        if (report.status() == RunStatus.SUCCESS) {
            System.out.println("OK: processed " + report.casePath());
            return;
        }

        System.out.println("ERROR: " + report.errorMessage());
        System.exit(2);
    }

}
