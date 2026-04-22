package io.github.timurpechenkin.output.image;

import static io.github.timurpechenkin.geometry.GeometryScale.scaledToMeters;
import static io.github.timurpechenkin.number.NumberConverter.format;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Objects;

import io.github.timurpechenkin.domain.grid.Grid2D;
import io.github.timurpechenkin.domain.presentation.NumberFormat;
import io.github.timurpechenkin.domain.recording.Profile;
import io.github.timurpechenkin.geometry.Axis2D;
import io.github.timurpechenkin.solver.recording.TemperatureFrame2D;

/**
 * Рендерер одного температурного кадра профиля в PNG-изображение.
 *
 * <p>
 * Геометрия профиля рисуется строго по координатам ребер сетки
 * {@link Grid2D#edgesScaled(Axis2D)} без инверсии оси H.
 */
public final class ProfileSnapshotRenderer {

    static final int LEFT_MARGIN = 110;
    static final int RIGHT_MARGIN = 140;
    static final int TOP_MARGIN = 70;
    static final int BOTTOM_MARGIN = 110;

    static final int LEGEND_WIDTH = 24;
    static final int LEGEND_GAP = 48;

    private static final Color BACKGROUND = Color.WHITE;
    private static final Color GRID_COLOR = new Color(30, 30, 30, 180);
    private static final Color AXIS_COLOR = Color.BLACK;
    private static final Color BORDER_COLOR = Color.BLACK;

    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font SMALL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    private final TemperatureColorScale colorScale = new TemperatureColorScale();

    public BufferedImage render(
            Profile profile,
            TemperatureFrame2D frame,
            NumberFormat numberFormat,
            ProfileRenderSettings settings) {

        Objects.requireNonNull(frame, "frame must not be null");
        Objects.requireNonNull(settings, "settings must not be null");
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(numberFormat, "numberFormat must not be null");

        Grid2D grid = Objects.requireNonNull(profile.grid2d(), "profile.grid2d must not be null");

        validateFrameSize(grid, frame);

        BufferedImage image = new BufferedImage(
                settings.imageWidth(),
                settings.imageHeight(),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();
        try {
            configureGraphics(g);

            g.setColor(BACKGROUND);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());

            Rectangle plotArea = buildPlotArea(grid, settings);
            int legendX = plotArea.x + plotArea.width + LEGEND_GAP;
            int legendY = plotArea.y;
            int legendHeight = plotArea.height;

            drawTitle(g, profile, frame, settings);
            drawTemperatureField(g, grid, frame, settings, plotArea);
            if (settings.drawGrid()) {
                drawGrid(g, grid, plotArea);
            }
            drawPlotBorder(g, plotArea);
            drawAxisLabels(g, grid, settings, plotArea, numberFormat);
            drawLegend(g, settings, legendX, legendY, LEGEND_WIDTH, legendHeight, numberFormat);
        } finally {
            g.dispose();
        }

        return image;
    }

    private void validateFrameSize(Grid2D grid, TemperatureFrame2D frame) {
        long expectedCellCount = grid.cellCount();
        int actualCellCount = frame.temperatureCByCell().length;

        if (expectedCellCount != actualCellCount) {
            throw new IllegalArgumentException(
                    "Temperature frame cell count mismatch: expected " + expectedCellCount + ", actual "
                            + actualCellCount);
        }
    }

    private Rectangle buildPlotArea(Grid2D grid, ProfileRenderSettings settings) {
        int availableWidth = settings.imageWidth() - LEFT_MARGIN - RIGHT_MARGIN;
        int availableHeight = settings.imageHeight() - TOP_MARGIN - BOTTOM_MARGIN;

        if (availableWidth <= 0 || availableHeight <= 0) {
            throw new IllegalArgumentException("Image size is too small for plot margins");
        }

        int[] wEdges = grid.edgesScaled(Axis2D.W);
        int[] hEdges = grid.edgesScaled(Axis2D.H);

        int minW = wEdges[0];
        int maxW = wEdges[wEdges.length - 1];
        int minH = hEdges[0];
        int maxH = hEdges[hEdges.length - 1];

        int profileWidthScaled = maxW - minW;
        int profileHeightScaled = maxH - minH;

        if (profileWidthScaled <= 0 || profileHeightScaled <= 0) {
            throw new IllegalArgumentException("Profile geometry must have positive size");
        }

        double scaleX = (double) availableWidth / profileWidthScaled;
        double scaleY = (double) availableHeight / profileHeightScaled;

        double scale = Math.min(scaleX, scaleY);

        int plotWidth = Math.max(1, (int) Math.round(profileWidthScaled * scale));
        int plotHeight = Math.max(1, (int) Math.round(profileHeightScaled * scale));

        // центрируем внутри доступной области
        int plotX = LEFT_MARGIN + (availableWidth - plotWidth) / 2;
        int plotY = TOP_MARGIN + (availableHeight - plotHeight) / 2;

        return new Rectangle(plotX, plotY, plotWidth, plotHeight);
    }

