package io.github.timurpechenkin.casefile;

import static io.github.timurpechenkin.geometry.GeometryScale.metersToScaled;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.timurpechenkin.casefile.dto.SimulationCaseDto;
import io.github.timurpechenkin.casefile.dto.bc.BoundaryConditionDefinition;
import io.github.timurpechenkin.casefile.dto.config.CaseConfigDto;
import io.github.timurpechenkin.casefile.dto.grid.GridSpecDto;
import io.github.timurpechenkin.casefile.dto.material.MaterialDefinition;
import io.github.timurpechenkin.casefile.dto.recording.PointDto;
import io.github.timurpechenkin.casefile.dto.recording.ProfileDto;
import io.github.timurpechenkin.casefile.dto.recording.SamplePointDto;
import io.github.timurpechenkin.casefile.dto.temperature.TemperatureDefinition;
import io.github.timurpechenkin.casefile.dto.time.TimeSettingsDto;
import io.github.timurpechenkin.casefile.resolve.BoundaryConditionDiscretizer;
import io.github.timurpechenkin.casefile.resolve.GridResolver;
import io.github.timurpechenkin.casefile.resolve.MaterialDiscretizer;
import io.github.timurpechenkin.casefile.resolve.OrthogonalProfileDiscretizer;
import io.github.timurpechenkin.casefile.resolve.ProfileDiscretizer;
import io.github.timurpechenkin.casefile.resolve.TemperatureDiscretizer;
import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.bc.BoundaryCondition;
import io.github.timurpechenkin.domain.bc.BoundaryConditionField;
import io.github.timurpechenkin.domain.bc.BoundaryConditionLibrary;
import io.github.timurpechenkin.domain.bc.BoundaryConditionSetup;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.material.Material;
import io.github.timurpechenkin.domain.material.MaterialField;
import io.github.timurpechenkin.domain.material.MaterialLibrary;
import io.github.timurpechenkin.domain.material.MaterialSetup;
import io.github.timurpechenkin.domain.metadata.CaseMetadata;
import io.github.timurpechenkin.domain.SimulationModel;
import io.github.timurpechenkin.domain.presentation.NumberFormat;
import io.github.timurpechenkin.domain.presentation.PresentationSettings;
import io.github.timurpechenkin.domain.recording.Profile;
import io.github.timurpechenkin.domain.recording.RecordingSettings;
import io.github.timurpechenkin.domain.recording.SamplePoint;
import io.github.timurpechenkin.domain.solver.SolverSettings;
import io.github.timurpechenkin.domain.temperature.Temperature;
import io.github.timurpechenkin.domain.temperature.TemperatureField;
import io.github.timurpechenkin.domain.temperature.TemperatureLibrary;
import io.github.timurpechenkin.domain.temperature.TemperatureSetup;
import io.github.timurpechenkin.domain.time.TimeSettings;
import io.github.timurpechenkin.geometry.Axis3D;
import io.github.timurpechenkin.geometry.Point3D;

public final class CaseResolver {
    private final MaterialDiscretizer materialDiscretizer = new MaterialDiscretizer();
    private final TemperatureDiscretizer temperatureDiscretizer = new TemperatureDiscretizer();
    private final BoundaryConditionDiscretizer bcDiscretizer = new BoundaryConditionDiscretizer();
    private final ProfileDiscretizer profileDiscretizer = new OrthogonalProfileDiscretizer();

