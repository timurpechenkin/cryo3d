package io.github.timurpechenkin.output.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class Csv {
    private Csv() {
    }

    static BufferedWriter writer(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        return Files.newBufferedWriter(file);
    }

    static String esc(String s) {
        if (s == null)
            return "";
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!needsQuotes)
            return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
