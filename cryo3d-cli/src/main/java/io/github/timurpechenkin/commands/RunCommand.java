package io.github.timurpechenkin.commands;

import java.nio.file.Path;

import io.github.timurpechenkin.casefile.CaseLoader;
import io.github.timurpechenkin.casefile.CaseResolver;
import io.github.timurpechenkin.casefile.CaseValidator;
import io.github.timurpechenkin.casefile.dto.SimulationCaseDto;
import io.github.timurpechenkin.casefile.validation.ValidationError;
import io.github.timurpechenkin.casefile.validation.ValidationResult;
import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.output.OutputWriter;
import io.github.timurpechenkin.solver.CaseSolver;
import io.github.timurpechenkin.solver.calculator.IdentityStepCalculator;
import io.github.timurpechenkin.solver.context.DirectCaseContextFactory;
import io.github.timurpechenkin.solver.result.CaseResult;
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
            SimulationCaseDto caseDto = loader.load(casePath);

            CaseValidator validator = new CaseValidator();
            ValidationResult validation = validator.validate(caseDto);
            if (!validation.isOk()) {
                System.out.println("ERROR: case is invalid:");
                for (ValidationError error : validation.errors()) {
                    System.out.println("- " + error.path() + ": " + error.message());
                }
                System.exit(2);
                return;
            }

            CaseResolver resolver = new CaseResolver();
            SimulationCase simulationCase = resolver.resolve(caseDto);

            OutputWriter writer = new OutputWriter(outDir);
            writer.writeSummary(simulationCase, "NOT_IMPLEMENTED_YET");
            System.out.println("OK: wrote " + outDir.resolve("summary.json"));

            CaseSolver solver = new CaseSolver(new IdentityStepCalculator(), new DirectCaseContextFactory());
            CaseResult result = solver.calculate(simulationCase);

            writer.writeResult(result);
            System.out.println("OK: wrote result for " + simulationCase.caseName());

        } catch (Exception ex) {
            System.out.println("ERROR: " + ex.getMessage());
            System.exit(2);
        }
    }

}
