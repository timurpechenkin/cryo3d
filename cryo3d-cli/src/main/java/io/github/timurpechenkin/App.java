package io.github.timurpechenkin;

import io.github.timurpechenkin.commands.RunCommand;
import io.github.timurpechenkin.commands.ValidateCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "cryo3d", mixinStandardHelpOptions = true, description = "Cryo3D CLI", subcommands = {
        ValidateCommand.class, RunCommand.class })
public class App implements Runnable {
    public static void main(String[] args) {
        int code = new CommandLine(new App()).execute(args);
        System.exit(code);
    }

    @Override
    public void run() {
        System.out.println("Cryo3D CLI. Use --help.");
    }
}
