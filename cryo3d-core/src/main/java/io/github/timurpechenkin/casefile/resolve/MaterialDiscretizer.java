package io.github.timurpechenkin.casefile.resolve;

import io.github.timurpechenkin.casefile.dto.common.Field;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.material.Material;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.model.Library;

public class MaterialDiscretizer extends AbstractDiscretizer3D<Material> {

    public MaterialField discretize(Grid3D grid, Field<String> field, Library<Material> lib) {
        return new MaterialField(discretizeToIndex(grid, field, lib));
    }

}
