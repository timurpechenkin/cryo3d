package io.github.timurpechenkin.solver;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.config.CaseConfig;
import io.github.timurpechenkin.solver.calculator.StepCalculator;
import io.github.timurpechenkin.solver.calculator.StepCalculatorRegistry;
import io.github.timurpechenkin.solver.context.CaseContextFactory;
import io.github.timurpechenkin.solver.context.CaseContextFactoryRegistry;

public final class CaseSolverFactory {
    private final StepCalculatorRegistry stepCalculatorRegistry = new StepCalculatorRegistry();
    private final CaseContextFactoryRegistry contextFactoryRegistry = new CaseContextFactoryRegistry();

    public CaseSolverFactory() {
    }

    public CaseSolver create(SimulationCase simulationCase) {
        CaseConfig config = simulationCase.solverConfig();
        StepCalculator calculator = stepCalculatorRegistry.get(config.stepCalculatorKey());
        CaseContextFactory contextFactory = contextFactoryRegistry.get(config.materialModelKey());
        return new DefaultCaseSolver(calculator, contextFactory);
    }
}