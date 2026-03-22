package io.github.timurpechenkin.casefile;

import static io.github.timurpechenkin.geometry.GeometryScale.metersToScaled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.timurpechenkin.casefile.dto.SimulationCaseDto;
import io.github.timurpechenkin.casefile.dto.bc.BoundaryConditionDefinition;
import io.github.timurpechenkin.casefile.dto.grid.GridSpecDto;
import io.github.timurpechenkin.casefile.dto.material.MaterialDefinition;
import io.github.timurpechenkin.casefile.dto.measurement.*;
import io.github.timurpechenkin.casefile.dto.temperature.TemperatureDefinition;
import io.github.timurpechenkin.casefile.dto.time.TimeSettingsDto;
import io.github.timurpechenkin.casefile.resolve.BoundaryConditionDiscretizer;
import io.github.timurpechenkin.casefile.resolve.GridResolver;
import io.github.timurpechenkin.casefile.resolve.MaterialDiscretizer;
import io.github.timurpechenkin.casefile.resolve.ProfileDiscretizer;
import io.github.timurpechenkin.casefile.resolve.TemperatureDiscretizer;
import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.bc.BoundaryCondition;
import io.github.timurpechenkin.domain.bc.BoundaryConditionField;
import io.github.timurpechenkin.domain.bc.BoundaryConditionLibrary;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.material.Material;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.measurement.Profile;
import io.github.timurpechenkin.domain.measurement.SamplePoint;
import io.github.timurpechenkin.domain.temperature.Temperature;
import io.github.timurpechenkin.domain.temperature.TemperatureField;
import io.github.timurpechenkin.domain.temperature.TemperatureLibrary;
import io.github.timurpechenkin.domain.time.TimeSettings;
import io.github.timurpechenkin.geometry.Axis3D;
import io.github.timurpechenkin.geometry.Point3D;

public final class CaseResolver {
    private final MaterialDiscretizer materialDiscretizer = new MaterialDiscretizer();
    private final TemperatureDiscretizer temperatureDiscretizer = new TemperatureDiscretizer();
    private final BoundaryConditionDiscretizer bcDiscretizer = new BoundaryConditionDiscretizer();
    private final ProfileDiscretizer profileDiscretizer = new ProfileDiscretizer();

    public SimulationCase resolve(SimulationCaseDto dto) {

        // 1) time
        TimeSettings time = resolveTime(dto.time());

        // 2) grid
        Grid3D grid = resolveGrid(dto.grid());

        // 3) libraries
        BoundaryConditionLibrary bcLibrary = resolveBcLibrary(dto.boundaryConditions().definitions());
        MaterialLibrary materialLibrary = resolveMaterialLibrary(dto.materials().definitions());
        TemperatureLibrary temperatureLibrary = resolveTemperatureLibrary(dto.temperature().definitions());

        // 4) fields
        BoundaryConditionField bcField = bcDiscretizer.discretize(grid, dto.boundaryConditions().field().faces(),
                bcLibrary);
        MaterialField materialField = materialDiscretizer.discretize(grid, dto.materials().field(),
                materialLibrary);
        TemperatureField temperatureField = temperatureDiscretizer.discretize(grid, dto.temperature().field(),
                temperatureLibrary);

        // 5) profiles
        List<Profile> profiles = new ArrayList<>();
        for (ProfileDto profileDto : dto.profiles()) {
            Profile profile = profileDiscretizer.discretize(grid, profileDto);
            profiles.add(profile);
        }

        // 6) sample points
        List<SamplePoint> samplePoints = resolveSamplePoints(dto.samplePoints(), grid);

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
                profiles,
                samplePoints);
    }

    private TimeSettings resolveTime(TimeSettingsDto t) {
        return new TimeSettings(t.startDate(), t.endDate(), t.dtSeconds(), t.saveEverySeconds());
    }

    private Grid3D resolveGrid(GridSpecDto g) {
        return GridResolver.virtualGridFrom(g);
    }

    private TemperatureLibrary resolveTemperatureLibrary(Map<String, TemperatureDefinition> defs) {
        TemperatureLibrary lib = new TemperatureLibrary();
        for (var entry : defs.entrySet()) {
            String id = entry.getKey();
            TemperatureDefinition m = entry.getValue();

            Temperature temp = new Temperature(
                    id,
                    m.type(),
                    m.temperature());

            lib.add(id, temp);
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
                    d.ambientTemperature(),
                    d.heatTransferCoefficient());

            lib.add(id, bc);
        }
        return lib;
    }

    private List<SamplePoint> resolveSamplePoints(List<SamplePointDto> dtos, Grid3D grid) {
        List<SamplePoint> samplePoints = new ArrayList<>();
        for (SamplePointDto dto : dtos) {
            int x = grid.findCellScaled(Axis3D.X, metersToScaled(dto.point().x()));
            int y = grid.findCellScaled(Axis3D.Y, metersToScaled(dto.point().y()));
            int z = grid.findCellScaled(Axis3D.Z, metersToScaled(dto.point().z()));
            int cellIndex = grid.index(x, y, z);
            samplePoints.add(new SamplePoint(dto.name(), toPoint3d(dto.point()), cellIndex));
        }
        return samplePoints;
    }

    private Point3D toPoint3d(PointDto dto) {
        return new Point3D(metersToScaled(dto.x()), metersToScaled(dto.y()), metersToScaled(dto.z()));
    }
}
