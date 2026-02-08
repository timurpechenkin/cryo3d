package io.github.timurpechenkin.casefile.resolve;

import static io.github.timurpechenkin.geometry.GeometryScale.metersToScaled2;

import java.util.List;

import io.github.timurpechenkin.casefile.dto.common.Rule;
import io.github.timurpechenkin.casefile.dto.selector.BoxSelector;
import io.github.timurpechenkin.casefile.dto.selector.Selector;
import io.github.timurpechenkin.casefile.dto.selector.ZRangeSelector;
import io.github.timurpechenkin.domain.model.Library;

public class Compiler<T> {
    public List<CompiledRule> compileRules(List<Rule<String>> rules, Library<T> lib) {
        if (rules == null || rules.isEmpty())
            return List.of();

        return rules.stream().map(rule -> {
            if (rule == null)
                throw new IllegalArgumentException("Rule is null");
            if (rule.selector() == null)
                throw new IllegalArgumentException("Rule '" + rule.name() + "' has null selector");
            if (rule.value() == null)
                throw new IllegalArgumentException("Rule '" + rule.name() + "' has null value");
            int idx = lib.indexOf(rule.value());
            return new CompiledRule(rule.name(), compileSelector(rule.selector()), idx);
        }).toList();
    }

    private CompiledSelector compileSelector(Selector s) {
        return switch (s) {
            case BoxSelector box -> {
                // BOX bounds in yaml are meters; convert to SCALE*2
                int xMin2 = metersToScaled2(box.minXMeters());
                int yMin2 = metersToScaled2(box.minYMeters());
                int zMin2 = metersToScaled2(box.minZMeters());
                int xMax2 = metersToScaled2(box.maxXMeters());
                int yMax2 = metersToScaled2(box.maxYMeters());
                int zMax2 = metersToScaled2(box.maxZMeters());

                // Convention: [min, max) to avoid double hits on boundaries
                yield (cx2, cy2, cz2) -> cx2 >= xMin2 && cx2 < xMax2 &&
                        cy2 >= yMin2 && cy2 < yMax2 &&
                        cz2 >= zMin2 && cz2 < zMax2;
            }
            case ZRangeSelector range -> {
                int zMin2 = metersToScaled2(range.minZMeters());
                int zMax2 = metersToScaled2(range.maxZMeters());
                yield (cx2, cy2, cz2) -> cz2 >= zMin2 && cz2 < zMax2;
            }
        };
    }
}