    private void configureGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(1f));
    }

    private void drawTitle(
            Graphics2D g,
            Profile profile,
            TemperatureFrame2D frame,
            ProfileRenderSettings settings) {

        g.setFont(TITLE_FONT);
        g.setColor(AXIS_COLOR);

        String title = "Profile: " + profile.name() + ", t = " + frame.seconds() + " s";
        FontMetrics fm = g.getFontMetrics();
        int x = Math.max(16, (settings.imageWidth() - fm.stringWidth(title)) / 2);
        int y = 32;

        g.drawString(title, x, y);
    }

    private void drawTemperatureField(
            Graphics2D g,
            Grid2D grid,
            TemperatureFrame2D frame,
            ProfileRenderSettings settings,
            Rectangle plotArea) {

        int widthCells = grid.n(Axis2D.W);
        int heightCells = grid.n(Axis2D.H);

        int[] wEdges = grid.edgesScaled(Axis2D.W);
        int[] hEdges = grid.edgesScaled(Axis2D.H);

        int minW = wEdges[0];
        int maxW = wEdges[wEdges.length - 1];
        int minH = hEdges[0];
        int maxH = hEdges[hEdges.length - 1];

        for (int h = 0; h < heightCells; h++) {
            for (int w = 0; w < widthCells; w++) {
                int cellIndex = grid.index(w, h);
                double temperatureC = frame.temperatureCByCell()[cellIndex];

                int x0 = toPixel(wEdges[w], minW, maxW, plotArea.x, plotArea.width);
                int x1 = toPixel(wEdges[w + 1], minW, maxW, plotArea.x, plotArea.width);

                int y0 = toPixel(hEdges[h], minH, maxH, plotArea.y, plotArea.height);
                int y1 = toPixel(hEdges[h + 1], minH, maxH, plotArea.y, plotArea.height);

                int rectWidth = Math.max(1, x1 - x0);
                int rectHeight = Math.max(1, y1 - y0);

                g.setColor(colorScale.color(
                        temperatureC,
                        settings.minTemperatureC(),
                        settings.maxTemperatureC()));

                g.fillRect(x0, y0, rectWidth, rectHeight);
            }
        }
    }

    private void drawGrid(Graphics2D g, Grid2D grid, Rectangle plotArea) {
        int[] wEdges = grid.edgesScaled(Axis2D.W);
        int[] hEdges = grid.edgesScaled(Axis2D.H);

        int minW = wEdges[0];
        int maxW = wEdges[wEdges.length - 1];
        int minH = hEdges[0];
        int maxH = hEdges[hEdges.length - 1];

        g.setColor(GRID_COLOR);

        for (int edge : wEdges) {
            int x = toPixel(edge, minW, maxW, plotArea.x, plotArea.width);
            g.drawLine(x, plotArea.y, x, plotArea.y + plotArea.height);
        }

        for (int edge : hEdges) {
            int y = toPixel(edge, minH, maxH, plotArea.y, plotArea.height);
            g.drawLine(plotArea.x, y, plotArea.x + plotArea.width, y);
        }
    }

    private void drawPlotBorder(Graphics2D g, Rectangle plotArea) {
        g.setColor(BORDER_COLOR);
        g.drawRect(plotArea.x, plotArea.y, plotArea.width, plotArea.height);
    }

    private void drawAxisLabels(
            Graphics2D g,
            Grid2D grid,
            ProfileRenderSettings settings,
            Rectangle plotArea,
            NumberFormat numberFormat) {

        int[] wEdges = grid.edgesScaled(Axis2D.W);
        int[] hEdges = grid.edgesScaled(Axis2D.H);

        drawWAxis(g, wEdges, settings.axisWLabel(), plotArea, numberFormat);
        drawHAxis(g, hEdges, settings.axisHLabel(), plotArea, numberFormat);
    }

    private void drawWAxis(
            Graphics2D g,
            int[] wEdgesScaled,
            String label,
            Rectangle plotArea,
            NumberFormat numberFormat) {

        g.setColor(AXIS_COLOR);
        g.setFont(SMALL_FONT);

        int minW = wEdgesScaled[0];
        int maxW = wEdgesScaled[wEdgesScaled.length - 1];

        boolean drawAllTicks = wEdgesScaled.length <= 21;

        if (drawAllTicks) {
            for (int edge : wEdgesScaled) {
                int x = toPixel(edge, minW, maxW, plotArea.x, plotArea.width);
                String text = formatMeters(scaledToMeters(edge), numberFormat);
                drawCenteredString(g, text, x, plotArea.y + plotArea.height + 22);
            }
        } else {
            drawCenteredString(g, formatMeters(scaledToMeters(maxW), numberFormat), plotArea.x + plotArea.width,
                    plotArea.y - 10);
        }

        g.setFont(LABEL_FONT);
        drawCenteredString(g, label, plotArea.x + plotArea.width / 2, plotArea.y - 15);
    }

    private void drawHAxis(
            Graphics2D g,
            int[] hEdgesScaled,
            String label,
            Rectangle plotArea,
            NumberFormat numberFormat) {

        g.setColor(AXIS_COLOR);
        g.setFont(SMALL_FONT);

        int minH = hEdgesScaled[0];
        int maxH = hEdgesScaled[hEdgesScaled.length - 1];

        boolean drawAllTicks = hEdgesScaled.length <= 21;

        if (drawAllTicks) {
            for (int edge : hEdgesScaled) {
                int y = toPixel(edge, minH, maxH, plotArea.y, plotArea.height);
                String text = formatMeters(scaledToMeters(edge), numberFormat);
                drawRightAlignedString(g, text, plotArea.x - 10, y + 4);
            }
        } else {
            drawRightAlignedString(g, formatMeters(scaledToMeters(minH), numberFormat), plotArea.x - 10,
                    plotArea.y + 4);
            drawRightAlignedString(g, formatMeters(scaledToMeters(maxH), numberFormat), plotArea.x - 10,
                    plotArea.y + plotArea.height + 4);
        }

        g.setFont(LABEL_FONT);
        AffineTransform oldTransform = g.getTransform();
        try {
            g.rotate(-Math.PI / 2.0);
            int labelX = -(plotArea.y + plotArea.height / 2);
            int labelY = plotArea.x - 20;
            drawCenteredString(g, label, labelX, labelY);
        } finally {
            g.setTransform(oldTransform);
        }
    }

    private void drawLegend(
            Graphics2D g,
            ProfileRenderSettings settings,
            int x,
            int y,
            int width,
            int height,
            NumberFormat numberFormat) {

        for (int py = 0; py < height; py++) {
            double t = 1.0 - ((double) py / Math.max(1, height - 1));
            double temperature = settings.minTemperatureC()
                    + t * (settings.maxTemperatureC() - settings.minTemperatureC());

            g.setColor(colorScale.color(
                    temperature,
                    settings.minTemperatureC(),
                    settings.maxTemperatureC()));

            g.drawLine(x, y + py, x + width - 1, y + py);
        }

        g.setColor(BORDER_COLOR);
        g.drawRect(x, y, width, height);

        g.setFont(SMALL_FONT);
        g.setColor(AXIS_COLOR);

        g.drawString("T, °C", x - 2, y - 10);

        drawLegendTicks(g, settings, numberFormat, x, y, width, height);
    }

    private int toPixel(int value, int min, int max, int pixelStart, int pixelSpan) {
        if (max == min) {
            return pixelStart;
        }

        double t = (double) (value - min) / (double) (max - min);
        return pixelStart + (int) Math.round(t * pixelSpan);
    }

    private String formatMeters(double meters, NumberFormat numberFormat) {
        return format(meters, numberFormat);
    }

    private String formatTemperature(double valueC, NumberFormat numberFormat) {
        return format(valueC, numberFormat) + " °C";
    }

    private void drawCenteredString(Graphics2D g, String text, int centerX, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        int x = centerX - fm.stringWidth(text) / 2;
        g.drawString(text, x, baselineY);
    }

    private void drawRightAlignedString(Graphics2D g, String text, int rightX, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        int x = rightX - fm.stringWidth(text);
        g.drawString(text, x, baselineY);
    }

    private void drawLegendTicks(
            Graphics2D g,
            ProfileRenderSettings settings,
            NumberFormat numberFormat,
            int x,
            int y,
            int width,
            int height) {

        double min = settings.minTemperatureC();
        double max = settings.maxTemperatureC();
        double range = max - min;

        double[] values = new double[] {
                max,
                min + 0.75 * range,
                min + 0.50 * range,
                min + 0.25 * range,
                min
        };

        for (double value : values) {
            int yTick = legendY(value, min, max, y, height);

            // маленькая риска справа от шкалы
            g.drawLine(x + width, yTick, x + width + 6, yTick);

            // подпись температуры
            g.drawString(
                    formatTemperature(value, numberFormat),
                    x + width + 10,
                    yTick + 4);
        }
    }

    private int legendY(double value, double min, double max, int legendTop, int legendHeight) {
        if (max == min) {
            return legendTop + legendHeight / 2;
        }

        double t = (value - min) / (max - min);
        double invertedT = 1.0 - t;

        return legendTop + (int) Math.round(invertedT * legendHeight);
    }
}