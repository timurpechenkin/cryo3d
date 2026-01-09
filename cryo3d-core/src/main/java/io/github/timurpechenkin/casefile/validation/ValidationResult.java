package io.github.timurpechenkin.casefile.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidationResult {
    private final List<ValidationError> errors = new ArrayList<>();

    public void add(String path, String message) {
        errors.add(new ValidationError(path, message));
    }

    public void addAll(List<ValidationError> additionalErrors) {
        errors.addAll(additionalErrors);
    }

    public boolean isOk() {
        return errors.isEmpty();
    }

    public List<ValidationError> errors() {
        return Collections.unmodifiableList(errors);
    }
}
