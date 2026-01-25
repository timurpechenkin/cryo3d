package io.github.timurpechenkin.casefile.resolve;

import static io.github.timurpechenkin.geometry.GeometryScale.toScaled2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.timurpechenkin.casefile.dto.common.Field;
import io.github.timurpechenkin.casefile.dto.common.Rule;
import io.github.timurpechenkin.casefile.dto.selector.BoxSelector;
import io.github.timurpechenkin.casefile.dto.selector.Selector;
import io.github.timurpechenkin.casefile.dto.selector.ZRangeSelector;
import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.domain.model.Library;

public class AbstractDiscretizer3D<T> {

    protected final int[] discretizeToIndex(Grid grid, Field<String> field, Library<T> lib) {
        Objects.requireNonNull(grid, "grid");
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(lib, "lib");
        Objects.requireNonNull(field.defaultValue(), "field.defaultValue");

        int nx = grid.nx();
        int ny = grid.ny();
        int nz = grid.nz();

        long cellCount = grid.cellCount();
        if (cellCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Grid too large for int[] field: cellCount=" + cellCount);
        }

        int[] indexArr = new int[(int) cellCount];

        // default item id -> defaultIndex
        int defaultIndex = lib.indexOf(field.defaultValue());

        // compile rules once
        List<CompiledRule> compiledRules = compileRules(field.rules(), lib);

        // fill
        int idx = 0;
        for (int k = 0; k < nz; k++) {
            int cz2 = grid.centerZScaled2(k);
            for (int j = 0; j < ny; j++) {
                int cy2 = grid.centerYScaled2(j);
                for (int i = 0; i < nx; i++) {
                    int cx2 = grid.centerXScaled2(i);

                    int value = defaultIndex;
                    for (CompiledRule rule : compiledRules) {
                        if (rule.selector.contains(cx2, cy2, cz2)) {
                            value = rule.itemIndex;
                        }
                    }

                    indexArr[idx++] = value;
                }
            }
        }

        return indexArr;
    }

    private List<CompiledRule> compileRules(List<Rule<String>> rules, Library<T> lib) {
        if (rules == null || rules.isEmpty())
            return List.of();

        List<CompiledRule> compiledRules = new ArrayList<>(rules.size());
        for (Rule<String> rule : rules) {
            if (rule == null)
                throw new IllegalArgumentException("Rule is null");
            if (rule.selector() == null)
                throw new IllegalArgumentException("Rule '" + rule.name() + "' has null selector");
            if (rule.value() == null) {
                throw new IllegalArgumentException("Rule '" + rule.name() + "' has null value");
            }

            int matIndex = lib.indexOf(rule.value());
            CompiledSelector selector = compileSelector(rule.selector());

            compiledRules.add(new CompiledRule(rule.name(), selector, matIndex));
        }
        return compiledRules;
    }

    private CompiledSelector compileSelector(Selector s) {
        return switch (s) {
            case BoxSelector box -> {
                // BOX bounds in yaml are meters; convert to SCALE*2
                int xMin2 = toScaled2(box.minXMeters());
                int yMin2 = toScaled2(box.minYMeters());
                int zMin2 = toScaled2(box.minZMeters());
                int xMax2 = toScaled2(box.maxXMeters());
                int yMax2 = toScaled2(box.maxYMeters());
                int zMax2 = toScaled2(box.maxZMeters());

                // Convention: [min, max) to avoid double hits on boundaries
                yield (cx2, cy2, cz2) -> cx2 >= xMin2 && cx2 < xMax2 &&
                        cy2 >= yMin2 && cy2 < yMax2 &&
                        cz2 >= zMin2 && cz2 < zMax2;
            }
            case ZRangeSelector range -> {
                int zMin2 = toScaled2(range.minZMeters());
                int zMax2 = toScaled2(range.maxZMeters());
                yield (cx2, cy2, cz2) -> cz2 >= zMin2 && cz2 < zMax2;
            }
        };
    }

    // --- compiled structures ---

    @FunctionalInterface
    private interface CompiledSelector {
        boolean contains(int cx2, int cy2, int cz2);
    }

    private record CompiledRule(String name, CompiledSelector selector, int itemIndex) {
    }
}
