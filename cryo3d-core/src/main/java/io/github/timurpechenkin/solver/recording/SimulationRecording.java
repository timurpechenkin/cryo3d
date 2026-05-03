package io.github.timurpechenkin.solver.recording;

import java.util.List;

/**
 * Результат расчёта, содержащий историю состояний системы
 * в контрольных точках и на профилях.
 *
 * <p>
 * Все временные ряды в результате используют одну и ту же временную сетку:
 * {@code timeSeconds[step]} соответствует состоянию системы на шаге
 * {@code step}.
 *
 * <p>
 * Результат не обязан хранить полное поле температуры во всём 3D объёме.
 * Он содержит только те данные, которые нужны для анализа, вывода и экспорта:
 * историю температур по точкам и профилям.
 *
 * @param pointSeries   история температур по контрольным точкам
 * @param profileSeries история температур по профилям
 */
public record SimulationRecording(
                List<SamplePointSeries> pointSeries,
                List<ProfileSeries> profileSeries) {
}