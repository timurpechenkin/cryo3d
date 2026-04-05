package io.github.timurpechenkin.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import io.github.timurpechenkin.app.DefaultSimulationRunService;
import io.github.timurpechenkin.app.RunStatus;
import io.github.timurpechenkin.app.SimulationRunReport;
import io.github.timurpechenkin.app.SimulationRunService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "queue", description = "Run all YAML simulation cases from a directory sequentially")
public final class QueueCommand implements Runnable {

    @Option(names = { "-i", "--input-dir" }, required = true, description = "Directory with case YAML files")
    private Path inputDir;

    @Option(names = { "-o", "--out" }, required = true, description = "Output directory")
    private Path outDir;

    @Override
    public void run() {
        try {
            validateInputDirectory(inputDir);

            List<Path> caseFiles = findCaseFiles(inputDir);
            if (caseFiles.isEmpty()) {
                System.out.println("ERROR: no YAML files found in " + inputDir);
                System.exit(2);
                return;
            }

            SimulationRunService runService = new DefaultSimulationRunService();

            int total = caseFiles.size();
            int successCount = 0;
            int failedCount = 0;

            System.out.println("Found " + total + " case file(s).");

            for (int i = 0; i < caseFiles.size(); i++) {
                Path casePath = caseFiles.get(i);
                System.out.println();
                System.out.println("[" + (i + 1) + "/" + total + "] Processing " + casePath.getFileName());

                SimulationRunReport report = runService.run(casePath, outDir);

                if (report.status() == RunStatus.SUCCESS) {
                    successCount++;
                    System.out.println("OK: " + report.casePath());
                } else if (report.status() == RunStatus.VALIDATION_FAILED) {
                    failedCount++;
                    System.out.println("INVALID: " + report.casePath());
                    report.validationErrors()
                            .forEach(error -> System.out.println("- " + error.path() + ": " + error.message()));
                } else {
                    failedCount++;
                    System.out.println("FAILED: " + report.casePath());
                    System.out.println("- " + report.errorMessage());
                }
            }

            System.out.println();
            System.out.println("Queue finished.");
            System.out.println("Successful: " + successCount);
            System.out.println("Failed: " + failedCount);
            System.out.println("Total: " + total);

            if (failedCount > 0) {
                System.exit(2);
            }

        } catch (Exception ex) {
            System.out.println("ERROR: " + ex.getMessage());
            System.exit(2);
        }
    }

    private static void validateInputDirectory(Path inputDir) {
        if (!Files.exists(inputDir)) {
            throw new IllegalArgumentException("Input directory does not exist: " + inputDir);
        }
        if (!Files.isDirectory(inputDir)) {
            throw new IllegalArgumentException("Input path is not a directory: " + inputDir);
        }
    }

    private static List<Path> findCaseFiles(Path inputDir) throws IOException {
        try (Stream<Path> stream = Files.list(inputDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(QueueCommand::isYamlFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                    .toList();
        }
    }

    private static boolean isYamlFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".yaml") || name.endsWith(".yml");
    }
}