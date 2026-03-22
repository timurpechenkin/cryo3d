package io.github.timurpechenkin.casefile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.timurpechenkin.geometry.GeometryScale.*;
import static io.github.timurpechenkin.Constants.*;

import io.github.timurpechenkin.casefile.dto.SimulationCaseDto;
import io.github.timurpechenkin.casefile.dto.common.Field;
import io.github.timurpechenkin.casefile.dto.common.Rule;
import io.github.timurpechenkin.casefile.dto.grid.Segment;
import io.github.timurpechenkin.casefile.dto.selector.BoxSelector;
import io.github.timurpechenkin.casefile.dto.selector.Selector;
import io.github.timurpechenkin.casefile.dto.selector.ZRangeSelector;
import io.github.timurpechenkin.casefile.dto.temperature.TemperatureDefinition;
import io.github.timurpechenkin.casefile.validation.ValidationResult;
import io.github.timurpechenkin.geometry.Axis3D;
import io.github.timurpechenkin.geometry.Face;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public final class CaseValidator {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public CaseValidator() {
    }

    public ValidationResult validate(SimulationCaseDto simulationCase) {
        ValidationResult result = new ValidationResult();

        if (simulationCase == null) {
            result.add("", "Case is null");
            return result;
        }

        // Bean Validation (аннотации)
        Set<ConstraintViolation<SimulationCaseDto>> violations = VALIDATOR.validate(simulationCase);
        for (ConstraintViolation<SimulationCaseDto> v : violations) {
            result.add(v.getPropertyPath().toString(), v.getMessage());
        }

        // СМЫСЛОВЫЕ ПРОВЕРКИ
        // 1) Проверка параметров сетки
        if (simulationCase.grid() == null) {
            result.add("grid", "grid params must not be empty");
        } else {
            Map<Axis3D, List<Segment>> axesSegments = simulationCase.grid().axesSegments();

            for (Axis3D axis : axesSegments.keySet()) {
                List<Segment> segments = axesSegments.get(axis);
                if (segments == null || segments.isEmpty()) {
                    result.add("grid.axes." + axis.name(), "axis segment must not be empty");
                } else {
                    int last = 0;
                    for (int i = 0; i < segments.size(); i++) {
                        Segment s = segments.get(i);
                        double from = s.from();
                        double to = s.to();
                        double step = s.step();

                        int intFrom = metersToScaled(from);
                        int intTo = metersToScaled(to);
                        int intStep = metersToScaled(step);

                        // Проверка кратности длинны сегмента и шага
                        if ((intTo - intFrom) % intStep != 0) {
                            result.add("grid.axes." + axis.name() + ".segments[" + i + "].from",
                                    "the segment length must be evenly divisible by the step size");
                        }

                        // Проврека отсутствия наложений и разрывов + первый сегмент начинается с 0
                        if (intFrom != last) {
                            result.add("grid.axes." + axis.name() + ".segments[" + i + "].from",
                                    "segments must adjoin each other without gaps and overlaps");
                        }
                        last = intTo;

                        // Проверка, что шаг не слишком велик или мал
                        if (step > 10 || step < 0.01) {
                            result.add("grid.axes." + axis.name() + ".segments[" + i + "].step",
                                    "step must be between 0.1 and 10");
                        }
                    }
                }
            }
        }

        // 2) Проверка временных параметров
        if (simulationCase.time() == null) {
            result.add("time", "time params must not be empty");
        } else {
            LocalDateTime startDate = simulationCase.time().startDate();
            LocalDateTime endDate = simulationCase.time().endDate();
            int startDateYear = startDate.getYear();
            int endDateYear = endDate.getYear();
            long dt = simulationCase.time().dtSeconds();
            long save = simulationCase.time().saveEverySeconds();
            long total = Duration.between(startDate, endDate).getSeconds();
            if (dt <= 0)
                result.add("time.dtSeconds", "dtSeconds must be more than zero");
            if (save < dt)
                result.add("time.saveEverySeconds", "saveEverySeconds must be >= dtSeconds");
            if (save % dt != 0)
                result.add("time.saveEverySeconds", "saveEverySeconds must be multiple of dtSeconds");
            if (dt * 5 > total)
                result.add("time.dtSeconds",
                        "dtSeconds must be at least 5 times smaller than total duration (endDate-startDate)");
            if (!endDate.isAfter(startDate)
                    || endDateYear - startDateYear > 1000)
                result.add("time.endDate",
                        "endDate must be after startDate, and the difference between them must be less than 1000 years: startDate="
                                + simulationCase.time().startDate()
                                + ", endDate=" + simulationCase.time().endDate());
        }

        // 3) Проверка materials
        if (simulationCase.materials() != null) {
            Map<String, ?> defs = simulationCase.materials().definitions();
            Field<String> field = simulationCase.materials().field();

            if (defs == null || defs.isEmpty()) {
                result.add("materials.definitions", "definitions must not be empty");
            } else if (field != null) {
                checkRef(result, "materials.field.default", field.defaultValue(), defs);

                List<Rule<String>> rules = field.rules();
                if (rules != null) {
                    for (int i = 0; i < rules.size(); i++) {
                        Rule<String> rule = rules.get(i);
                        if (rule != null) {
                            checkRef(result, "materials.field.rules[" + i + "].value", rule.value(), defs);
                            checkSelector(result, "materials.field.rules[" + i + "].seletor", rule.selector());
                        }
                    }
                }
            }
        }

        // 4) Проверка температур
        if (simulationCase.temperature() != null) {
            Map<String, TemperatureDefinition> defs = simulationCase.temperature().definitions();
            Field<String> field = simulationCase.temperature().field();

            if (defs == null || defs.isEmpty()) {
                result.add("temperature.definitions", "definitions must not be empty");
            } else {
                List<TemperatureDefinition> temperatures = new ArrayList<>(defs.values());
                for (int i = 0; i < temperatures.size(); i++) {
                    checkTemperatureValue(result, "temperature.definitions[" + i + "]", temperatures.get(i));
                }

                if (field != null) {
                    checkRef(result, "temperature.field.default", field.defaultValue(), defs);

                    List<Rule<String>> rules = field.rules();
                    if (rules != null) {
                        for (int i = 0; i < rules.size(); i++) {
                            Rule<String> rule = rules.get(i);
                            if (rule != null) {
                                checkRef(result, "temperature.field.rules[" + i + "].value", rule.value(), defs);
                                checkSelector(result, "temperature.field.rules[" + i + "].seletor", rule.selector());
                            }
                        }
                    }
                } else {
                    result.add("temperature.field", "field must not be empty");
                }
            }
        }

        // 5) Проверка boundaryConditions
        if (simulationCase.boundaryConditions() == null) {
            result.add("boundaryConditions", "boundaryConditions must not be empty");
        } else if (simulationCase.boundaryConditions().field() == null) {
            result.add("boundaryConditions.field", "field must not bu empty");
        } else {
            Map<String, ?> bcDefs = simulationCase.boundaryConditions().definitions();
            if (bcDefs == null || bcDefs.isEmpty()) {
                result.add("boundaryConditions.definitions", "definitions must not be empty");
            } else {
                Map<Face, Field<String>> bcByFacesMap = simulationCase.boundaryConditions().field().faces();
                if (bcByFacesMap == null || bcByFacesMap.isEmpty()) {
                    result.add("boundaryConditions.field.faces", "faces must not be empty");
                } else {
                    for (Face face : Face.values()) {
                        Field<String> field = bcByFacesMap.get(face);
                        if (field == null) {
                            result.add("boundaryConditions.field.faces." + face, "face entry is missing");
                            continue;
                        }
                        checkRef(result, "boundaryConditions.field.faces." + face + ".default", field.defaultValue(),
                                bcDefs);

                        List<Rule<String>> rules = field.rules();
                        if (rules != null) {
                            for (int i = 0; i < rules.size(); i++) {
                                Rule<String> rule = rules.get(i);
                                if (rule != null) {
                                    checkRef(result,
                                            "boundaryConditions.field.faces." + face + ".rules[" + i + "].value",
                                            rule.value(),
                                            bcDefs);
                                    checkSelector(result,
                                            "boundaryConditions.field.faces." + face + ".rules[" + i + "].selector",
                                            rule.selector());
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private static void checkRef(ValidationResult result, String path, String ref, Map<String, ?> defs) {
        if (ref == null || ref.isBlank()) {
            result.add(path, "reference must not be blank");
        } else if (!defs.containsKey(ref)) {
            result.add(path, "unknown reference '" + ref + "'");
        }
    }

    // Проверка значения температуры
    // ConstantTemperature
    private static void checkTemperatureValue(ValidationResult result, String path, TemperatureDefinition value) {
        if (value == null) {
            result.add(path, "temperature value must not be blank");
        }

        switch (value.type()) {
            case CONSTANT:
                double temperature = value.temperature();
                if (temperature < MIN_TEMPERATURE || temperature > MAX_TEMPERATURE) {
                    result.add(path,
                            "temperature must be between " + MIN_TEMPERATURE + " C and " + MAX_TEMPERATURE + " C.");
                }
                break;

            default:
                result.add(path,
                        "unknown temperature type");
                break;
        }
    }

    // Проверки селекторов (минимум)
    // BOX, Z_RANGE
    private static void checkSelector(ValidationResult result, String path, Selector selector) {

        if (selector == null) {
            result.add(path, "selector must not be emplty");
        } else if (selector.getClass() == ZRangeSelector.class) {
            checkZ_RANGE(result, path, (ZRangeSelector) selector);
        } else if (selector.getClass() == BoxSelector.class) {
            checkBOX(result, path, (BoxSelector) selector);
        } else {
            result.add(path, "selector has unknown type");
        }

    }

    private static void checkZ_RANGE(ValidationResult result, String path, ZRangeSelector selector) {
        double zMin = selector.minZMeters();
        double zMax = selector.maxZMeters();

        if (zMax < zMin) {
            result.add(path, "zMax must be bigger then zMin");
        }
    }

    private static void checkBOX(ValidationResult result, String path, BoxSelector selector) {
        double[] min = selector.min();
        double[] max = selector.max();
        if (max.length != 3) {
            result.add(path, "there must be 3 points for top face of box");
        }
        if (min.length != 3) {
            result.add(path, "there must be 3 points for bottom face of box");
        }
        if (max[2] < min[2]) {
            result.add(path, "top fae must be hier then bottom face");
        }
    }
}
