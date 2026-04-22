package io.github.timurpechenkin.output.image;

import io.github.timurpechenkin.domain.presentation.NumberFormat;
import io.github.timurpechenkin.domain.recording.Profile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import javax.imageio.ImageIO;

import io.github.timurpechenkin.solver.recording.TemperatureFrame2D;

/**
 * Записывает один PNG-кадр профиля на диск.
 */
public final class ProfilePngWriter {

    private final ProfileSnapshotRenderer renderer;

    public ProfilePngWriter() {
        this(new ProfileSnapshotRenderer());
    }

    public ProfilePngWriter(ProfileSnapshotRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer must not be null");
    }

    public void writeTemperature(
            Path outDir,
            Profile profile,
            TemperatureFrame2D frame,
            String fileName,
            NumberFormat format,
            ProfileRenderSettings settings) throws IOException {

        Objects.requireNonNull(outDir, "file must not be null");
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(frame, "frame must not be null");
        Objects.requireNonNull(settings, "settings must not be null");

        Files.createDirectories(outDir);
        Path file = outDir.resolve(fileName + ".png");

        BufferedImage image = renderer.render(profile, frame, format, settings);

        boolean written = ImageIO.write(image, "png", file.toFile());
        if (!written) {
            throw new IOException("No ImageIO writer found for PNG format");
        }
    }
}