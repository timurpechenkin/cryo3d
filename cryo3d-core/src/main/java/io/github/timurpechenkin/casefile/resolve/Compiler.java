package io.github.timurpechenkin.casefile.resolve;

import static io.github.timurpechenkin.geometry.GeometryScale.metersToScaled2;

import java.util.List;

import io.github.timurpechenkin.casefile.dto.common.Rule;
import io.github.timurpechenkin.casefile.dto.recording.RadiusPointDto;
import io.github.timurpechenkin.casefile.dto.selector.BoxSelector;
import io.github.timurpechenkin.casefile.dto.selector.RevolutionSelector;
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
            int idx = lib.idOf(rule.value());
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
            case RevolutionSelector revolution -> compileRevolutionSelector(revolution);
        };
    }

    private CompiledSelector compileRevolutionSelector(RevolutionSelector revolution) {
        int centerX2 = metersToScaled2(revolution.centerX());
        int centerY2 = metersToScaled2(revolution.centerY());

        List<RadiusPointDto> points = revolution.radiusPoints();
        int size = points.size();

        int[] z2 = new int[size];
        int[] r2 = new int[size];

        for (int i = 0; i < size; i++) {
            RadiusPointDto point = points.get(i);
            z2[i] = metersToScaled2(point.z());
            r2[i] = metersToScaled2(point.radius());
        }

        int zMin2 = z2[0];
        int zMax2 = z2[size - 1];

        return (cx2, cy2, cz2) -> {
            if (cz2 < zMin2 || cz2 >= zMax2) {
                return false;
            }

            int segmentIndex = findSegmentIndex(z2, cz2);
            int zLeft2 = z2[segmentIndex];
            int zRight2 = z2[segmentIndex + 1];
            int rLeft2 = r2[segmentIndex];
            int rRight2 = r2[segmentIndex + 1];

            double t = (double) (cz2 - zLeft2) / (double) (zRight2 - zLeft2);
            double radius2 = rLeft2 + t * (rRight2 - rLeft2);

            long dx2 = (long) cx2 - centerX2;
            long dy2 = (long) cy2 - centerY2;

            double distanceSquared = (double) dx2 * dx2 + (double) dy2 * dy2;
            double radiusSquared = radius2 * radius2;

            return distanceSquared <= radiusSquared;
        };
    }

    private int findSegmentIndex(int[] z2, int cz2) {
        for (int i = 0; i < z2.length - 1; i++) {
            if (cz2 >= z2[i] && cz2 < z2[i + 1]) {
                return i;
            }
        }

        throw new IllegalStateException("No segment found for cz2=" + cz2);
    }
}
