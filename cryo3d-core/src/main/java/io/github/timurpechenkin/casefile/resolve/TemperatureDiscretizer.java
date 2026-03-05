package io.github.timurpechenkin.casefile.resolve;

import io.github.timurpechenkin.casefile.dto.common.Field;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.model.Library;
import io.github.timurpechenkin.domain.temperature.Temperature;
import io.github.timurpechenkin.domain.temperature.TemperatureField;

public class TemperatureDiscretizer extends AbstractDiscretizer3D<Temperature> {
    public TemperatureField discretize(Grid3D grid, Field<String> field, Library<Temperature> lib) {
        int[] indexArr = discretizeToIndex(grid, field, lib);
        double[] temperatureArr = new double[indexArr.length];

        for (int i = 0; i < temperatureArr.length; i++) {
            Temperature temperature = lib.getByIndex(indexArr[i]);
            switch (temperature.type()) {
                case CONSTANT:
                    temperatureArr[i] = temperature.value();
                    break;

                default:
                    throw new UnsupportedOperationException("Only CONSTANT temperature type supported in this version");
            }
        }

        return new TemperatureField(temperatureArr);
    }

}
