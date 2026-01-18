package io.github.timurpechenkin.domain.grid;

public interface Grid {

    // Общее количество ячеек

    public long cellCount();

    // Количество ячеек по оси

    /** Количетво ячеек по оси X */
    public int nx();

    /** Количетво ячеек по оси Y */
    public int ny();

    /** Количетво ячеек по оси Z */
    public int nz();

    // ----- ВНЕШНИЙ API (метры double) -----

    // Центр i-ой ячейки

    public double centerXMeters(int i);

    public double centerYMeters(int j);

    public double centerZMeters(int k);

    // Объём ячейки по индексам

    public double cellVolumeMeters3(int i, int j, int k);

    // Поиск индекса ячейки по координате

    public int findCellX(double xMeters);

    public int findCellY(double yMeters);

    public int findCellZ(double zMeters);

    // Длинна осей

    double sizeMetersX();

    double sizeMetersY();

    double sizeMetersZ();

    // ----- ВНУТРЕННИЙ API (scaled int) -----

    // Масивы координаты ячеек по осям

    // Для Х

    /**
     * Координаты ребер ячеек (edges.length = cells + 1).
     * edges[i] - левый край i-й ячейки, edges[i+1] - правый. Умножены на SCALE.
     */
    public int[] edgesScaledX();

    /** Центры ячеек (centers.length = cells). Умножены на SCALE*2. */
    int[] centersScaled2X();

    /** Длины ячеек (steps.length = cells). Умножены на SCALE. */
    int[] stepsScaledX();

    // Для Y

    /**
     * Координаты ребер ячеек (edges.length = cells + 1).
     * edges[i] - левый край i-й ячейки, edges[i+1] - правый. Умножены на SCALE.
     */
    int[] edgesScaledY();

    /** Центры ячеек (centers.length = cells). Умножены на SCALE*2. */
    int[] centersScaled2Y();

    /** Длины ячеек (steps.length = cells). Умножены на SCALE. */
    int[] stepsScaledY();

    // Для Z

    /**
     * Координаты ребер ячеек (edges.length = cells + 1).
     * edges[i] - левый край i-й ячейки, edges[i+1] - правый. Умножены на SCALE.
     */
    int[] edgesScaledZ();

    /** Центры ячеек (centers.length = cells). Умножены на SCALE*2. */
    int[] centersScaled2Z();

    /** Длины ячеек (steps.length = cells). Умножены на SCALE. */
    int[] stepsScaledZ();

    // Центр i-ой ячейки

    public int centerXScaled2(int i);

    public int centerYScaled2(int j);

    public int centerZScaled2(int k);

    // Объём ячейки по индексам

    public long cellVolumeScaled3(int i, int j, int k);

    // Поиск индекса ячейки по координате*SCALE

    public int findCellXScaled(int x);

    public int findCellYScaled(int y);

    public int findCellZScaled(int z);

    // Длинна осей

    int sizeScaledX();

    int sizeScaledY();

    int sizeScaledZ();
}
