package io.github.timurpechenkin.solver.context;

public class CaseContextFactoryRegistry {
    private final static String IDENTITY = "Direct";
    private final static String STEFAN_ENTHALPY = "StefanEnthalpy";

    public CaseContextFactory get(String key) {
        return switch (key) {
            case IDENTITY -> new DirectCaseContextFactory();
            case STEFAN_ENTHALPY -> new StefanEnthalpyCaseContextFactory();
            default -> throw new IllegalArgumentException("Unknown key for CaseContext: " + key);
        };
    }
}
