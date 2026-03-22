package io.github.timurpechenkin.domain.material;

public final class MaterialField {
    private final int[] materialIdByCell;

    public MaterialField(int[] materialIndexByCell) {
        this.materialIdByCell = materialIndexByCell;
    }

    public int[] materialIdByCell() {
        return materialIdByCell;
    }
}
