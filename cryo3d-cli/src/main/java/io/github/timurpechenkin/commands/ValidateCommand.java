package io.github.timurpechenkin.commands;

import java.nio.file.Path;

import io.github.timurpechenkin.casefile.CaseLoader;
import io.github.timurpechenkin.casefile.SimulationCase;
import io.github.timurpechenkin.casefile.validation.CaseValidator;
import io.github.timurpechenkin.casefile.validation.ValidationError;
import io.github.timurpechenkin.casefile.validation.ValidationResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "validate", description = "Validate case.yaml and print errors")
public class ValidateCommand implements Runnable {

    @Parameters(index = "0", description = "Path to case.yaml")
    private Path path;

    @Override
    public void run() {
        try {
            CaseLoader caseLoader = new CaseLoader();
            SimulationCase simulationCase = caseLoader.load(path);

            CaseValidator caseValidator = new CaseValidator();
            ValidationResult result = caseValidator.validate(simulationCase);

            if (result.isOk()) {
                System.out.println("OK");
            } else {
                System.out.println("ERROR: case is invalid");
                for (ValidationError error : result.errors()) {
                    System.out.println(" - " + error.path() + ": " + error.message());
                }
                System.exit(2);
            }
        } catch (Exception ex) {
            System.out.print("ERROR: " + ex.getMessage());
            System.exit(2);
        }
    }

}
