package io.github.timurpechenkin.casefile.validation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.timurpechenkin.Constants.*;

import io.github.timurpechenkin.casefile.dto.SimulationCase;
import io.github.timurpechenkin.casefile.dto.bc.Face;
import io.github.timurpechenkin.casefile.dto.common.Field;
import io.github.timurpechenkin.casefile.dto.common.Rule;
import io.github.timurpechenkin.casefile.dto.grid.Segment;
import io.github.timurpechenkin.casefile.dto.selector.BoxSelector;
import io.github.timurpechenkin.casefile.dto.selector.Selector;
import io.github.timurpechenkin.casefile.dto.selector.ZRangeSelector;
import io.github.timurpechenkin.casefile.dto.temperature.ConstantTemperature;
import io.github.timurpechenkin.casefile.dto.temperature.TemperatureValue;
import io.github.timurpechenkin.geometry.Axis;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public final class CaseValidator {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public CaseValidator() {
    }

    public ValidationResult validate(SimulationCase simulationCase) {
        ValidationResult result = new ValidationResult();

        if (simulationCase == null) {
            result.add("", "Case is null");
            return result;
        }

        // Bean Validation (аннотации)
        Set<ConstraintViolation<SimulationCase>> violations = VALIDATOR.validate(simulationCase);
        for (ConstraintViolation<SimulationCase> v : violations) {
            result.add(v.getPropertyPath().toString(), v.getMessage());
        }

        // СМЫСЛОВЫЕ ПРОВЕРКИ
        // 1) Проверка параметров сетки
        if (simulationCase.grid() == null) {
            result.add("grid", "grid params must not be empty");
        } else {
            Map<Axis, List<Segment>> axesSegments = simulationCase.grid().axesSegments();

            for (Axis axis : axesSegments.keySet()) {
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

                        int intFrom = (int) Math.round(from * SCALE);
                        int intTo = (int) Math.round(to * SCALE);
                        int intStep = (int) Math.round(step * SCALE);

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
            long dt = simulationCase.time().dtSeconds();
            long save = simulationCase.time().saveEverySeconds();
            long total = simulationCase.time().totalSeconds();

            if (save < dt)
                result.add("time.saveEverySeconds", "saveEverySeconds must be >= dtSeconds");
            if (save % dt != 0)
                result.add("time.saveEverySeconds", "saveEverySeconds must be multiple of dtSeconds");
            if (total % dt != 0)
                result.add("time.totalSeconds", "totalSeconds must be multiple of dtSeconds");
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

        // 4) Проверка boundaryConditions
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

        // 5) Проверка температур
        if (simulationCase.temperature() == null) {
            result.add("temperature", "temperature must not be empty");
        } else {
            Field<TemperatureValue> field = simulationCase.temperature().field();
            if (field == null) {
                result.add("temperature.field", "temperature field must not be empty");
            } else {
                checkTemperatureValue(result, "temperature.field.default.temperature", field.defaultValue());
                List<Rule<TemperatureValue>> rules = field.rules();
                if (rules != null) {
                    for (int i = 0; i < rules.size(); i++) {
                        Rule<TemperatureValue> rule = rules.get(i);
                        if (rule != null) {
                            checkSelector(result, "temperature.field.rule[" + i + "].seletor", rule.selector());
                            checkTemperatureValue(result, "temperature.field.rule[" + i + "].value", rule.value());
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
    private static void checkTemperatureValue(ValidationResult result, String path, TemperatureValue value) {
        if (value != null && value.getClass() == ConstantTemperature.class) {
            ConstantTemperature constTemperature = (ConstantTemperature) value;
            double temperature = constTemperature.temperature();
            if (temperature < MIN_TEMPERATURE || temperature > MAX_TEMPERATURE) {
                result.add(path,
                        "temperature must be between " + MIN_TEMPERATURE + " C and " + MAX_TEMPERATURE + " C.");
            }
        } else {
            result.add(path,
                    "unknown temperature type");
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
        double zMin = selector.zMin();
        double zMax = selector.zMax();

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
