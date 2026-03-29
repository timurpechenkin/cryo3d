package io.github.timurpechenkin.solver.calculator;

public class StepCalculatorRegistry {
    private final static String EXPLICIT_NO_PHASE = "ExplicitNoPhaseStepCalculator";
    private final static String IDENTITY = "IdentityStepCalculator";

    public StepCalculator get(String key) {
        return switch (key) {
            case IDENTITY -> new IdentityStepCalculator();
            case EXPLICIT_NO_PHASE -> new ExplicitNoPhaseStepCalculator();
            default -> throw new IllegalArgumentException("Unknown key for StepCalculator: " + key);
        };
    }
}
