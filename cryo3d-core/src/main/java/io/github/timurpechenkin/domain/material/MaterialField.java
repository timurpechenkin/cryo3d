package io.github.timurpechenkin.domain.material;

public final class MaterialField {
    private final int[] materialIndexByCell; // length = grid.cellCount()

    public MaterialField(int[] materialIndexByCell) {
        this.materialIndexByCell = materialIndexByCell;
    }

    public static MaterialField empty() {
        return new MaterialField(new int[0]);
    }

    public int[] materialIndexByCell() {
        return materialIndexByCell;
    }
}
