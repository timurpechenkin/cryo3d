package io.github.timurpechenkin.domain.material;

public final class MaterialField {
    private final int[] materialIndexByCell;

    public MaterialField(int[] materialIndexByCell) {
        this.materialIndexByCell = materialIndexByCell;
    }

    public int[] materialIndexByCell() {
        return materialIndexByCell;
    }
}
