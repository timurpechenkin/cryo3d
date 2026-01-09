package io.github.timurpechenkin.casefile.validation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.timurpechenkin.casefile.SimulationCase;
import io.github.timurpechenkin.casefile.bc.Face;
import io.github.timurpechenkin.casefile.common.Field;
import io.github.timurpechenkin.casefile.common.Rule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public final class CaseValidator {
    private final Validator validator;

    public CaseValidator() {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public ValidationResult validate(SimulationCase simulationCase) {
        ValidationResult result = new ValidationResult();

        if (simulationCase == null) {
            result.add("", "Case is null");
            return result;
        }

        // 1) Bean Validation (аннотации)
        Set<ConstraintViolation<SimulationCase>> violations = validator.validate(simulationCase);
        for (ConstraintViolation<SimulationCase> v : violations) {
            result.add(v.getPropertyPath().toString(), v.getMessage());
        }

        // 2) Смысловые проверки
        if (simulationCase.time() != null) {
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

        // 3) Проверка ссылок на materials.definitions
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
                        }
                    }
                }
            }
        }

        // 4) Проверка ссылок на boundaryConditions.definitions по граням
        if (simulationCase.boundaryConditions() != null && simulationCase.boundaryConditions().field() != null) {
            Map<String, ?> bcDefs = simulationCase.boundaryConditions().definitions();
            if (bcDefs == null || bcDefs.isEmpty()) {
                result.add("boundaryConditions.definitions", "definitions must not be empty");
            } else {
                Map<Face, Field<String>> faces = simulationCase.boundaryConditions().field().faces();
                if (faces == null || faces.isEmpty()) {
                    result.add("boundaryConditions.field.faces", "faces must not be empty");
                } else {
                    for (Face face : Face.values()) {
                        Field<String> field = faces.get(face);
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
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5) Проверки селекторов (минимум)
        // BOX min/max длины 3, Z_RANGE zMin<=zMax — лучше сделать отдельной функцией,
        // но можно добавить позже (как только заведём методы или instanceof).

        return result;
    }

    private static void checkRef(ValidationResult result, String path, String ref, Map<String, ?> defs) {
        if (ref == null || ref.isBlank()) {
            result.add(path, "reference must not be blank");
        } else if (!defs.containsKey(ref)) {
            result.add(path, "unknown reference '" + ref + "'");
        }
    }
}
