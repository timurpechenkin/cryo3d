package io.github.timurpechenkin.solver.context;

public class CaseContextFactoryRegistry {
    private final static String IDENTITY = "DirectCaseContext";

    public CaseContextFactory get(String key) {
        return switch (key) {
            case IDENTITY -> new DirectCaseContextFactory();
            default -> throw new IllegalArgumentException("Unknown key for CaseContext: " + key);
        };
    }
}