    public SimulationCase resolve(SimulationCaseDto dto) {

        // 1) model
        TimeSettings time = resolveTime(dto.time());
        Grid3D grid = resolveGrid(dto.grid());

        BoundaryConditionLibrary bcLibrary = resolveBcLibrary(dto.boundaryConditions().definitions());
        MaterialLibrary materialLibrary = resolveMaterialLibrary(dto.materials().definitions());
        TemperatureLibrary temperatureLibrary = resolveTemperatureLibrary(dto.temperature().definitions());

        BoundaryConditionField bcField = bcDiscretizer.discretize(
                grid,
                dto.boundaryConditions().field().faces(),
                bcLibrary);

        MaterialField materialField = materialDiscretizer.discretize(
                grid,
                dto.materials().field(),
                materialLibrary);

        TemperatureField temperatureField = temperatureDiscretizer.discretize(
                grid,
                dto.temperature().field(),
                temperatureLibrary);

        SimulationModel model = new SimulationModel(
                time,
                grid,
                new BoundaryConditionSetup(bcLibrary, bcField),
                new MaterialSetup(materialLibrary, materialField),
                new TemperatureSetup(temperatureLibrary, temperatureField));

        // 2) recording
        List<Profile> profiles = resolveProfiles(dto.profiles(), grid);
        List<SamplePoint> samplePoints = resolveSamplePoints(dto.samplePoints(), grid);

        RecordingSettings recording = new RecordingSettings(profiles, samplePoints);

        // 3) metadata
        CaseMetadata metadata = new CaseMetadata(dto.caseName());

        // 4) solver + presentation
        SolverSettings solver = resolveSolverSettings(dto.config());
        PresentationSettings presentation = resolvePresentationSettings(dto.config());

        return new SimulationCase(
                metadata,
                model,
                solver,
                recording,
                presentation);
    }

    private SolverSettings resolveSolverSettings(CaseConfigDto dto) {
        return new SolverSettings(dto.stepCalculatorKey(), dto.materialModelKey());
    }

    private PresentationSettings resolvePresentationSettings(CaseConfigDto dto) {
        RoundingMode roundingMode = RoundingMode.valueOf(dto.numberFormat().roundingMode());
        NumberFormat numberFormat = new NumberFormat(
                dto.numberFormat().fractionDigits(),
                roundingMode);

        return new PresentationSettings(dto.timeFormat(), numberFormat);
    }

    private TimeSettings resolveTime(TimeSettingsDto dto) {
        return new TimeSettings(dto.startDate(), dto.endDate(), dto.dtSeconds());
    }

    private Grid3D resolveGrid(GridSpecDto dto) {
        return GridResolver.virtualGridFrom(dto);
    }

    private List<Profile> resolveProfiles(List<ProfileDto> dtos, Grid3D grid) {
        List<Profile> profiles = new ArrayList<>();
        for (ProfileDto dto : dtos) {
            profiles.add(profileDiscretizer.discretize(grid, dto));
        }
        return profiles;
    }

    private TemperatureLibrary resolveTemperatureLibrary(Map<String, TemperatureDefinition> defs) {
        TemperatureLibrary lib = new TemperatureLibrary();

        for (var entry : defs.entrySet()) {
            String id = entry.getKey();
            TemperatureDefinition def = entry.getValue();

            Temperature temperature = new Temperature(
                    id,
                    def.type(),
                    def.temperature());

            lib.add(id, temperature);
        }

        return lib;
    }

    private MaterialLibrary resolveMaterialLibrary(Map<String, MaterialDefinition> defs) {
        MaterialLibrary lib = new MaterialLibrary();

        for (var entry : defs.entrySet()) {
            String id = entry.getKey();
            MaterialDefinition def = entry.getValue();

            Material material = new Material(
                    id,
                    def.thermalConductivityThawed(),
                    def.thermalConductivityFrozen(),
                    def.heatCapacityThawed(),
                    def.heatCapacityFrozen(),
                    def.phaseTransitionsHeat(),
                    def.freezingTemperature());

            lib.add(id, material);
        }

        return lib;
    }

    private BoundaryConditionLibrary resolveBcLibrary(Map<String, BoundaryConditionDefinition> defs) {
        BoundaryConditionLibrary lib = new BoundaryConditionLibrary();

        for (var entry : defs.entrySet()) {
            String id = entry.getKey();
            BoundaryConditionDefinition def = entry.getValue();

            BoundaryCondition bc = new BoundaryCondition(
                    id,
                    def.type(),
                    def.temperature(),
                    def.heatFlow(),
                    def.ambientTemperature(),
                    def.heatTransferCoefficient());

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

            samplePoints.add(new SamplePoint(
                    dto.name(),
                    dto.saveStep(),
                    toPoint3d(dto.point()),
                    cellIndex));
        }

        return samplePoints;
    }

    private Point3D toPoint3d(PointDto dto) {
        return new Point3D(
                metersToScaled(dto.x()),
                metersToScaled(dto.y()),
                metersToScaled(dto.z()));
    }
}