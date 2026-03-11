package io.github.timurpechenkin.solver.calculator;

import io.github.timurpechenkin.solver.context.CaseContext;

/**
 * Временная заглушка численной схемы.
 *
 * <p>
 * Просто копирует температурное поле без изменений.
 * Полезно для проверки контура расчёта, накопления результата и вывода.
 */
public final class IdentityStepCalculator implements StepCalculator {

    @Override
    public void calculateStep(
            CaseContext context,
            double[] currentTemperatureCByCell,
            double[] nextTemperatureCByCell,
            long dtSeconds) {

        System.arraycopy(currentTemperatureCByCell, 0, nextTemperatureCByCell, 0, currentTemperatureCByCell.length);
    }

}