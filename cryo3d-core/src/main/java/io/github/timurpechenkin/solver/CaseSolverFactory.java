package io.github.timurpechenkin.solver;

import io.github.timurpechenkin.domain.SimulationCase;
import io.github.timurpechenkin.domain.solver.SolverSettings;
import io.github.timurpechenkin.solver.calculator.StepCalculator;
import io.github.timurpechenkin.solver.calculator.StepCalculatorRegistry;
import io.github.timurpechenkin.solver.context.CaseContextFactory;
import io.github.timurpechenkin.solver.context.CaseContextFactoryRegistry;
import io.github.timurpechenkin.solver.progress.SimulationProgressListener;

public final class CaseSolverFactory {
    private final StepCalculatorRegistry stepCalculatorRegistry = new StepCalculatorRegistry();
    private final CaseContextFactoryRegistry contextFactoryRegistry = new CaseContextFactoryRegistry();

    public CaseSolverFactory() {
    }

    public CaseSolver create(SimulationCase simulationCase,
            SimulationProgressListener progressListener, int targetProgressUpdates) {
        SolverSettings solverSettings = simulationCase.solver();
        StepCalculator calculator = stepCalculatorRegistry.get(solverSettings.stepCalculatorKey());
        CaseContextFactory contextFactory = contextFactoryRegistry.get(solverSettings.materialModelKey());
        return new DefaultCaseSolver(simulationCase, calculator, contextFactory, progressListener,
                targetProgressUpdates);
    }
}