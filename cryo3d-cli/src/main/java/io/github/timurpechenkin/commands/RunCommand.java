package io.github.timurpechenkin.commands;

import java.nio.file.Path;

import io.github.timurpechenkin.casefile.CaseLoader;
import io.github.timurpechenkin.casefile.dto.SimulationCase;
import io.github.timurpechenkin.casefile.validation.CaseValidator;
import io.github.timurpechenkin.casefile.validation.ValidationError;
import io.github.timurpechenkin.casefile.validation.ValidationResult;
import io.github.timurpechenkin.output.OutputWriter;
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
        try {
            CaseLoader loader = new CaseLoader();
            SimulationCase simulationCase = loader.load(casePath);

            CaseValidator validator = new CaseValidator();
            ValidationResult result = validator.validate(simulationCase);
            if (!result.isOk()) {
                System.out.println("ERROR: case is invalid:");
                for (ValidationError error : result.errors()) {
                    System.out.println("- " + error.path() + ": " + error.message());
                }
                System.exit(2);
                return;
            }

            OutputWriter writer = new OutputWriter();
            writer.writeSummary(outDir, simulationCase, "NOT_IMPLEMENTED_YET");

            System.out.println("OK: wrote " + outDir.resolve("summary.json"));

        } catch (Exception ex) {
            System.out.println("ERROR: " + ex.getMessage());
            System.exit(2);
        }
    }

}
