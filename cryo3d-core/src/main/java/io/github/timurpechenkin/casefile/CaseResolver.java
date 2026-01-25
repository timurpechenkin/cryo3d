package io.github.timurpechenkin.casefile;

import static io.github.timurpechenkin.geometry.GeometryScale.toScaled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.timurpechenkin.casefile.dto.geometry.*;
import io.github.timurpechenkin.casefile.dto.SimulationCaseDto;
import io.github.timurpechenkin.casefile.dto.bc.BoundaryConditionDefinition;
import io.github.timurpechenkin.casefile.dto.grid.GridSpecDto;
import io.github.timurpechenkin.casefile.dto.material.MaterialDefinition;
import io.github.timurpechenkin.casefile.dto.temperature.TemperatureDefinition;
import io.github.timurpechenkin.casefile.dto.time.TimeSettingsDto;
import io.github.timurpechenkin.casefile.resolve.GridResolver;
import io.github.timurpechenkin.casefile.resolve.MaterialDiscretizer;
import io.github.timurpechenkin.casefile.resolve.TemperatureDiscretizer;
import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.bc.BoundaryCondition;
import io.github.timurpechenkin.domain.bc.BoundaryConditionField;
import io.github.timurpechenkin.domain.bc.BoundaryConditionLibrary;
import io.github.timurpechenkin.domain.grid.Grid;
import io.github.timurpechenkin.domain.material.Material;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.temperature.Temperature;
import io.github.timurpechenkin.domain.temperature.TemperatureField;
import io.github.timurpechenkin.domain.temperature.TemperatureLibrary;
import io.github.timurpechenkin.domain.time.TimeSettings;
import io.github.timurpechenkin.geometry.Point3D;
import io.github.timurpechenkin.geometry.Profile;

public final class CaseResolver {
    private final MaterialDiscretizer materialDiscretizer = new MaterialDiscretizer();
    private final TemperatureDiscretizer temperatureDiscretizer = new TemperatureDiscretizer();

    public SimulationCase resolve(SimulationCaseDto dto) {
        // 1) time
        TimeSettings time = resolveTime(dto.time());

        // 2) grid
        Grid grid = resolveGrid(dto.grid());

        // 3) libraries
        BoundaryConditionLibrary bcLibrary = resolveBcLibrary(dto.boundaryConditions().definitions());
        MaterialLibrary materialLibrary = resolveMaterialLibrary(dto.materials().definitions());
        TemperatureLibrary temperatureLibrary = resolveTemperatureLibrary(dto.temperature().definitions());

        // 4) profiles
        List<Profile> profiles = resolveProfiles(dto.profiles());

        // 5) fields
        BoundaryConditionField bcField = BoundaryConditionField.empty();
        MaterialField materialField = materialDiscretizer.discretize(grid, dto.materials().field(),
                materialLibrary);
        TemperatureField temperatureField = temperatureDiscretizer.discretize(grid, dto.temperature().field(),
                temperatureLibrary);

        return new SimulationCase(
                dto.caseName(),
                time,
                grid,
                bcLibrary,
                bcField,
                materialLibrary,
                materialField,
                temperatureLibrary,
                temperatureField,
                profiles);
    }

    private TimeSettings resolveTime(TimeSettingsDto t) {
        return new TimeSettings(t.dtSeconds(), t.saveEverySeconds(), t.totalSeconds());
    }

    private Grid resolveGrid(GridSpecDto g) {
        return GridResolver.virtualGridFrom(g);
    }

    private TemperatureLibrary resolveTemperatureLibrary(Map<String, TemperatureDefinition> defs) {
        TemperatureLibrary lib = new TemperatureLibrary();
        for (var entry : defs.entrySet()) {
            String id = entry.getKey();
            TemperatureDefinition m = entry.getValue();

            Temperature material = new Temperature(
                    id,
                    m.type(),
                    m.temperature());

            lib.add(id, material);
        }
        return lib;
    }

    private MaterialLibrary resolveMaterialLibrary(Map<String, MaterialDefinition> defs) {
        MaterialLibrary lib = new MaterialLibrary();
        for (var entry : defs.entrySet()) {
            String id = entry.getKey();
            MaterialDefinition m = entry.getValue();

            Material material = new Material(
                    id,
                    m.thermalConductivityThawed(),
                    m.thermalConductivityFrozen(),
                    m.heatCapacityThawed(),
                    m.heatCapacityFrozen(),
                    m.phaseTransitionsHeat(),
                    m.freezingTemperature());

            lib.add(id, material);
        }
        return lib;
    }

    private BoundaryConditionLibrary resolveBcLibrary(Map<String, BoundaryConditionDefinition> defs) {
        BoundaryConditionLibrary lib = new BoundaryConditionLibrary();
        for (var entry : defs.entrySet()) {
            String id = entry.getKey();
            BoundaryConditionDefinition d = entry.getValue();

            BoundaryCondition bc = new BoundaryCondition(
                    id,
                    d.type(),
                    d.temperature(),
                    d.heatFlow(),
                    d.heatTransferCoefficient());

            lib.add(id, bc);
        }
        return lib;
    }

    private List<Profile> resolveProfiles(List<ProfileDto> dtos) {
        List<Profile> profiles = new ArrayList<>(dtos.size());
        for (var p : dtos) {
            List<Point3D> pts = new ArrayList<>(p.points().size());
            for (var pt : p.points()) {
                pts.add(new Point3D(toScaled(pt.x()), toScaled(pt.y()), toScaled(pt.z())));
            }
            profiles.add(new Profile(p.name(), pts));
        }
        return profiles;
    }
}
