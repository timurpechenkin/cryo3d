package io.github.timurpechenkin.output.image;

import io.github.timurpechenkin.domain.grid.Grid2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import javax.imageio.ImageIO;

import io.github.timurpechenkin.solver.recording.ProfileSeries;
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

    public void write(
            Path file,
            ProfileSeries profileSeries,
            TemperatureFrame2D frame,
            ProfileRenderSettings settings, Grid2D grid2d) throws IOException {

        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(profileSeries, "profileSeries must not be null");
        Objects.requireNonNull(frame, "frame must not be null");
        Objects.requireNonNull(settings, "settings must not be null");

        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        BufferedImage image = renderer.render(grid2d, profileSeries, frame, settings);

        boolean written = ImageIO.write(image, "png", file.toFile());
        if (!written) {
            throw new IOException("No ImageIO writer found for PNG format");
        }
    }
}